# Daily Report & Notification System

Spring Boot + MySQL + Thymeleaf 기반 CRUD + 파일 업로드 + 일일 배치 리포트 프로젝트입니다.

## 실행 준비

1. MySQL 데이터베이스 생성
```
CREATE DATABASE daily_report CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. `src/main/resources/application.yml`에서 DB 계정 수정

3. 실행 (Gradle 설치되어 있다면)
```
gradle bootRun
```

Gradle Wrapper를 쓰고 싶다면:
```
gradle wrapper
./gradlew bootRun
```

## 주요 URL

- 대시보드: `http://localhost:8080/dashboard`
- 요청 목록: `http://localhost:8080/requests`
- 알림 목록: `http://localhost:8080/notifications?userEmail=사용자이메일`

## 배치 스케줄

- 매일 02:00: 전날 처리 완료 건수 집계 (Spring Batch Job)
- 매일 09:00: 사용자 알림 생성

## 파일 업로드

- 업로드 파일은 프로젝트 루트의 `uploads/`에 저장됩니다.
