# ALB Rolling 무중단 배포 가이드

ALB + EC2에서 Docker + Nginx + GitHub Actions를 이용한 Rolling 무중단 배포 전략.
배포 시 ALB 타겟 그룹에서 인스턴스를 제거한 후 배포하고, 완료 후 다시 등록한다.
EC2마다 1개의 프로젝트를 운영하며, 대상 프로젝트만 바꾸면 동일한 구조를 그대로 재사용할 수 있다.

---

## 로컬(내 컴퓨터) 세팅

서버를 세팅하기 전에 내 컴퓨터에서 먼저 준비해야 하는 항목들이다.

```bash
# 1. GitHub Secrets 등록 (GitHub Actions가 서버에 SSH 접속하기 위해 필요)
#    리포지토리 Settings → Secrets and variables → Actions
#    ┌────────────────────────┬──────────────────────────────────────────┐
#    │ Secret                 │ 값                                       │
#    ├────────────────────────┼──────────────────────────────────────────┤
#    │ USER_API_SERVER_HOST   │ user EC2 IP (예: 10.0.1.10)         │
#    │ ADMIN_API_SERVER_HOST  │ admin EC2 IP (예: 10.0.1.20)        │
#    │                        │ 2대 이상: 쉼표로 구분 (예: 10.0.1.10,10.0.1.11) │
#    │                        │ → 첫 번째 서버 배포 완료 후 두 번째 서버 순차 배포  │
#    │ SERVER_USER            │ EC2 기본 유저 (예: ec2-user)              │
#    │ SSH_PRIVATE_KEY        │ EC2 키페어 .pem 파일 내용 전체             │
#    └────────────────────────┴──────────────────────────────────────────┘
#    → SSH_PRIVATE_KEY에는 EC2 인스턴스 생성 시 발급받은 .pem 파일 내용을 그대로 붙여넣는다.
#      cat ~/.ssh/my-key.pem  →  전체 복사 후 Secret value에 입력
#
#    참고: GHCR push는 워크플로우의 GITHUB_TOKEN이 자동 처리하므로 별도 설정 불필요.

# 2. GHCR PAT 발급 (서버에서 이미지 pull 시 사용 — GitHub Actions와는 무관)
#    GitHub → Settings → Developer settings → Personal access tokens
#    → Generate new token → 스코프: read:packages
#    → 발급된 토큰을 메모 (서버 세팅 3단계 docker login에서 입력)

# 3. 배포 브랜치 생성 (최초 1회)
git push origin develop:deploy/user
git push origin develop:deploy/admin
#    → 각 프로젝트의 GitHub Actions 워크플로우가
#      on.push.branches에 해당 브랜치를 트리거로 등록하고 있다.
#      따라서 deploy/user 브랜치에 push하면 워크플로우가 자동 실행되어
#      JAR 빌드 → Docker 이미지 → GHCR push → SSH 배포까지 진행된다.
#
#      워크플로우 파일 위치:
#        원본: docs/backend/deployment/user/deploy-user.yml
#        실제: .github/workflows/deploy-user.yml (GitHub Actions가 인식하는 경로)
```

끝. 이제 아래 AWS 세팅을 진행한다.

---

## AWS 세팅

서버 세팅 전에 AWS 콘솔에서 먼저 준비해야 하는 항목들이다.
프로젝트당 1회 수행하며, user 기준으로 설명한다. admin도 동일한 절차.

### 1. EC2 인스턴스 생성

| 항목           | 설정                                                  |
|--------------|-----------------------------------------------------|
| AMI          | Amazon Linux 2023 (AL2023)                          |
| 인스턴스 타입      | t3a.small (2 vCPU, 2GB) 또는 t3a.medium (2 vCPU, 4GB) |
| 키페어          | 기존 키페어 선택 또는 신규 생성 (`.pem` 파일 보관)                   |
| 보안 그룹 (인바운드) | 아래 표 참조                                             |

**보안 그룹 인바운드 규칙:**

| 포트 | 소스          | 용도                |
|----|-------------|-------------------|
| 80 | ALB 보안 그룹   | ALB → Nginx → App |
| 22 | 내 IP 또는 VPN | SSH 접속 (배포 + 관리)  |

> EC2에 직접 접근하는 것은 ALB(포트 80)와 SSH(포트 22)뿐이다. 퍼블릭 80/443 트래픽은 ALB가 처리한다.
> 앱 포트(8080)는 localhost에서만 접근하므로 보안 그룹에 추가하지 않는다.

### 2. IAM 역할 생성 & EC2 연결

deploy.sh가 AWS CLI로 ALB 타겟을 조작하므로 EC2에 IAM 역할이 필요하다.

**IAM 콘솔 → 역할 → 역할 생성:**

1. 신뢰할 수 있는 엔터티: **AWS 서비스 → EC2**
2. 권한 정책: 인라인 정책 추가 →

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowALBTargetManagement",
      "Effect": "Allow",
      "Action": [
        "elasticloadbalancing:RegisterTargets",
        "elasticloadbalancing:DeregisterTargets",
        "elasticloadbalancing:DescribeTargetHealth"
      ],
      "Resource": "*"
    }
  ]
}
```

3. 역할 이름: `ec2-deploy-role` (예시)

**EC2에 역할 연결:**

> EC2 → 인스턴스 선택 → 작업 → 보안 → IAM 역할 수정 → `ec2-deploy-role` 선택

이 역할이 연결되면 EC2 내에서 `aws elbv2` 명령이 별도 credentials 없이 동작한다.

### 3. ALB 타겟 그룹 생성

deploy.sh가 타겟을 등록/해제할 대상이다. 프로젝트마다 별도의 타겟 그룹을 만든다.

**EC2 → 타겟 그룹 → 타겟 그룹 생성:**

| 항목                    | 설정                |
|-----------------------|-------------------|
| Target type           | **Instances**     |
| 타겟 그룹 이름              | `user-tg`     |
| Protocol / Port       | **HTTP / 80**     |
| VPC                   | EC2가 속한 VPC       |
| Health check protocol | HTTP              |
| Health check path     | **`/api/health`** |
| Healthy threshold     | 2                 |
| Unhealthy threshold   | 3                 |
| Health check interval | 30초               |
| Success codes         | 200               |

**타겟 등록:**

> 타겟 그룹 → Targets 탭 → Register targets → user EC2 선택 → 포트 80 → Include as pending

**Deregistration delay 변경 (중요):**

> 타겟 그룹 → Attributes 탭 → Edit → Deregistration delay → **120초** (기본 300초에서 변경)

기본값 300초는 배포 시 5분을 무의미하게 대기한다.
REST API 기준 30초면 진행 중인 요청 완료에 충분하다.

**생성 후 ARN 복사:**

타겟 그룹 상세에서 ARN을 복사하여 `.env` 파일의 `TARGET_GROUP_ARN`에 입력한다.

```
arn:aws:elasticloadbalancing:ap-northeast-2:123456789012:targetgroup/user-tg/abc123def456
```

### 4. ALB 리스너 규칙 추가

ALB가 트래픽을 올바른 타겟 그룹으로 라우팅하도록 규칙을 추가한다.

**EC2 → 로드밸런서 → ALB 선택 → 리스너 (HTTPS:443) → 규칙 관리:**

| 조건 (IF)                           | 액션 (THEN)                 |
|-----------------------------------|---------------------------|
| Host header = `api.example.com`   | Forward to `user-tg`  |
| Host header = `admin.example.com` | Forward to `admin-tg` |
| 기본 규칙                             | Fixed response 404        |

> 호스트 기반 라우팅으로 하나의 ALB에서 여러 프로젝트를 처리한다.
> 경로 기반(`/api/*`)도 가능하지만, 프로젝트별 도메인 분리를 권장한다.

### 5. Route 53 DNS 설정

도메인을 ALB에 연결한다.

**Route 53 → 호스팅 영역 → 레코드 생성:**

| 레코드 이름              | 타입 | 라우팅         | 대상     |
|---------------------|----|-------------|--------|
| `api.example.com`   | A  | Alias → ALB | ALB 선택 |
| `admin.example.com` | A  | Alias → ALB | ALB 선택 |

### 요약 — AWS에서 확인할 것

| 리소스        | 핵심 설정                                                          | 확인 방법                    |
|------------|----------------------------------------------------------------|--------------------------|
| EC2        | t3a.small/medium, AL2023, 보안 그룹(80, 22)                        | EC2 콘솔 → 인스턴스            |
| IAM 역할     | `RegisterTargets`, `DeregisterTargets`, `DescribeTargetHealth` | EC2 → IAM 역할 확인          |
| ALB 타겟 그룹  | HTTP/80, health: `/api/health`, dereg delay: **120초**          | EC2 → 타겟 그룹 → Attributes |
| ALB 리스너 규칙 | 호스트 기반 라우팅 → 타겟 그룹                                             | EC2 → 로드밸런서 → 리스너 → 규칙   |
| Route 53   | A 레코드 Alias → ALB                                              | Route 53 → 호스팅 영역        |
| ACM        | SSL 인증서 발급 & ALB 리스너에 연결                                       | ACM 콘솔 → 인증서 목록          |
| WAF (선택)   | ALB에 Web ACL 연결                                                | WAF 콘솔 → Web ACLs        |

> ACM, WAF, ALB 자체의 생성은 이미 되어 있다고 가정한다.
> 새 프로젝트 추가 시에는 **타겟 그룹 생성 + 리스너 규칙 추가 + DNS 레코드 추가**만 하면 된다.

---

## 서버 세팅 (user 기준)

서버를 새로 만들면 아래 8단계를 순서대로 실행한다. 10분이면 끝난다.

```bash
# 1. Docker + AWS CLI 설치
sudo dnf install -y docker aws-cli
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
newgrp docker

# 2. Nginx + cronie 설치
sudo dnf install -y nginx cronie
sudo systemctl enable --now nginx crond

# 3. GHCR 로그인 (이미지 pull 용)
echo "<GHCR_PAT>" | docker login ghcr.io -u <GITHUB_USERNAME> --password-stdin

# 4. 디렉토리 생성
sudo mkdir -p /opt/deploy/projects
sudo chown -R $USER:$USER /opt/deploy

sudo mkdir -p /app/{logs,backup}
sudo chown -R $USER:$USER /app

# 5. 파일 복사 (docs/backend/deployment/ 에서)
cp deploy.sh /opt/deploy/deploy.sh && chmod +x /opt/deploy/deploy.sh
cp user/user.env /opt/deploy/projects/
sudo cp user/nginx/user.conf /etc/nginx/conf.d/

# 6. 서버별 값 수정
#    /opt/deploy/projects/user.env
#      → IMAGE, SPRING_PROFILES, TARGET_GROUP_ARN (실제 타겟 그룹 ARN)
#    /etc/nginx/conf.d/user.conf
#      → server_name

# 7. Nginx 검증 & 적용
sudo nginx -t && sudo nginx -s reload

# ── 결과 디렉토리 구조 ─────────────────────
# /opt/deploy/
# ├── deploy.sh                          ← 범용 배포 스크립트
# └── projects/
#     └── user.env                   ← 배포 설정 (TARGET_GROUP_ARN 포함)
#
# /app/
# ├── logs/                              ← 앱 로그 (컨테이너에서 볼륨 마운트)
# │   ├── app.log                        ← 현재 로그
# │   └── app-2026-03-01.0.log           ← 일별 롤링 파일
# └── backup/                            ← 로그 백업 (cron이 관리)
#
# /etc/nginx/conf.d/
# └── user.conf                      ← Nginx server 블록

# 8. 로그 백업 cron 등록
#    매일 새벽 2시: /app/logs의 롤링 로그(1일 이상 경과)를 gzip 압축 후 /app/backup으로 이동
#    매일 새벽 3시: /app/backup에서 90일 넘은 백업 파일 자동 삭제
(crontab -l 2>/dev/null; echo '0 2 * * * find /app/logs -name "app-*.log" -mtime +1 -exec gzip {} \; -exec mv {}.gz /app/backup/ \;') | crontab -
(crontab -l 2>/dev/null; echo '0 3 * * * find /app/backup -name "*.gz" -mtime +90 -delete') | crontab -
```

끝. 이후 `deploy/user` 브랜치에 push하면 자동 배포된다.

---

## 빠른 요약 — 서버에 필요한 것

user 서버 세팅 기준. 다른 프로젝트는 해당 프로젝트 디렉토리의 파일을 사용한다.

### 파일

| 서버 경로                               | 원본                                                             | 비고                                             |
|-------------------------------------|----------------------------------------------------------------|------------------------------------------------|
| `/opt/deploy/deploy.sh`             | [`deploy.sh`](deploy.sh)                                       | 모든 서버 동일                                       |
| `/opt/deploy/projects/user.env` | [`user/user.env`](user/user.env)               | `IMAGE`, `SPRING_PROFILES`, `TARGET_GROUP_ARN` |
| `/etc/nginx/conf.d/user.conf`   | [`user/nginx/user.conf`](user/nginx/user.conf) | `server_name` 수정                               |

### 서버마다 달라지는 값

| 항목                 | 파일         | 설명                    |
|--------------------|------------|-----------------------|
| `IMAGE`            | `.env`     | GHCR 이미지 경로의 OWNER 부분 |
| `SPRING_PROFILES`  | `.env`     | `prod`, `staging` 등   |
| `TARGET_GROUP_ARN` | `.env`     | ALB 타겟 그룹 ARN         |
| `server_name`      | Nginx conf | 실제 도메인                |

> `deploy.sh`, 포트(8080), health check 경로, 디렉토리 구조는 모든 서버에서 동일하다.

### 프로젝트별 파일

| 프로젝트      | 디렉토리                                               |
|-----------|----------------------------------------------------|
| user  | [`docs/backend/deployment/user/`](user/)   |
| admin | [`docs/backend/deployment/admin/`](admin/) |

---

## 1. 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│  GitHub                                                 │
│  push → Actions → build JAR → Docker image → Push GHCR  │
└──────────────┬──────────────────────┬───────────────────┘
               │ SSH                  │ SSH
┌──────────────▼───────┐  ┌──────────▼────────────┐
│  ALB (HTTPS:443)     │  │                       │
│  + ACM SSL + WAF     │  │                       │
└──────┬─────────┬─────┘  │                       │
       │         │        │                       │
┌──────▼──┐  ┌───▼─────┐  │                       │
│ EC2-1   │  │ EC2-2   │  │                       │
│ Nginx   │  │ Nginx   │  │                       │
│ (:80)   │  │ (:80)   │  │                       │
│ ┌─────┐ │  │ ┌─────┐ │  │                       │
│ │ App │ │  │ │ App │ │  │                       │
│ │8080 │ │  │ │8080 │ │  │                       │
│ └─────┘ │  │ └─────┘ │  │                       │
│user │  │admin│  │                       │
└────┬────┘  └────┬────┘  │                       │
     │            │       │                       │
  ┌──▼────────────▼──┐    │                       │
  │  DB 서버          │    │                       │
  │  PostgreSQL      │    │                       │
  └──────────────────┘    │                       │
                          └───────────────────────┘
```

> ALB가 HTTPS 종료 + WAF를 처리하므로, EC2의 Nginx는 HTTP(:80)만 수신한다.
> DB는 별도 서버에 분리한다. 앱 서버에는 Docker + Nginx만 존재한다.

### 배포 흐름 (deploy.sh)

```
 ALB 라우팅 중                  배포 중                              완료
 ──────────                  ──────                              ────
 ALB → Nginx → App(8080) ✅    ALB → (타겟 제거, drain) ❌            ALB → Nginx → App(8080) ✅
                               App 교체: stop → pull → run          (타겟 재등록)
```

| 단계 | 동작                                   | 실패 시                         |
|----|--------------------------------------|------------------------------|
| 1  | ALB 타겟 그룹에서 인스턴스 제거 (drain)          | —                            |
| 2  | Deregistration delay 대기 (기존 요청 완료)   | —                            |
| 3  | 새 이미지 pull                           | 컨테이너 유지, ALB 재등록 필요 (수동)     |
| 4  | 기존 컨테이너 graceful shutdown (30초)      | —                            |
| 5  | 새 컨테이너 기동                            | ALB 재등록하지 않음, 수동 확인 필요       |
| 6  | Health check (`/api/health`, 최대 60초) | 컨테이너 유지, ALB 재등록하지 않음, 수동 확인 |
| 7  | ALB 타겟 그룹에 인스턴스 재등록                  | —                            |
| 8  | ALB healthy 상태 대기                    | 자동 전환 대기                     |

> 다중 서버 환경에서는 서버를 하나씩 순차 배포하므로, 배포 중에도 나머지 서버가 트래픽을 처리한다.

### 핵심 원칙

| 원칙            | 설명                              |
|---------------|---------------------------------|
| **빌드/배포 분리**  | CI에서 빌드, 서버에서는 pull + 기동만       |
| **불변 아티팩트**   | Docker 이미지 = 배포 단위, 서버에 소스코드 없음 |
| **ALB 기반 전환** | ALB 타겟 탈착으로 무중단 배포              |
| **1서버 1프로젝트** | 서버마다 하나의 프로젝트만 운영한다             |
| **재사용**       | 프로젝트명만 바꾸면 동일 구조 적용             |

### Blue/Green과의 차이

| 항목          | Blue/Green (이전)            | ALB Rolling (현재)          |
|-------------|----------------------------|---------------------------|
| 컨테이너 수      | 2개 (Blue:8081, Green:8082) | 1개 (8080)                 |
| 트래픽 전환      | Nginx upstream 변경          | ALB 타겟 그룹 탈착              |
| 서버 사양       | t3a.large (2컨테이너 리소스)      | t3a.small/medium (1컨테이너)  |
| 롤백          | 이전 컨테이너로 즉시 전환             | 이전 이미지로 재배포 (수 분)         |
| 배포 중 서비스 유지 | 항상 1개 컨테이너가 서빙             | ALB가 다른 서버로 라우팅 (다중 서버 시) |

---

## 2. 레포지토리 구성

### 공용 파일

| 파일                                         | 배치 위치                              | 설명            |
|--------------------------------------------|------------------------------------|---------------|
| [`deploy.sh`](deploy.sh)                   | 서버 `/opt/deploy/deploy.sh`         | 범용 배포 스크립트    |
| [`backend.Dockerfile`](backend.Dockerfile) | `infra/docker/backend.Dockerfile`  | 공용 Dockerfile |
| [`backend-cd.yml`](backend-cd.yml)         | `.github/workflows/backend-cd.yml` | 재사용 워크플로우     |

### 프로젝트별 파일

| 파일                                                             | 배치 위치                                   |
|----------------------------------------------------------------|-----------------------------------------|
| [`user/deploy-user.yml`](user/deploy-user.yml) | `.github/workflows/deploy-user.yml` |
| [`user/stage-user.yml`](user/stage-user.yml)   | `.github/workflows/stage-user.yml`  |
| [`user/user.env`](user/user.env)               | 서버 `/opt/deploy/projects/user.env`  |
| [`user/nginx/user.conf`](user/nginx/user.conf) | 서버 `/etc/nginx/conf.d/user.conf`    |

> admin도 동일한 구조. [`admin/`](admin/) 디렉토리 참조.

### 수정 규칙

`docs/backend/deployment/`가 원본이다. 배포 설정을 변경할 때는 반드시 아래 순서를 따른다.

1. **`docs/backend/deployment/`의 원본 파일을 먼저 수정**한다
2. 수정한 내용을 실제 배치 위치에 동일하게 반영한다

| 원본 (docs)                        | 배치 위치                                    |
|----------------------------------|------------------------------------------|
| `backend-cd.yml`                 | `.github/workflows/backend-cd.yml`       |
| `user/deploy-user.yml`   | `.github/workflows/deploy-user.yml`  |
| `user/stage-user.yml`    | `.github/workflows/stage-user.yml`   |
| `admin/deploy-admin.yml` | `.github/workflows/deploy-admin.yml` |
| `admin/stage-admin.yml`  | `.github/workflows/stage-admin.yml`  |
| `backend.Dockerfile`             | `infra/docker/backend.Dockerfile`        |

> 원본과 배치 파일은 항상 동일한 내용을 유지해야 한다. 배치 위치만 직접 수정하면 원본과 불일치가 발생한다.

---

## 3. 배포 & 롤백

### 3.1 자동 배포

프로젝트 전용 브랜치에 push하면 해당 프로젝트만 배포된다. 변경된 파일과 무관하게 항상 동작한다.

| 브랜치                | 배포 대상     |
|--------------------|-----------|
| `deploy/user`  | user  |
| `deploy/admin` | admin |

```
git push origin deploy/user
  → deploy-user.yml 트리거
    → build JAR → Docker image → GHCR push → SSH → deploy.sh → 완료
```

### 3.2 수동 배포

**(1) GitHub UI**: Actions 탭 → 워크플로우 선택 → "Run workflow" 클릭

**(2) 서버에서 직접**:

```bash
# 최신 이미지
/opt/deploy/deploy.sh user latest

# 특정 커밋 이미지
/opt/deploy/deploy.sh user abc1234
```

### 3.3 롤백

이전 커밋의 이미지 태그로 배포하면 롤백된다. 재빌드 불필요.

```bash
# 이전 이미지 태그 확인
docker images ghcr.io/namgyunkim/mono-repo/user --format "{{.Tag}}\t{{.CreatedAt}}"

# 롤백 실행
/opt/deploy/deploy.sh user <이전-태그>
```

> Blue/Green과 달리 이전 컨테이너가 남아있지 않으므로, 이전 이미지를 다시 pull + 기동하는 과정이 필요하다.
> 롤백 소요 시간: drain 30초 + pull + 기동 + health check ≒ 1~2분.

---

## 4. 새 프로젝트 추가 체크리스트

새로운 Spring Boot 앱(예: `payment-api`)을 배포 대상에 추가할 때:

### 레포지토리

- [ ] `apps/payment-api/`에 `/api/health` 엔드포인트 존재 확인
- [ ] `docs/backend/deployment/payment-api/` 디렉토리 생성
- [ ] 기존 프로젝트 디렉토리(예: `user/`)를 복사하고 프로젝트명 변경
    - `deploy-payment-api.yml` (브랜치: `deploy/payment-api`)
    - `stage-payment-api.yml` (브랜치: `stage/payment-api`)
    - `payment-api.env` (`TARGET_GROUP_ARN`에 새 타겟 그룹 ARN)
    - `nginx/payment-api.conf`

### AWS

- [ ] ALB 타겟 그룹 생성 (HTTP/80, health check: `/api/health`, deregistration delay: 120초)
- [ ] ALB 리스너 규칙 추가 (호스트/경로 기반 라우팅)
- [ ] EC2 IAM 역할에 새 타겟 그룹 권한 확인

### 서버

- [ ] [`deploy.sh`](deploy.sh)를 `/opt/deploy/deploy.sh`에 복사 (이미 있으면 생략)
- [ ] `payment-api/` 디렉토리의 파일들을 서버에 배치 (서버 세팅 5~6단계 참조)
- [ ] `.env`의 `TARGET_GROUP_ARN`과 Nginx conf의 `server_name` 수정
- [ ] `sudo nginx -t && sudo nginx -s reload`

> `deploy.sh`와 서버 디렉토리 구조는 모든 서버에서 동일하다. 프로젝트명만 다르다.

---

## 5. 운영

### 로그 확인

```bash
# 파일 로그 실시간 확인
tail -f /app/logs/app.log

# Docker 컨테이너 로그 (stdout)
docker logs -f user

# 최근 100줄
docker logs user --tail 100

# 일별 로그 파일 목록
ls -lt /app/logs/
```

### 로그 백업 (cron)

일별 롤링 파일을 압축하여 `/app/backup`에 보관한다. 서버에서 최초 1회 설정.

```bash
# crontab 등록
crontab -e

# 매일 새벽 2시: 2일 이상 된 롤링 파일을 압축 후 /app/backup으로 이동
0 2 * * * find /app/logs -name "app-*.log" -mtime +1 -exec gzip {} \; -exec mv {}.gz /app/backup/ \;

# (선택) 90일 이상 된 백업 파일 자동 삭제
0 3 * * * find /app/backup -name "*.gz" -mtime +90 -delete
```

### 현재 상태 확인

```bash
# 실행 중인 컨테이너
docker ps --filter "name=user"

# Health check
curl -s http://localhost:8080/api/health

# ALB 타겟 상태 확인
aws elbv2 describe-target-health \
    --target-group-arn <TARGET_GROUP_ARN> \
    --query 'TargetHealthDescriptions[*].{Id:Target.Id,State:TargetHealth.State}' \
    --output table
```

### 오래된 이미지 정리

```bash
# 사용하지 않는 이미지 일괄 제거
docker image prune -a --filter "until=168h" -f
```

---

## 6. 트러블슈팅

| 증상                       | 원인                  | 해결                                                     |
|--------------------------|---------------------|--------------------------------------------------------|
| Health check 실패          | 앱 기동 느림             | `.env`의 `HEALTH_TIMEOUT` 증가                            |
| Health check 실패          | DB 연결 불가            | `application-prod.yml`의 DB 접속 정보 확인, DB 서버 방화벽/보안그룹 점검 |
| ALB deregister 실패        | IAM 권한 부족           | EC2 IAM 역할에 ELBv2 권한 확인 (AWS 세팅 → 2. IAM 역할)           |
| ALB register 후 unhealthy | Health check 설정 불일치 | 타겟 그룹의 health check 경로/포트가 앱과 일치하는지 확인                 |
| 이미지 pull 실패              | GHCR 인증 만료          | `docker login ghcr.io` 재실행                             |
| Container exited         | 앱 크래시               | `docker logs <컨테이너명>` 확인                               |
| AWS CLI 명령 실패            | credentials 없음      | EC2에 IAM 역할이 연결되어 있는지 확인                               |
| Deregister 타임아웃          | ALB 설정 문제           | 타겟 그룹의 Deregistration delay 값 확인 (30초 권장)              |

---

## 7. 전체 흐름 요약

```
[개발자]
    │
    ├── git push origin deploy/user
    │
[GitHub Actions — deploy-user.yml]
    ├── checkout
    ├── ./gradlew :apps:user:bootJar
    ├── docker build → ghcr.io/.../user:abc1234
    ├── docker push
    └── ssh ec2-user@EC2-1 "/opt/deploy/deploy.sh user abc1234"
            │
[EC2-1 deploy.sh]
            ├── ALB 타겟 그룹에서 제거 (drain)
            ├── deregistration delay 대기
            ├── docker pull ...user:abc1234
            ├── docker stop user (graceful, 30초)
            ├── docker run → user (8080)
            ├── health check → OK
            └── ALB 타겟 그룹에 재등록 → healthy
            │
[ALB]       └── 클라이언트 → ALB(:443) → EC2-1 Nginx(:80) → App(8080)
```

---

## 8. Graceful Shutdown 설정

Spring Boot의 Graceful Shutdown이 활성화되어 있다.
`docker stop -t 30`이 SIGTERM을 보내면, Spring Boot가 진행 중인 요청을 최대 30초까지 완료한 후 종료한다.

```yaml
# application.yml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

> Deregistration delay(ALB) + Graceful Shutdown(Spring Boot)의 이중 보호로,
> 배포 중 진행 중인 요청이 끊기지 않는다.

---

## 9. 배포 알림

GitHub Actions는 기본적으로 워크플로우 실패 시 이메일을 발송한다.
성공 시에도 이메일을 받으려면 GitHub 설정을 변경한다.

### 설정 방법

GitHub → Settings → Notifications → Actions:

| 설정                      | 동작                |
|-------------------------|-------------------|
| **Only failures** (기본값) | 실패 시에만 이메일 발송     |
| **All workflows**       | 성공 · 실패 모두 이메일 발송 |

> 별도 워크플로우 수정 없이 GitHub 계정 설정만 바꾸면 된다.
> 추후 Slack 알림이 필요하면 워크플로우에 알림 step을 추가한다.
