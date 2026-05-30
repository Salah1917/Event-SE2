FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY discovery-service/pom.xml discovery-service/pom.xml
COPY event-service/pom.xml event-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY registration-service/pom.xml registration-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY ui-service/pom.xml ui-service/pom.xml
COPY event-service/src ./event-service/src
RUN mvn -pl event-service dependency:go-offline -B
RUN mvn -pl event-service package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget
WORKDIR /app
COPY --from=build /app/event-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
