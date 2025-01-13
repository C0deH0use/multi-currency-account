FROM gradle:8.4.0-jdk21 AS build-stage

WORKDIR /home/gradle
COPY . .

RUN gradle clean build --no-daemon

FROM azul/zulu-openjdk:21

COPY --from=build-stage /home/gradle/build/libs/multi-currency-account-0.0.1-*.jar /multi-currency-account.jar


ENTRYPOINT ["java","-jar", "/multi-currency-account.jar" ]
HEALTHCHECK CMD curl --fail http://localhost:8080/actuator/health || exit