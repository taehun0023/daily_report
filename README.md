# 고객 문의/지원 문의 리포트 & 알림 시스템

Spring Boot + MySQL + Thymeleaf 기반 **고객 문의/지원 문의** CRUD + 파일 업로드 + 일일 배치 리포트 프로젝트입니다.

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
- 문의 목록: `http://localhost:8080/supports`
- 담당자(사용자) 목록: `http://localhost:8080/users`
- 알림 목록: `http://localhost:8080/notifications?userId=사용자ID`

## 배치 스케줄

- 매일 02:00: 전날 처리 완료 문의 건수 집계 (Spring Batch Job)
- 매일 09:00: 담당자 알림 생성

## 파일 업로드

- 업로드 파일은 프로젝트 루트의 `uploads/`에 저장됩니다.

## 이메일 발송 설정

`src/main/resources/application.yml`에서 아래 항목을 수정하세요.

- `spring.mail.host`, `spring.mail.port`, `spring.mail.username`, `spring.mail.password`
- `app.mail.enabled: true` 로 변경
- `app.mail.from` 발신 주소 설정

### Gmail (SMTP)

- 2단계 인증이 켜져 있다면 앱 비밀번호를 사용하세요.

```
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_gmail@gmail.com
    password: your_app_password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
app:
  mail:
    enabled: true
    from: your_gmail@gmail.com
```

### 네이버 메일 (SMTP)

```
spring:
  mail:
    host: smtp.naver.com
    port: 587
    username: your_id@naver.com
    password: your_password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
app:
  mail:
    enabled: true
    from: your_id@naver.com
```

### 카카오 메일 (Daum) SMTP

```
spring:
  mail:
    host: smtp.daum.net
    port: 465
    username: your_id@daum.net
    password: your_password
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
app:
  mail:
    enabled: true
    from: your_id@daum.net
```

### 회사 메일 (일반 예시)

```
spring:
  mail:
    host: smtp.company.com
    port: 587
    username: your_id@company.com
    password: your_password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
app:
  mail:
    enabled: true
    from: no-reply@company.com
```
