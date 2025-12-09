FROM openjdk:17.0.2-slim

RUN groupadd --system --gid 999 app && \
    useradd --system --create-home --home-dir /opt/app --shell /usr/sbin/nologin --gid app --uid 999 app

WORKDIR /opt/app

EXPOSE 8080/tcp

USER app

ADD --chown=999:999 ./target/flowboard-*.jar backend.jar

CMD java -Xmx${MEM:-512m} -XX:OnOutOfMemoryError="kill -9 %p" -DCONF_DIR=/opt/app/conf/ -Dlogging.config=/opt/app/conf/logback.xml -jar /opt/app/backend.jar --spring.config.additional-location=/opt/app/conf/
