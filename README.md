# Веб чат приложение
Приложение на Spring Boot Thymeleaf, реализующее фунции веб чата.
Для корректной работы авторизации VK требуется обеспечить облачное соединение,
защищённое протоколом https. Самый простой способ это сделать:
```shell

```
## Локальный запуск
Необходимо обеспечить доступ в базе данных PostgreSQL и создать приложение в VK ID.
Заполнить переменные окружения:
```
DATABASE_PASSWORD=chatuser;
DATABASE_URL=jdbc:postgresql://localhost:5432/chatdb;
DATABASE_USERNAME=chatuser;
VK_APP_ID=VkAppNumberId;
VK_REDIRECT_URL=https://localhost/chat
```
Постфикс */chat* позволит автоматически переводить пользователя на страницу чата

## Запуск в Docker
```shell
VK_CLIENT_ID=id VK_REDIRECT_URI="https://host/chat" docker compose up --build
```
