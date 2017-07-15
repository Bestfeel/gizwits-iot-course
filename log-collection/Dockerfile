FROM daocloud.io/gizwits2015/java8
MAINTAINER  feel  <fye@gizwits.com>
RUN mkdir -p /app
ADD target/log-collection-1.0.jar    /app/app.jar
EXPOSE 8080
VOLUME /app
WORKDIR /app
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]