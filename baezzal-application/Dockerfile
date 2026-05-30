FROM eclipse-temurin:21-jdk

RUN apt-get update && \
    apt-get install -y libvips-tools && \
    rm -rf /var/lib/apt/lists/*

COPY build/libs/app.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]