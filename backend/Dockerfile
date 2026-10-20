FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B verify
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S crm && adduser -S crm -G crm
WORKDIR /app
COPY --from=build /build/target/bsicrm-*.jar app.jar
USER crm
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","app.jar"]
