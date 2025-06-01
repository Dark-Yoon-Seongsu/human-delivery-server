# 1. Base image
FROM eclipse-temurin:21-jdk

# 2. JAR 파일을 컨테이너로 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 3. 포트 노출 (필요 시)
EXPOSE 8080

# 4. 실행 명령
ENTRYPOINT ["java", "-jar", "/app.jar"]
