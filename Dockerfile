# ---------- Étape 1 : construction du .jar ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copie d'abord le pom et le wrapper pour profiter du cache Docker des dépendances
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Puis le code source, et construction (les tests sont déjà lancés par la CI)
COPY src src
RUN ./mvnw -B clean package -DskipTests

# ---------- Étape 2 : image finale, légère ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
