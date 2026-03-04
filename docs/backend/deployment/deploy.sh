#!/bin/bash
# =============================================================
#  ALB Rolling 무중단 배포 스크립트
#  Usage: ./deploy.sh <project> [image-tag]
#  Example: ./deploy.sh user-api latest
#           ./deploy.sh user-api abc1234
#
#  서버의 /opt/deploy/deploy.sh 에 복사하여 사용한다.
#  프로젝트별 설정은 /opt/deploy/projects/<project>.env 에 정의한다.
#
#  배포 흐름:
#    1. ALB 타겟 그룹에서 이 인스턴스를 제거 (drain)
#    2. Deregistration delay 대기 (기존 요청 완료)
#    3. 기존 컨테이너 graceful shutdown
#    4. 새 이미지 pull → 새 컨테이너 기동
#    5. Health check 통과 확인
#    6. ALB 타겟 그룹에 다시 등록
# =============================================================
set -euo pipefail

# ----- 인자 파싱 -----
PROJECT="${1:?Usage: $0 <project> [image-tag]}"
TAG="${2:-latest}"
ENV_FILE="/opt/deploy/projects/${PROJECT}.env"

if [ ! -f "$ENV_FILE" ]; then
    echo "[ERROR] 환경 파일 없음: $ENV_FILE"
    exit 1
fi

source "$ENV_FILE"

FULL_IMAGE="${IMAGE}:${TAG}"
CONTAINER_NAME="${PROJECT}"

echo "======================================"
echo " Project : $PROJECT"
echo " Image   : $FULL_IMAGE"
echo " Port    : $PORT"
echo "======================================"

# ----- EC2 인스턴스 ID 자동 감지 (IMDSv2) -----
echo "[INFO] 인스턴스 ID 조회..."
IMDS_TOKEN=$(curl -sf -X PUT "http://169.254.169.254/latest/api/token" \
    -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")
INSTANCE_ID=$(curl -sf -H "X-aws-ec2-metadata-token: $IMDS_TOKEN" \
    "http://169.254.169.254/latest/meta-data/instance-id")
echo "[INFO] 인스턴스 ID: $INSTANCE_ID"

# ----- 1. ALB 타겟 그룹에서 제거 (drain) -----
echo "[INFO] ALB 타겟 그룹에서 제거: $TARGET_GROUP_ARN"
aws elbv2 deregister-targets \
    --target-group-arn "$TARGET_GROUP_ARN" \
    --targets "Id=$INSTANCE_ID"

echo "[INFO] Deregistration 대기 (최대 ${DEREGISTER_TIMEOUT}초)..."
aws elbv2 wait target-deregistered \
    --target-group-arn "$TARGET_GROUP_ARN" \
    --targets "Id=$INSTANCE_ID" 2>/dev/null || true

# 추가 대기: deregistration delay 동안 기존 연결이 완료되도록 보장
sleep 5

echo "[OK] ALB에서 제거 완료. 새 트래픽 차단됨."

# ----- 2. 이미지 Pull -----
echo "[INFO] 이미지 Pull: ${FULL_IMAGE}"
docker pull "$FULL_IMAGE"

# ----- 3. 기존 컨테이너 Graceful Shutdown -----
if docker ps -q --filter "name=^${CONTAINER_NAME}$" | grep -q .; then
    echo "[INFO] 기존 컨테이너 종료: ${CONTAINER_NAME} (graceful, 30초 대기)"
    docker stop -t 30 "$CONTAINER_NAME" 2>/dev/null || true
    docker rm -f "$CONTAINER_NAME" 2>/dev/null || true
else
    echo "[INFO] 기존 컨테이너 없음. 최초 배포."
    docker rm -f "$CONTAINER_NAME" 2>/dev/null || true
fi

# ----- 4. 새 컨테이너 기동 -----
# DOCKER_ENV_ 접두사 변수를 자동으로 -e 플래그로 변환
DOCKER_ENV_FLAGS=""
while IFS='=' read -r key value; do
    [[ -z "$key" || "$key" == \#* ]] && continue
    if [[ "$key" == DOCKER_ENV_* ]]; then
        ENV_NAME="${key#DOCKER_ENV_}"
        DOCKER_ENV_FLAGS="${DOCKER_ENV_FLAGS} -e ${ENV_NAME}=${value}"
    fi
done < "$ENV_FILE"

echo "[INFO] 컨테이너 기동: ${CONTAINER_NAME} (port ${PORT})"
docker run -d \
    --name "${CONTAINER_NAME}" \
    --network host \
    --restart unless-stopped \
    -v /app/logs:/app/logs \
    -e SERVER_PORT="${PORT}" \
    -e SPRING_PROFILES_ACTIVE="${SPRING_PROFILES}" \
    -e LOGGING_FILE_NAME="${LOG_FILE}" \
    -e LOGGING_LOGBACK_ROLLINGPOLICY_FILE_NAME_PATTERN="${LOG_ROLLING_PATTERN}" \
    -e LOGGING_LOGBACK_ROLLINGPOLICY_MAX_HISTORY="${LOG_MAX_HISTORY}" \
    -e LOGGING_LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE="${LOG_MAX_FILE_SIZE}" \
    -e LOGGING_LOGBACK_ROLLINGPOLICY_TOTAL_SIZE_CAP="${LOG_TOTAL_SIZE_CAP}" \
    $DOCKER_ENV_FLAGS \
    "$FULL_IMAGE"

# ----- 5. Health Check -----
echo "[INFO] Health check 대기 (최대 ${HEALTH_TIMEOUT}초)..."
ELAPSED=0
while [ $ELAPSED -lt "${HEALTH_TIMEOUT}" ]; do
    if curl -sf "http://localhost:${PORT}${HEALTH_PATH}" > /dev/null 2>&1; then
        echo "[OK] Health check 통과 (${ELAPSED}초)"
        break
    fi
    sleep 2
    ELAPSED=$((ELAPSED + 2))
done

if [ $ELAPSED -ge "${HEALTH_TIMEOUT}" ]; then
    echo "[FAIL] Health check 실패. 컨테이너 로그:"
    docker logs "${CONTAINER_NAME}" --tail 50
    echo ""
    echo "[WARN] ALB에 재등록하지 않음. 수동 확인 필요."
    exit 1
fi

# ----- 6. ALB 타겟 그룹에 재등록 -----
echo "[INFO] ALB 타겟 그룹에 재등록: $TARGET_GROUP_ARN"
aws elbv2 register-targets \
    --target-group-arn "$TARGET_GROUP_ARN" \
    --targets "Id=$INSTANCE_ID"

echo "[INFO] ALB healthy 상태 대기..."
HEALTH_ELAPSED=0
HEALTH_MAX=120
while [ $HEALTH_ELAPSED -lt $HEALTH_MAX ]; do
    STATUS=$(aws elbv2 describe-target-health \
        --target-group-arn "$TARGET_GROUP_ARN" \
        --targets "Id=$INSTANCE_ID" \
        --query 'TargetHealthDescriptions[0].TargetHealth.State' \
        --output text 2>/dev/null || echo "unknown")
    if [ "$STATUS" = "healthy" ]; then
        echo "[OK] ALB 타겟 healthy (${HEALTH_ELAPSED}초)"
        break
    fi
    sleep 5
    HEALTH_ELAPSED=$((HEALTH_ELAPSED + 5))
done

if [ $HEALTH_ELAPSED -ge $HEALTH_MAX ]; then
    echo "[WARN] ALB healthy 대기 타임아웃 (${HEALTH_MAX}초). 현재 상태: $STATUS"
    echo "[WARN] ALB가 곧 healthy로 전환될 수 있습니다. 수동 확인 권장."
fi

echo "======================================"
echo " 배포 완료: ${CONTAINER_NAME} (port ${PORT})"
echo "======================================"
