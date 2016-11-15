FROM java:8-alpine
MAINTAINER leafclick s.r.o. <info@leafclick.com>

ADD target/uberjar/pripojme.jar /pripojme/app.jar

EXPOSE 3000

WORKDIR /pripojme
CMD ["java", "-jar", "/pripojme/app.jar"]
