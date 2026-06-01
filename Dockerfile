# Shared multi-stage build. Each service passes its module name via the MODULE arg.
# Stage 1 builds the requested module (and its dependencies, e.g. common) from the
# full reactor; stage 2 runs the resulting executable jar on a slim JRE.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
ARG MODULE
RUN chmod +x mvnw \
    && ./mvnw -q -pl ${MODULE} -am clean package -DskipTests \
    && cp "$(ls ${MODULE}/target/*.jar | grep -v '\.original$' | head -n1)" /workspace/app.jar

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
