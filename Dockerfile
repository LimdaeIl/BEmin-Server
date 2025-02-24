FROM eclipse-temurin:17-jdk-alpine

# 필요한 패키지 설치 (tzdata)
RUN apk add --no-cache tzdata

# 시간대 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 애플리케이션 JAR 파일 추가
COPY ./build/libs/*SNAPSHOT.jar project.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "project.jar"]

