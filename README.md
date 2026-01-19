# Веб чат приложение
Приложение на Spring Boot Thymeleaf, реализующее фунции веб чата.

Авторизация VK является обязательной как распространённая практика
одного из барьеров от ботов.
Для корректной работы авторизации VK требуется обеспечить облачное соединение,
защищённое протоколом https. Самый простой способ это сделать:
```shell
ssh -p 443 -R0:127.0.0.1:8080 qr@free.pinggy.io
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
VK_APP_ID=id VK_REDIRECT_URL="https://host/chat" docker compose up --build
```
Клиент будет доступен на порту **8080**;  
Админка для базы данных на **8081**;
База данных на **5432**.


