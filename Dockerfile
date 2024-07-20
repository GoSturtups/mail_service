# Укажите базовый образ с Zulu JDK 20
FROM azul/zulu-openjdk:21
COPY . .
RUN ./gradlew shadowJar
COPY server/build/libs/server-all.jar .
COPY .env .
CMD ["java", "-jar", "server-all.jar"]


#CMD ["./server/build/install/server/bin/mail-server"]
#
#RUN mkdir /app
#WORKDIR /app
#COPY app.jar /app/
#CMD ["java", "-jar", "/app/app.jar"]
#EXPOSE 8080