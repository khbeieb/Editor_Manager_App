# Base image with Java
FROM mcr.microsoft.com/playwright/java:v1.53.0-jammy

# Install Maven 3.9.10
RUN rm -rf /usr/share/maven && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.10/binaries/apache-maven-3.9.10-bin.tar.gz && \
    tar -xzf apache-maven-3.9.10-bin.tar.gz -C /opt && \
    mv /opt/apache-maven-3.9.10 /opt/maven && \
    rm apache-maven-3.9.10-bin.tar.gz

# Install Allure
RUN wget https://github.com/allure-framework/allure2/releases/download/2.29.0/allure-2.29.0.tgz && \
    tar -xzf allure-2.29.0.tgz -C /opt && \
    mv /opt/allure-2.29.0 /opt/allure && \
    rm allure-2.29.0.tgz

# Update PATH
ENV MAVEN_HOME=/opt/maven
ENV ALLURE_HOME=/opt/allure
ENV PATH=$MAVEN_HOME/bin:$ALLURE_HOME/bin:$PATH

# Set working directory to api-tests
WORKDIR /api-tests

# Copy project files
COPY pom.xml ./
COPY src ./src

# Pre-download dependencies & compile
RUN mvn dependency:go-offline -B
RUN mvn compile

# Expose port for Allure serve (if needed)
EXPOSE 8080

# Default command just runs API tests
CMD ["mvn", "test"]
