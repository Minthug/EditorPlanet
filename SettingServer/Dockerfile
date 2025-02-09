# 빌드 스테이지
FROM amazoncorretto:17 AS build
WORKDIR /app

# 의존성 파일만 먼저 복사하고 다운로드
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN chmod +x ./gradlew
RUN ./gradlew dependencies

# 소스 복사 및 빌드
COPY src ./src
RUN ./gradlew clean build

# 실행 스테이지
FROM amazoncorretto:17
WORKDIR /app
COPY --from=build /app/build/libs/SettingServer-0.0.1-SNAPSHOT.jar ./app.jar
EXPOSE 8000
CMD ["java", "-jar", "app.jar"]