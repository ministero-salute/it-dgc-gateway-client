FROM adoptopenjdk:11-jre-hotspot

# switch to root user
USER root

# Metadata
LABEL module.name="it-dgc-gateway-client" \
      module.version="0.0.1-SNAPSHOT"

WORKDIR /
ADD . /it-dgc-gateway-client
WORKDIR /it-dgc-gateway-client


COPY [ "target/it-dgc-gateway-client-0.0.1-SNAPSHOT.jar", "/it-dgc-gateway-client/app.jar" ]

ENV JAVA_OPTS="$JAVA_OPTS -Xms256M -Xmx1G"

EXPOSE 8080

RUN useradd \
        --no-log-init \
        --home /it-dgc-gateway-client \
        --shell /bin/bash \
        dgc \
    && chown --recursive immuni:root /it-dgc-gateway-client \   
    && chmod -R g+rwx /it-dgc-gateway-client
USER immuni


ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /it-dgc-gateway-client/app.jar --spring.config.location=file:/it-dgc-gateway-client/config/application.properties" ]
