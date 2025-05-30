FROM openjdk:17

WORKDIR /app

# Copia tutto (java + json)
COPY . .

# Compila tutti i .java nella root
RUN javac -d . *.java

EXPOSE 8080

CMD ["java", "Main"]
