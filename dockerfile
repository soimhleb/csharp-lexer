FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY src .
RUN mkdir out && javac -d out $(find . -name "*.java")
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/out /app
COPY examples /app/examples

ENTRYPOINT ["java", "Main"]