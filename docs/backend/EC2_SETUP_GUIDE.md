# EC2 서버 세팅 가이드 (Claude 자동화용)

> **이 문서의 목적**: EC2 서버에서 Claude Code가 이 문서를 읽고, 업로드된 파일을 배치하여
> 서버 세팅을 완료할 수 있도록 하는 자동화 지침서이다.
>
> **사용 방법**:
> 1. EC2 인스턴스에 SSH 접속
> 2. Claude Code 설치 (`npm install -g @anthropic-ai/claude-code`) 및 로그인
> 3. 아래 "필요 파일 목록"의 파일들을 서버의 임의 작업 디렉토리(예: `/tmp/setup/`)에 업로드
> 4. Claude에게 "이 문서대로 서버 세팅해줘"라고 요청

---

## 배포 흐름 (전체 구조)

```
개발자: deploy/user 브랜치에 push
         │
         ▼
GitHub Actions (backend-cd.yml)
  ├─ build-and-push job
  │    ├─ Gradle bootJar 빌드
  │    ├─ Docker 이미지 빌드
  │    └─ GHCR(ghcr.io)에 push
  │
  └─ deploy job
       └─ SSH로 EC2 접속 → /opt/deploy/deploy.sh 실행
            │
            ├─ 1. ALB 타겟 해지 (deregister) ─── 새 트래픽 차단
            ├─ 2. Deregistration 대기 ────────── 기존 요청 완료 보장
            ├─ 3. Docker pull (새 이미지)
            ├─ 4. 기존 컨테이너 graceful stop ── 30초 대기
            ├─ 5. 새 컨테이너 start
            ├─ 6. Health check ────────────────── 최대 60초
            └─ 7. ALB 타겟 재등록 (register) ─── 트래픽 수신 재개
```

> 빌드부터 ALB 재등록까지 **전부 GitHub Actions 파이프라인 안에서** 자동 실행된다.
> EC2 서버에서 수동으로 할 일은 없다 (최초 세팅 이후).

---

## 전제 조건

아래 항목은 **AWS 콘솔에서 사전에 완료**되어 있어야 한다:

- [ ] EC2 인스턴스 생성 완료 (Amazon Linux 2023, t3a.small 이상)
- [ ] SSH 접속 가능 (Security Group에 22번 포트 허용)
- [ ] IAM 역할 부여: `elasticloadbalancing:RegisterTargets`, `DeregisterTargets`, `DescribeTargetHealth` 권한
- [ ] ALB 타겟 그룹 생성 완료 (HTTP/80, health check: `/api/health`)
- [ ] ALB 리스너 규칙 설정 완료 (호스트 기반 라우팅)
- [ ] GHCR Personal Access Token 발급 (`read:packages` 권한)

---

## 필요 파일 목록

아래 파일들을 서버의 작업 디렉토리(예: `/tmp/setup/`)에 업로드한다.

| # | 원본 경로 (레포지토리)                                          | 서버 최종 배치 경로                         | 설명               |
|---|--------------------------------------------------------|-------------------------------------|------------------|
| 1 | `docs/backend/deployment/deploy.sh`                    | `/opt/deploy/deploy.sh`             | 범용 배포 스크립트       |
| 2 | `docs/backend/deployment/user/user.env`        | `/opt/deploy/projects/user.env` | 프로젝트 환경설정        |
| 3 | `docs/backend/deployment/user/nginx/user.conf` | `/etc/nginx/conf.d/user.conf`   | Nginx 리버스 프록시 설정 |

> **중요**: `user.env` 파일의 `<플레이스홀더>` 값은 업로드 전에 실제 값으로 변경하거나,
> 서버에서 Claude에게 수정을 요청한다.

---

## Phase 1: 기본 패키지 설치

Amazon Linux 2023 기준이다.

### 1-1. Docker 설치

```bash
sudo dnf update -y
sudo dnf install -y docker
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker ec2-user
```

> `usermod` 적용을 위해 SSH 세션을 재접속하거나 `newgrp docker`를 실행한다.

### 1-2. Nginx 설치

```bash
sudo dnf install -y nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

### 1-3. AWS CLI 확인

Amazon Linux 2023에는 AWS CLI v2가 기본 설치되어 있다. 확인만 한다:

```bash
aws --version
```

### 1-4. jq 설치

```bash
sudo dnf install -y jq
```

---

## Phase 2: 디렉토리 구조 생성

```bash
sudo mkdir -p /opt/deploy/projects
sudo mkdir -p /app/logs
sudo mkdir -p /app/backup

sudo chown -R ec2-user:ec2-user /opt/deploy
sudo chown -R ec2-user:ec2-user /app
```

---

## Phase 3: 파일 배치

작업 디렉토리에 업로드된 파일을 최종 경로로 복사한다.
아래 명령에서 `/tmp/setup/`은 파일을 업로드한 디렉토리로 대체한다.

```bash
# 배포 스크립트
cp /tmp/setup/deploy.sh /opt/deploy/deploy.sh
chmod +x /opt/deploy/deploy.sh

# 프로젝트 환경설정
cp /tmp/setup/user.env /opt/deploy/projects/user.env

# Nginx 설정
sudo cp /tmp/setup/user.conf /etc/nginx/conf.d/user.conf
```

---

## Phase 4: 환경변수 설정

`/opt/deploy/projects/user.env` 파일에서 아래 플레이스홀더를 실제 값으로 변경한다.

### 배포 설정 (필수)

| 변수                 | 플레이스홀더                     | 설명                          |
|--------------------|----------------------------|-----------------------------|
| `TARGET_GROUP_ARN` | `ACCOUNT_ID`, `XXXXXXXXXX` | ALB 타겟 그룹 ARN (AWS 콘솔에서 확인) |

### 앱 환경변수 (DOCKER_ENV_* 섹션)

`DOCKER_ENV_` 접두사가 붙은 변수는 `deploy.sh`가 접두사를 제거한 뒤 컨테이너에 `-e` 옵션으로 자동 주입한다.
주입된 환경변수는 `application-prod.yml`의 `${...}` 플레이스홀더에 바인딩된다.

```
.env:                    DOCKER_ENV_DB_URL=jdbc:postgresql://...
  ↓ deploy.sh 접두사 제거
docker run:              -e DB_URL=jdbc:postgresql://...
  ↓ Spring Boot 자동 바인딩
application-prod.yml:    url: ${DB_URL}
```

| 변수                               | 플레이스홀더                        | 대응하는 yml 속성                  |
|----------------------------------|-------------------------------|------------------------------|
| `DOCKER_ENV_DB_URL`              | `<RDS_HOST>`, `<DB_NAME>`     | `spring.datasource.url`      |
| `DOCKER_ENV_DB_USERNAME`         | `<DB_USER>`                   | `spring.datasource.username` |
| `DOCKER_ENV_DB_PASSWORD`         | `<DB_PASSWORD>`               | `spring.datasource.password` |
| `DOCKER_ENV_JWT_SECRET`          | `<JWT_SECRET_KEY_MIN_256BIT>` | `app.jwt.secret`             |
| `DOCKER_ENV_S3_BUCKET`           | `<S3_BUCKET_NAME>`            | `s3.bucket`                  |
| `DOCKER_ENV_AWS_ACCESS_KEY`      | `<AWS_ACCESS_KEY_ID>`         | `aws.access-key`             |
| `DOCKER_ENV_AWS_SECRET_KEY`      | `<AWS_SECRET_ACCESS_KEY>`     | `aws.secret-key`             |
| `DOCKER_ENV_GOOGLE_CLIENT_ID`    | `<GOOGLE_CLIENT_ID>`          | `social.google.clientId`     |
| `DOCKER_ENV_GOOGLE_SECRET_KEY`   | `<GOOGLE_SECRET_KEY>`         | `social.google.secretKey`    |
| `DOCKER_ENV_GOOGLE_REDIRECT_URI` | `<API_DOMAIN>`                | `social.google.redirectUri`  |

---

## Phase 5: GHCR Docker 로그인

GitHub Container Registry에 로그인하여 Docker 이미지를 pull할 수 있도록 한다.

```bash
echo "<GHCR_PAT>" | docker login ghcr.io -u <GITHUB_USERNAME> --password-stdin
```

- `<GHCR_PAT>`: GitHub Personal Access Token (`read:packages` 권한)
- `<GITHUB_USERNAME>`: GitHub 계정명

> 로그인 정보는 `~/.docker/config.json`에 저장되며 재부팅 후에도 유지된다.

---

## Phase 6: Nginx 설정 적용

### 6-1. 도메인 수정

`/etc/nginx/conf.d/user.conf` 파일에서 `server_name`을 실제 도메인으로 변경한다:

```nginx
server_name api.example.com;  # ← 실제 도메인으로 변경
```

### 6-2. 기본 설정 비활성화

Nginx 기본 welcome 페이지가 충돌하지 않도록 제거한다:

```bash
sudo rm -f /etc/nginx/conf.d/default.conf
```

### 6-3. 검증 & 적용

```bash
sudo nginx -t
sudo systemctl reload nginx
```

---

## Phase 7: 첫 배포 & 검증

### 7-1. 수동 배포 실행

```bash
/opt/deploy/deploy.sh user latest
```

> 최초 실행 시 ALB 타겟 제거(deregister) 단계에서 "타겟이 없다"는 경고가 나올 수 있다. 정상이다.

### 7-2. 검증

```bash
# 컨테이너 실행 확인
docker ps --filter "name=user"

# Health check
curl -s http://localhost:8080/api/health

# 앱 로그 확인
tail -20 /app/logs/app.log

# ALB 타겟 상태 확인
aws elbv2 describe-target-health \
    --target-group-arn "$(grep TARGET_GROUP_ARN /opt/deploy/projects/user.env | cut -d= -f2)"
```

---

## Phase 8: 로그 관리 (cron)

일별 로그 백업을 위한 cron을 등록한다.

```bash
cat <<'CRON' | crontab -
# 매일 02:00 — 7일 이상된 백업 로그 삭제
0 2 * * * find /app/backup -name "*.log" -mtime +7 -delete 2>/dev/null
CRON
```

> 로그 롤링은 Spring Boot의 Logback이 자동으로 처리한다 (`.env`의 `LOG_*` 설정 참조).
> cron은 `/app/backup` 디렉토리의 오래된 파일만 정리하는 용도이다.

---

## 세팅 완료 확인 체크리스트

- [ ] `docker ps` — user 컨테이너 실행 중
- [ ] `curl localhost:8080/api/health` — 200 OK
- [ ] ALB 타겟 상태 — healthy
- [ ] `/app/logs/app.log` — 에러 없이 정상 기동 로그
- [ ] `nginx -t` — syntax ok

---

## GitHub Actions Secrets 점검

EC2 세팅을 완료했으면, **GitHub Actions에서 자동 배포가 동작하려면** 아래 Secrets가 등록되어 있어야 한다.
GitHub 레포 → Settings → Secrets and variables → Actions에서 확인한다.

### 공통 (필수)

| Secret 이름         | 값                            | 등록 여부 |
|-------------------|------------------------------|-------|
| `SERVER_USER`     | EC2 SSH 사용자명 (보통 `ec2-user`) | [ ]   |
| `SSH_PRIVATE_KEY` | EC2 접속용 `.pem` 파일 전체 내용      | [ ]   |

### user 배포 (`deploy/user` 브랜치)

| Secret 이름              | 값                               | 등록 여부 |
|------------------------|---------------------------------|-------|
| `USER_API_SERVER_HOST` | user EC2의 퍼블릭 IP 또는 프라이빗 IP | [ ]   |

### admin 배포 (`deploy/admin` 브랜치)

| Secret 이름               | 값                                | 등록 여부 |
|-------------------------|----------------------------------|-------|
| `ADMIN_API_SERVER_HOST` | admin EC2의 퍼블릭 IP 또는 프라이빗 IP | [ ]   |

### 스테이지 환경 (향후 `stage/*` 브랜치 사용 시)

| Secret 이름                     | 값                     | 등록 여부 |
|-------------------------------|-----------------------|-------|
| `STAGE_USER_API_SERVER_HOST`  | 스테이지 user EC2 IP  | [ ]   |
| `STAGE_ADMIN_API_SERVER_HOST` | 스테이지 admin EC2 IP | [ ]   |

> `GITHUB_TOKEN`은 GitHub Actions가 자동 제공하므로 별도 등록이 불필요하다.
> GHCR Docker 이미지 push/pull에 사용된다.

---

---

# AMI에서 admin 전환 가이드

user 세팅이 완료된 EC2 인스턴스에서 **AMI 이미지를 생성**한 뒤,
새 EC2 인스턴스를 만들어 admin로 전환하는 방법이다.

## 전제 조건

- user가 세팅 완료된 EC2의 AMI 이미지에서 새 인스턴스를 생성했다
- admin용 ALB 타겟 그룹이 별도로 생성되어 있다

## 변경할 파일 (2개)

### 1. `/opt/deploy/projects/admin.env` 생성

기존 `user.env`를 복사하고 아래 값만 변경한다:

```bash
cp /opt/deploy/projects/user.env /opt/deploy/projects/admin.env
```

**변경 항목**:

| 변수                 | user 값                              | admin 값                              |
|--------------------|-----------------------------------------|------------------------------------------|
| `PROJECT`          | `user`                              | `admin`                              |
| `IMAGE`            | `ghcr.io/namgyunkim/mono-repo/user` | `ghcr.io/namgyunkim/mono-repo/admin` |
| `TARGET_GROUP_ARN` | user 타겟 그룹 ARN                      | **admin 타겟 그룹 ARN**                  |

> 나머지 값(DB, JWT, S3, 로깅 등)은 **동일한 인프라를 공유한다면 그대로** 사용한다.
> 별도 DB나 설정이 필요하면 해당 `DOCKER_ENV_*` 값도 변경한다.

### 2. `/etc/nginx/conf.d/` Nginx 설정 교체

```bash
# user 설정 제거
sudo rm /etc/nginx/conf.d/user.conf

# admin 설정 추가 (업로드한 파일 또는 user.conf 복사 후 수정)
sudo cp /tmp/setup/admin.conf /etc/nginx/conf.d/admin.conf
```

`admin.conf`에서 변경할 항목:

| 항목            | user          | admin           |
|---------------|-------------------|---------------------|
| `server_name` | `api.example.com` | `admin.example.com` |

변경 후 적용:

```bash
sudo nginx -t && sudo systemctl reload nginx
```

## 변경하지 않아도 되는 것

| 항목                       | 이유                 |
|--------------------------|--------------------|
| `/opt/deploy/deploy.sh`  | 프로젝트명을 인자로 받으므로 공용 |
| Docker / Nginx / AWS CLI | AMI에 이미 설치됨        |
| 디렉토리 구조                  | AMI에 이미 생성됨        |
| GHCR 로그인                 | AMI에 인증 정보 포함      |
| cron 설정                  | AMI에 이미 등록됨        |

## 첫 배포

```bash
/opt/deploy/deploy.sh admin latest
```

## 검증

```bash
docker ps --filter "name=admin"
curl -s http://localhost:8080/api/health
```
