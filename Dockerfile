FROM ghcr.io/graalvm/jdk-community:17

WORKDIR /app

COPY target/mandacarubroker-0.0.1-SNAPSHOT.jar /app/mandacarubroker-0.0.1-SNAPSHOT.jar

CMD	["java", "-jar", "mandacarubroker-0.0.1-SNAPSHOT.jar"]