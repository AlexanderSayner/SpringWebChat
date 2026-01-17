# Серверное веб чат приложение
## Описание

В данном проекте реализовано веб чат приложение для обмена сообщениями между пользователями.

Архитектура системы построена на разделении beck end и front end частей.
Такой подход был выбран в силу более лёгкой поддержи системы и явного
разделения ответственности логики пользовательских интерфейсов и обработки данных.

*web-chat-backend* содержит back end часть приложения на Spring.
*web-chat-frontend* содержит front end часть приложения на Svelte (Vite).

## Ключевые используемые технологии
- Java 25
- Spring Boot 4 (Gradle)
- Docker

Для backend/frontend взаимодействия был выбран **GraphQL** 
для демонстрации возможностей применения технологии.

## Разработка и развёртка приложения
### Запуск back end в IDE

Для запуска проекта *web-chat-backend* в IDE необходимо
обеспечить доступ к бд *Postgres* и указать переменные окружения:
```
CHAT_APP_DB_URL=jdbc:postgresql://localhost:5432/db;
CHAT_APP_DB_PWD=postgres;
CHAT_APP_DB_USER=postgres;
VK_CLIENT_ID=числовое значение из личного кабинета;
VK_CLIENT_SECRET=строка сгенерированная для vk приложения 
```

### Запуск frond end

Для запуска frontend части, создайте файл `.env` в корне папки `web-chat-frontend` со следующими переменными:
```
VITE_BACKEND_WS_URL=ws://localhost:8180/graphql
VITE_VK_APP_ID=ваш VK App ID
```

### Запуск в Docker
