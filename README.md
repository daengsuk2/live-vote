# YNC Live Class

실시간 수업 Q&A · 투표 시스템. 새로고침 없이 반 전체의 질문과 투표가 화면에 바로 올라옵니다.
Spring Boot · WebSocket(STOMP) · PostgreSQL · SB Admin 2

## 1. 실행 방법

필요한 것: JDK 21, Docker.

```bash
# 1) 환경 파일 (비밀번호를 바꾸세요. .env 는 커밋하지 않습니다)
cp .env.example .env

# 2) DB (PostgreSQL 16, 처음 한 번 docs/schema.sql 이 자동으로 적용됩니다)
docker compose up -d

# 3) 서버
./gradlew bootRun
```

- 강사 화면: http://localhost:8080 (로그인 계정은 `.env` 의 `INSTRUCTOR_EMAIL`, `INSTRUCTOR_PASSWORD`)
- 학생 휴대폰 접속: 강사 PC와 **같은 Wi-Fi**에서 `http://<강사 PC의 IP>:8080/join` (예: `http://192.168.0.15:8080/join`)
  - IP 확인: Windows `ipconfig`
  - 방화벽에서 **8080 포트 허용**이 필요합니다.
- DB는 호스트 포트 5433 입니다(다른 프로젝트의 5432와 겹치지 않게).
- DB를 처음부터 다시 만들려면: `docker compose down -v` 후 `docker compose up -d`

## 2. 토픽 · 이벤트 형식

TODO (`docs/context.md` 의 표를 구현 후 정리)

## 3. 동시성 테스트 결과

TODO (틀린 방식 vs 고친 방식 숫자, `tools/load-test.mjs`)

## 4. 익명 운영 원칙 8개 체크와 알려진 한계

TODO

## 5. 상호 테스트에서 찾은 버그 + AI가 틀려서 고친 사례

TODO

## 6. 화면 캡처 (강사 · 학생 · 리포트)

TODO

## 요구사항 체크리스트

- [ ] L1 세션 열기, L2 학생 참여, L3 실시간 연결, L4 질문 등록, L5 공감, L6 답변 완료·숨김, L7 새로고침 복구
- [ ] P1 투표 만들기, P2 시작·마감, P3 응답·결과, P4 세션 종료, S1 도배 방지, R1 수업 리포트
- [x] 프로젝트 뼈대, DB 스키마(7테이블 + users), STOMP 설정, 공통 에러 응답, 강사 계정 시드

DB 제약(B3~B6)은 `docs/schema.sql` 에 있고 직접 시험했습니다: 같은 공감 두 번(B4), 201자 질문(B3), 한 세션의 두 번째 OPEN 투표(B6), 다른 투표의 선택지(B5), 같은 참여자의 두 번째 응답(B5)은 모두 DB가 거부합니다.
