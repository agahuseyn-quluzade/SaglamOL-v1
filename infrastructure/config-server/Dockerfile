FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl && addgroup -S app && adduser -S app -G app
COPY build/libs/config-server-0.0.1-SNAPSHOT.jar /app/app.jar
USER app
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
