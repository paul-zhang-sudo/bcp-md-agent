FROM openjdk:8-jre-slim
MAINTAINER fish

ENV TZ=Asia/Shanghai
ENV LD_LIBRARY_PATH /usr/local/lib:$LD_LIBRARY_PATH
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone