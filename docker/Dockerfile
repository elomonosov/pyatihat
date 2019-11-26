FROM openjdk:13-ea-jdk-alpine3.9
#CMD ["/bin/sh"]
COPY target/lateness-0.0.1-SNAPSHOT.jar /opt/lateness.jar
EXPOSE 4747
ENTRYPOINT ["sh", "-c", "java -jar /opt/lateness.jar"]