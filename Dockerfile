# Базовый слой для сборки
FROM gradle:8.4.0-jdk17 AS build

WORKDIR /app

# Сначала копируем только gradle файлы и зависимости
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Ставим только зависимости (будет кэшироваться)
RUN gradle build -x test -x startScripts --no-daemon || true

# Потом копируем остальной код проекта
COPY . .

# И теперь уже реально билдим проект
RUN gradle build -x test -x startScripts --no-daemon


# Новый слой для минимального runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
