FROM maven:3.9.9-eclipse-temurin-17-focal AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
ARG PORT=8080
ENV PORT=${PORT}

RUN printf '#!/bin/sh\n\
echo "$GOOGLE_CREDENTIALS_JSON" > /tmp/key.json\n\
export GOOGLE_APPLICATION_CREDENTIALS=/tmp/key.json\n\
exec java -jar /app/app.jar --server.port=$PORT\n' > /entrypoint.sh \
 && chmod +x /entrypoint.sh

COPY --from=build /app/target/feelink_backend-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
CMD ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT}"]
