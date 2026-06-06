# Per-service Dockerfile for independent deploy (one server / one pipeline per service).
#
# IMPORTANT: build with the REPO ROOT as context — the Maven build needs the parent pom,
# the 'common' module and mvnw, which live at the root, not in this folder:
#
#     docker build -f notification-service/Dockerfile -t soundclown-notification-service .
#
# (The root .dockerignore applies. Equivalent to the root Dockerfile with MODULE=notification-service.)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
ARG MODULE=notification-service
RUN chmod +x mvnw \
    && ./mvnw -q -pl ${MODULE} -am clean package -DskipTests \
    && cp "$(ls ${MODULE}/target/*.jar | grep -v '\.original$' | head -n1)" /workspace/app.jar

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
