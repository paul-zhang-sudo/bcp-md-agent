FROM openjdk:8-jre-slim
MAINTAINER fish

# 安装Kerberos客户端和ping命令
RUN apt-get update && apt-get install -y krb5-user iputils-ping

ENV TZ=Asia/Shanghai
ENV LD_LIBRARY_PATH /usr/local/lib:$LD_LIBRARY_PATH
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app
RUN mkdir /db
COPY target/md-agent*.jar app.jar
COPY src/main/resources/libsapjco3.so /usr/local/lib/libsapjco3.so
RUN chmod a+x -R /usr/local/lib/libsapjco3.so
COPY src/main/resources/java.security /usr/local/openjdk-8/lib/security/java.security
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]