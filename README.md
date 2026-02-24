# Описание

## Проект является выполением тестового задания

### Использованые технологии:
- Java 21
- Spring Boot 4.0.2
- Webflux
- Reactive Hibernate (R2Dbc)
- Liquibase

### Тесты:
- Junit
- TestSuite
- TestContainers
- Mockito


# Как запускать

1. Запускаются `mvn test` 
2. Поднимаются контейнеры `docker compose up -d`
3. Запускается сервис `mvn spring-boot:run`
4. Схемы базы данных загрузятся автоматически


# Утилита загрузки n документов
Утилиты лежат в директории tool. Они собраны под разные архитектуры
1. app_amd_windows
2. app_amd_linux 
3. app_arm_darwin (macOS)
4. app_arm_linux

Использование: `app_amd_windows <Путь-к-файлу>`

Файл должен содержать только число – количество документов к созданию.
В терминале будет выведен прогресс работы утилиты или ошибки

# REST-Запросы

В приложении есть swagger-ui, доступен [здесь](http://localhost:8080/swagger-ui/index.html) (http://localhost:8080/swagger-ui/index.html)