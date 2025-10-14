FROM maven:3.9.8-eclipse-temurin-17

WORKDIR /e2e-selenium

COPY . .

RUN apt-get update && apt-get install -y curl iputils-ping

# Use the CMD to run tests via the remote Selenium server
CMD ["mvn", "clean", "test", "-Dmaven.test.failure.ignore=true"]
