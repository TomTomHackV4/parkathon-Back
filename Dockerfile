FROM java:8
EXPOSE 8080
COPY build/libs/*.jar parkathon.jar
ENV MONGO_PASSWORD ""
ENTRYPOINT ["java","-jar","parkathon.jar","-Dmongopassword=$MONGO_PASSWORD"]