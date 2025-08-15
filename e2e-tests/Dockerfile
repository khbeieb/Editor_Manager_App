FROM mcr.microsoft.com/playwright/java:v1.53.0-jammy

# Remove old Maven and install Maven 3.9.10
RUN rm -rf /usr/share/maven && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.10/binaries/apache-maven-3.9.10-bin.tar.gz && \
    tar -xzf apache-maven-3.9.10-bin.tar.gz -C /opt && \
    mv /opt/apache-maven-3.9.10 /opt/maven && \
    rm apache-maven-3.9.10-bin.tar.gz

# Update environment variables
ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH

WORKDIR /e2e-tests

# Copy Maven project files
COPY pom.xml ./
COPY src ./src

# Download dependencies and build tests
RUN mvn dependency:go-offline -B
RUN mvn compile

CMD ["mvn", "test"]
