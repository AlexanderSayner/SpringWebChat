# Веб чат приложение
## Локальный запуск
Необходимо обеспечить доступ в базе данных PostgreSQL.
Заполнить переменные окружения:
```
DATABASE_PASSWORD=chatuser;
DATABASE_URL=jdbc:postgresql://localhost:5432/chatdb;
DATABASE_USERNAME=chatuser;
VK_APP_ID=VkAppNumberId;
VK_REDIRECT_URL=https://localhost/chat
```
Постфикс */chat* позволит автоматически переводить пользователя на страницу чата

