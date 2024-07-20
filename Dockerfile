FROM azul/zulu-openjdk:21
RUN mkdir /app
WORKDIR /app
COPY ./server/build/libs/server-all.jar /app/
CMD ["java", "-jar", "/app/server-all.jar"]
