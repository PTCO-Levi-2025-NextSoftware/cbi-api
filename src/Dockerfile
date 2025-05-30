FROM openjdk:17

WORKDIR /app

COPY ./src ./src
COPY ./complesso.json ./complesso.json
COPY ./transazioni.json ./transazioni.json

RUN javac -d . src/*.java

EXPOSE 8080

CMD ["java", "Main"]
