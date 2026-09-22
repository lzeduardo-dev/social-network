# ---- build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# cache das dependências
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --no-create-home spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
