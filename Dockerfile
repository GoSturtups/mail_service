# Укажите базовый образ с Zulu JDK 20
FROM azul/zulu-openjdk:20

# Укажите рабочую директорию внутри контейнера
WORKDIR /app

# Скопируйте файл .env в контейнер
COPY .env .

# Скопируйте весь проект в рабочую директорию контейнера
COPY . .

# Выполните команду gradle для сборки и установки проекта
RUN ./gradlew installDist

# Определите команду по умолчанию для запуска сервера
CMD ["./server/build/install/server/bin/mail-server"]
