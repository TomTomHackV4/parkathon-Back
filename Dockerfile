FROM java:8
EXPOSE 8080
COPY build/libs/*.jar parkathon.jar
ENTRYPOINT java -Dmongopassword=$MONGO_PASSWORD -jar parkathon.jar