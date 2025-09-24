FROM docker.m.daocloud.io/library/eclipse-temurin:8-jdk

WORKDIR /build
COPY pom.xml /build
COPY ./src /build/src
# RUN wget https://archive.apache.org/dist/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz -O apache-maven-3.8.1-bin.tar.gz
COPY ./build/apache-maven-3.8.1-bin.tar.gz /build/apache-maven-3.8.1-bin.tar.gz
RUN tar -zxvf apache-maven-3.8.1-bin.tar.gz
ENV M2_HOME /build/apache-maven-3.8.1
ENV PATH $M2_HOME/bin:$PATH
RUN mvn -v
RUN --mount=type=secret,id=mvnsettings,target=/build/apache-maven-3.8.1/conf/settings.xml \
    mvn clean package -Dmaven.test.skip=true -U
# RUN mvn package spring-boot:repackage -Dmaven.test.skip=true
RUN ls -lh
RUN ls -lh target

WORKDIR /work
COPY run.sh /work/run.sh
RUN cp /build/target/LlmSecDefenseApi.jar /work/LlmSecDefenseApi.jar
RUN rm -rf /build
RUN chmod +x /work/run.sh

ENTRYPOINT ["/work/run.sh"]
CMD java $JAVA_OPTS -jar /work/LlmSecDefenseApi.jar \
--spring.config.location=$API_APP_CONF_FILE \
--logging.config=file:$API_LOG_CONF_FILE
