# OTP codes

Backend-сервис для генерации, доставки и проверки одноразовых OTP-кодов.

HTTP API работает на `com.sun.net.httpserver.HttpServer`, доступ к PostgreSQL выполнен через JDBC и `PreparedStatement`, бизнес-логика вынесена в сервисы, работа с БД - в DAO.

## Технологии

- Java 17
- Maven
- PostgreSQL 17
- JDBC
- Jackson Databind
- JJWT
- Jakarta Mail / Angus Mail
- JSMPP
- Telegram Bot API через стандартный `java.net.http.HttpClient`
- `java.util.logging`
- `ScheduledExecutorService`

## Структура

- `config` - загрузка настроек приложения, БД и каналов доставки.
- `db` - подключение к БД и запуск SQL-схемы.
- `model` - модели и enum-типы.
- `dao` - JDBC-запросы к PostgreSQL.
- `service` - основная бизнес-логика.
- `notification` - отправка OTP через email, SMS, Telegram и файл.
- `http` и `http.handler` - HTTP-утилиты, роутинг и обработчики.
- `security` - текущий пользователь и проверка JWT/ролей.
- `scheduler` - периодическая пометка просроченных OTP.

## База данных

Создайте базу PostgreSQL любым удобным образом, например, с помощью docker-контейнера:

```bash
docker run --name postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=otp_codes -p 5432:5432 -d postgres:17
```

Настройки подключения можно посмотреть в `src/main/resources/application.properties`:

```properties
db.url=jdbc:postgresql://localhost:5432/otp_codes
db.username=postgres
db.password=postgres
server.port=8080
jwt.secret=change-this-secret-key-to-at-least-32-characters
jwt.expires.seconds=3600
otp.expiration.check.seconds=60
otp.file.path=otp-codes.txt
```

При старте приложение выполняет `src/main/resources/db/schema.sql` и создает таблицы:

- `app_users`
- `otp_settings`
- `otp_codes`

В `otp_settings` всегда используется запись с `id = 1`; это ограничено check constraint.

## Каналы доставки

Файлы настроек лежат в `src/main/resources`:

- `email.properties`
- `sms.properties`
- `telegram.properties`

По умолчанию реальные внешние отправки выключены:

```properties
email.enabled=false
sms.enabled=false
telegram.enabled=false
```

Если канал выключен, сервис пишет код в лог. Канал `FILE` всегда доступен и сохраняет код в файл из настройки `otp.file.path`.

## Сборка и запуск

```bash
mvn clean package
java -jar target/otp-codes-1.0.0.jar
```

После запуска API доступно на `http://localhost:8080`.

## API

### Регистрация

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "login": "admin",
  "password": "qwerty123",
  "role": "ADMIN"
}
```

Роли: `ADMIN`, `USER`. Если администратор уже существует, второй администратор не регистрируется.

### Логин

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "login": "admin",
  "password": "qwerty123"
}
```

Ответ содержит JWT:

```json
{
  "token": "...",
  "expiresInSeconds": 3600
}
```

Для защищенных запросов передавайте заголовок:

```http
Authorization: Bearer <token>
```

### Изменить настройки OTP

Только `ADMIN`.

```http
PUT /api/admin/otp-config
Authorization: Bearer <admin-token>
Content-Type: application/json
```

```json
{
  "codeLength": 6,
  "lifetimeSeconds": 300
}
```

### Получить пользователей

Только `ADMIN`. Возвращает всех пользователей, кроме администраторов.

```http
GET /api/admin/users
Authorization: Bearer <admin-token>
```

### Удалить пользователя

Только `ADMIN`. OTP-коды пользователя удаляются каскадно через внешний ключ.

```http
DELETE /api/admin/users/2
Authorization: Bearer <admin-token>
```

### Сгенерировать OTP

Только `USER`.

```http
POST /api/otp/generate
Authorization: Bearer <user-token>
Content-Type: application/json
```

```json
{
  "operationId": "payment-100",
  "channel": "FILE",
  "destination": "payment-100"
}
```

Доступные каналы:

- `EMAIL`
- `SMS`
- `TELEGRAM`
- `FILE`

### Проверить OTP

Только `USER`.

```http
POST /api/otp/validate
Authorization: Bearer <user-token>
Content-Type: application/json
```

```json
{
  "operationId": "payment-100",
  "code": "qwerty123"
}
```

Если код корректен, его статус меняется на `USED`. Просроченные активные коды периодически переводятся в `EXPIRED`.

## Быстрая проверка через curl

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"login":"admin","password":"qwerty123","role":"ADMIN"}'

curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"login":"user","password":"qwerty123","role":"USER"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"user","password":"qwerty123"}'
```

Скопируйте токен пользователя и выполните генерацию:

```bash
curl -X POST http://localhost:8080/api/otp/generate \
  -H "Authorization: Bearer <user-token>" \
  -H "Content-Type: application/json" \
  -d '{"operationId":"payment-100","channel":"FILE","destination":"payment-100"}'
```

Код появится в `otp-codes.txt`.

## Логирование

Каждый HTTP-запрос логируется через `java.util.logging`:

- метод и путь входящего запроса;
- итоговый HTTP-статус;
- время выполнения;
- логин пользователя, если запрос был с валидным JWT.

Также логируются ключевые события: регистрация, изменение настроек, генерация OTP, неверный код, перевод просроченных кодов в `EXPIRED`.