FROM adoptopenjdk:11-jre-hotspot

# switch to root user
USER root

# Metadata
LABEL module.name="it-eucert-gateway-client" \
      module.version="0.0.1-SNAPSHOT"

WORKDIR /
ADD . /it-eucert-gateway-client
WORKDIR /it-eucert-gateway-client


COPY [ "target/it-eucert-gateway-client-0.0.1-SNAPSHOT.jar", "/it-eucert-gateway-client/app.jar" ]

ENV JAVA_OPTS="$JAVA_OPTS -Xms256M -Xmx1G"

EXPOSE 8080

RUN useradd \
        --no-log-init \
        --home /it-eucert-gateway-client \
        --shell /bin/bash \
        eucert \
    && chown --recursive immuni:root /it-eucert-gateway-client \   
    && chmod -R g+rwx /it-eucert-gateway-client
USER immuni


ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /it-eucert-gateway-client/app.jar --spring.config.location=file:/it-eucert-gateway-client/config/application.properties" ]
