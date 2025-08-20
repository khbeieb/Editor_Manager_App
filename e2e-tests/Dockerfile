# Base image with Java and Playwright
FROM mcr.microsoft.com/playwright/java:v1.53.0-jammy

# Set Maven 3.9.10
RUN rm -rf /usr/share/maven && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.10/binaries/apache-maven-3.9.10-bin.tar.gz && \
    tar -xzf apache-maven-3.9.10-bin.tar.gz -C /opt && \
    mv /opt/apache-maven-3.9.10 /opt/maven && \
    rm apache-maven-3.9.10-bin.tar.gz

# Update PATH
ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH

# Set working directory
WORKDIR /e2e-tests

# Copy project files
COPY pom.xml ./
COPY src ./src

# Pre-download dependencies & compile
RUN mvn dependency:go-offline -B
RUN mvn compile

# Default command just runs tests
CMD ["mvn", "test"]
