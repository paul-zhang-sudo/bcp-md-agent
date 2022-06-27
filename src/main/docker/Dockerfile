FROM openjdk:8-jre-slim
MAINTAINER fish

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

RUN mkdir /db
COPY target/md-agent*.jar app.jar
COPY src/main/resources/libsapjco3.so /app/libsapjco3.so
RUN chmod a+x -R /app/libsapjco3.so
COPY src/main/resources/java.security /usr/local/openjdk-8/lib/security/java.security
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]