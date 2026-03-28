FROM ghcr.io/graalvm/jdk-community:25

WORKDIR /app

COPY output/services/queue.jar queue.jar

ENTRYPOINT [ "java", "-jar", "queue.jar" ]
