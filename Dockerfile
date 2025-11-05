# JRE만으로 충분합니다 (이미 빌드된 JAR 실행)
FROM eclipse-temurin:21-jre

# 선택: 비루트 실행/타임존/JVM 메모리 튜닝
WORKDIR /app
ENV TZ=Asia/Seoul
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0"

# JAR 파일 경로가 바뀌어도 편하게 ARG로 받기
ARG JAR_FILE=springdeveloper-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app/app.jar

# Spring Boot 기본 포트는 8080 입니다. 8081로 쓸 거면 server.port도 8081로 맞추세요.
EXPOSE 8081

# 실행
ENTRYPOINT ["java","-jar","/app/app.jar"]
