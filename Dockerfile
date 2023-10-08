# syntax=docker/dockerfile:1.2

from amazoncorretto:21 as build-stage

WORKDIR /app

# copy build files
COPY mvnw .
COPY pom.xml .
COPY .mvn .mvn
COPY src src
# cache dependencies and create jar file
RUN --mount=type=cache,target=/root/.m2 ./mvnw install -DskipTests
# extract files from jar file
RUN java -Djarmode=layertools -jar target/*.jar extract

# move to release stage
from amazoncorretto:21-alpine as release-stage

WORKDIR /app
# copy files from build-stage
COPY --from=build-stage /app/dependencies .
COPY --from=build-stage /app/snapshot-dependencies .
COPY --from=build-stage /app/spring-boot-loader .
COPY --from=build-stage /app/application .

EXPOSE 8080 
CMD ["java", "org.springframework.boot.loader.JarLauncher"]
