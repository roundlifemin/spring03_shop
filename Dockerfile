# 1️ JDK 기반 이미지 선택
FROM openjdk:17-jdk-slim

# 2️ 애플리케이션 JAR 파일 복사
COPY build/libs/spring03_shop-0.0.1-SNAPSHOT.jar app.jar


# 3 컨테이너가 노출할 포트 지정
EXPOSE 8090

# ️4 컨테이너 실행 명령
ENTRYPOINT ["java", "-jar", "/app.jar"]
