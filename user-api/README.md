# SNS 연결고리 프로젝트 사용자 API 서버

<br>

## 프로젝트 설명

- 온라인 상에서 다른 사람들과 소통이 가능한 소셜 미디어 서비스(SNS)
  <br><br>

## API 서버 설명
- 연결고리 프로젝트의 사용자 REST API 서버

## Skills and Environment

- Gradle 8.2.1
- IntelliJ IDEA
- Java 11
- Spring Boot 2.7.14
- MySQL 8.1.0
- Swagger 3.0.0
- JWT 0.11.5
- Feign 3.1.1
- Apache 4.4
  <br><br>

- H2 2.1.214
- JUnit 5.8.1
  <br><br>

- Jenkins 2.414.1
- Docker 24.0.6
  <br><br>

## Deployed Server URI(변경 예정)
### [배포 서버](yeongyulgori.myduckdns.org:610)

## Swagger URI(변경 예정)
### [Swagger](yeongyulgori.duckdns.org:610/swagger-ui/index.html)

## Server Endpoints

### Domain
- **/users/v1**

### Resources

#### 회원

- **회원 가입: /sign-up** (POST)
- **로그인: /sign-in** (POST)
- **회원 정보 조회: {username}/details** (GET) 
- **주요 회원 정보 수정: /{username}/auth** (PATCH)
- **회원 정보 수정: /{username}** (PATCH)
- **비밀번호 재설정 요청: /password-reset/request** (POST)
- **비밀번호 재설정: /password-reset** (PATCH)
- **회원 탈퇴: /{username}** (DELETE)
  <br><br>

#### 공통

- **성명 키워드로 회원 검색 시 자동완성: /auto-complete** (GET)
- **성명 키워드로 회원 목록 조회: /** (GET)
- **회원 프로필 조회: /{username}** (GET)
  <br><br>

## Architecture
![User-ERD](https://github.com/hellmir/yeongyulgori/assets/128391669/4a2848a2-4fc1-46af-8224-58f3dbb31d1b)

## ERD
![User-API](https://github.com/hellmir/yeongyulgori/assets/128391669/c3f7d323-413c-45a3-8f93-ca97fc6dbe6a)
