FROM ubuntu:latest
COPY . repo/
WORKDIR repo
RUN apt-get update; apt-get -y install maven; apt-get -y install openjdk-8-jdk
RUN mvn -DskipTests package
WORKDIR /usr/src/app
RUN cp /repo/target/*.jar ./app.jar


ENTRYPOINT ["java","-Djava.security.egd=file:/dev/urandom","-jar","./app.jar", "--port=80"]
