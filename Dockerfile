FROM java:8
EXPOSE 8080
COPY build/libs/parkathon-0.0.1-SNAPSHOT.jar parkathon.jar
ENTRYPOINT ["java","-jar","parkathon.jar"]