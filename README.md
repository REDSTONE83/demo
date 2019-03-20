
개발 프레임워크
======================
## Spring-boot (v2.1.3)
### 활용모듈
+ Spring-boot Web Starter
+ Spring-boot JPA Data
+ Spring-boot Test

* * *
문제해결 전략
====================
## 1.개발환경
- 개발방법 : TDD
- 데이터베이스 : H2DB
- 명명규칙 : camelCase

## 2.과제선정
- 3번, 주택금융API 개발

## 3.이슈해결
#### CSV 파일 업로드 개발
+ Apache-commons CSV 라이브러리 활용 (v1.6)
+ EUC-KR로 인코딩 되어 있어, ByteStream 입력 시 반영
+ 금융기관 명칭에 '(억원)' 글자가 포함되어 있어서 제거
+ 일부 금액 데이터에 쉼표가 같이 포함되어 제거

#### 연도별 기관별 통계자료 조회
+ 3개 API에서 연도별, 기관별 통계자료를 활용하는 것을 확인
+ 동일 서비스로 구현하기 위해, 통계조회용 객체모델 정의

#### 2018년도 지원금액 예측 계산
+ 이전 자료들을 바탕으로 추세선 기반 예측
+ 3차 다항식 추세선 계수를 구하여, 차년도 동월 예측금액 계산
+ Apache-commons Math 라이브러리 활용 (v3.6)
  
* * *
빌드 및 실행
======================
#### 필수설치
+ [JDK 1.8+](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ [Maven 3.2+](https://maven.apache.org/download.cgi)

#### 소스코드 다운로드
+ Github에서 소스코드를 Clone
<pre><code> git clone </code></pre>

#### 테스트방법
+ 소스코드 Root 폴더에서, 커맨드 창으로 아래 명령어 입력
<pre><code> mvn test </code></pre>
  
#### 빌드방법
+ 소스코드 Root 폴더에서, 커맨드 창으로 아래 명령어 입력
<pre><code> mvn compile </code></pre>

#### 실행방법
+ 소스코드 Root 폴더에서, 커맨드 창으로 아래 명령어 입력
<pre><code> mvn spring-boot:run </code></pre>
