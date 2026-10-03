# ДЗ №2: автоматизация тестирования REST и GraphQL API

Связанные API-автотесты на JUnit 5 + RestAssured. Сценарии строятся по принципу
`request → response → extract → следующий request`: id ресурса и access token получаются в ходе
собственного прогона и передаются дальше, ничего не захардкожено.

## Требования

- JDK 21
- Maven 3.9+

Внешние сервисы, с которыми работают тесты, публичные и закрывать их не нужно.

## Запуск

Из корня проекта, без IDE:

```bash
mvn test
```

Отдельные сьюты:

```bash
mvn test -Dtest=ChainedPostPatchGetTest
mvn test -Dtest=AuthTokenScenarioTest
mvn test -Dtest=GraphqlQueryTest
mvn test -Dtest=GraphqlMutationTest
mvn test -Dtest=GraphqlNegativeScenarioTest
mvn test -Dtest=RestNegativeScenarioTest
```

Артефакты отчётов: `target/surefire-reports/`.

## Используемые API

### REST — `https://playground.nileslabs.com/api/v1`

| Запрос | Ответ | Что проверяется |
| --- | --- | --- |
| `POST /posts` | `201`, `id` вида `local-<uuid>` | код, сгенерированный id, эхо переданных `title` / `body` / `user_id` |
| `PATCH /posts/{id}` | `200` | код, тот же `id`, изменённый `title` |
| `GET /posts/{id}` | `200`, добавляется поле `thumbnail` | код, совпадение `id`, новый `title`, сохранённые `body` и `user_id` |
| `POST /auth/login` | `200`, `access_token` (JWT), `token_type`, `expires_in`, `user` | код, непустой строковый токен, формат JWT, `Bearer`, положительный TTL |
| `GET /auth/me` | `200` с профилем | передача `Authorization: Bearer <token>`, совпадение `id` / `username` / `email` с ответом логина |
| `GET /auth/me` без или с неверным токеном | `401` | код, `status`, `error`, упоминание Bearer в `message` |

Тестовый пользователь: `Bret` / `Password@123`.

### GraphQL — `https://graphql.postman-echo.com/graphql`

Схема, релевантная часть:

```graphql
type Query       { hello: String!  request: Request! }
type Request     { headers: String! }
type Mutation    { createPerson(person: PersonInput!): Person! }
input PersonInput{ name: String!  age: Int }
type Person      { id: ID!  name: String!  age: Int }
type Subscription{ greetings: String! }
```

| Операция | Ответ | Что проверяется |
| --- | --- | --- |
| `{ hello }` | `200`, `data.hello` | код, данные, отсутствие `errors` |
| `{ hello request { headers } }` | `200` | обе корневые выборки заполнены, `errors` отсутствует |
| `mutation CreatePerson($person: PersonInput!) { createPerson(person: $person) { id name age } }` | `200`, `data.createPerson` | код, сгенерированный `id`, переданные `name` и `age`, отсутствие `errors` |
| `subscription { greetings }` | `200`, `text/event-stream` | выполняется в Postman, см. ниже |

## Сценарии

| Файл | Сценарий |
| --- | --- |
| `support/ApiSpecs.java`, `support/ApiConfig.java` | общие настройки: base URI, Content-Type, User-Agent, bearer-заголовок |
| `support/SandboxSessionFilter.java` | перенос cookie `pg_identity` между запросами одного сценария |
| `support/ExchangeRecorderFilter.java`, `support/ApiDiagnostics.java` | печать запроса и ответа при падении теста |
| `ApiInfrastructureTest` | доступность обоих стендов, работа фильтра сессии, дефолты конфигурации |
| `rest/ChainedPostPatchGetTest` | `POST` → извлечение `id` → `PATCH` → `GET`: изменённое и неизменённые поля, уникальность id |
| `rest/AuthTokenScenarioTest` | логин → токен → `/auth/me`; позитивный и негативный сценарии, свежесть токена |
| `rest/RestNegativeScenarioTest` | параметризованные негативные REST-кейсы |
| `graphql/GraphqlQueryTest` | GraphQL query |
| `graphql/GraphqlMutationTest` | GraphQL mutation, включая необязательный `age` |
| `graphql/GraphqlNegativeScenarioTest` | параметризованные негативные GraphQL-кейсы |

Шаги одного бизнес-сценария выполняются внутри одного теста или через его приватные
helper-методы, поэтому тесты не зависят от порядка запуска друг друга.

## Subscription в Postman

Subscription — долгоживущий SSE-поток, его не автоматизируем в RestAssured, а выполняем в
Postman. Коллекция лежит в `postman/lab-2-api.postman_collection.json`.

1. Postman → Import → выбрать файл.
2. В окружении коллекции проверить `restBaseUrl`, `graphqlUrl`, `username`, `password`.
   `access_token` должен быть пустым — его заполняет запрос *REST / Login* во время прогона.
3. Запустить **REST / Login**, затем **REST / Current user**.
4. Отправить **GraphQL / Subscription - greetings (SSE)** и смотреть панель ответа.

Здоровый поток выглядит так:

```
:

event: next
data: {"data":{"greetings":"Hi"}}

event: next
data: {"data":{"greetings":"Bonjour"}}
...

event: complete
data:
```

Заголовок `Accept: text/event-stream` обязателен: без него стенд отвечает обычным JSON-документом
вместо потока. Запрос *GraphQL / Subscription* содержит проверки статуса, `Content-Type` и наличия
кадров `greetings`.

## Настройка

Любой параметр переопределяется системным свойством или переменной окружения:

| Параметр | System property | Переменная окружения | По умолчанию |
| --- | --- | --- | --- |
| REST base URI | `api.rest.baseUri` | `API_REST_BASE_URI` | `https://playground.nileslabs.com/api/v1` |
| GraphQL URI | `api.graphql.uri` | `API_GRAPHQL_URI` | `https://graphql.postman-echo.com/graphql` |
| Логин | `api.username` | `API_USERNAME` | `Bret` |
| Пароль | `api.password` | `API_PASSWORD` | `Password@123` |
| User-Agent | `api.userAgent` | `API_USER_AGENT` | Chrome 125, Windows |

```bash
mvn test -Dapi.username=Bret -Dapi.password=Password@123
```

> В PowerShell аргументы с точкой в имени нужно оборачивать в кавычки, иначе он разбирает
> `-Dapi.password=...` на два аргумента:
>
> ```powershell
> mvn test "-Dapi.password=Password@123"
> ```
>
> Переменные окружения от такой проблемы свободны и работают везде одинаково:
>
> ```powershell
> $env:API_PASSWORD = "Password@123"; mvn test
> ```

## Важные особенности стендов

Наблюдения, из-за которых тесты выглядят именно так. Каждая проверена на живом стенде.

1. **Пароль в методичке не подходит.** В задании указан `password123`, стенд принимает только
   `Password@123` — это подтверждает и его собственная спецификация
   (`https://playground.nileslabs.com/openapi.json`). Тесты работают на `Password@123`, но пароль
   вынесен в конфигурацию, чтобы заменить его можно было без правки кода.

2. **Песочница привязана к сессии.** Созданный ресурс хранится в песочнице идентичности, которую
   стенд выдаёт в cookie `pg_identity`. Без переноса cookie следующий `PATCH` или `GET` отвечает
   `404 Local record not found or not owned by session`. RestAssured cookie между вызовами не
   переносит, поэтому `SandboxSessionFilter` replay'ит выданный идентификатор. Идентичность
   сбрасывается перед каждым тестом, поэтому прогоны независимы, а лимит в 30 записей на песочницу
   не выбивается.

3. **Cloudflare режет GraphQL по User-Agent.** Запросы к `graphql.postman-echo.com` с не-браузерным
   User-Agent получают `403 Error 1010: browser_signature_banned`. Поэтому User-Agent задан явно в
   `ApiConfig`, а не полагается на дефолт RestAssured.

4. **Поле mutation называется `createPerson`, а `CreatePerson` — имя операции.** Заглавленная
   версия из методички не существует: GraphQL отвечает ошибкой
   `Cannot query field "CreatePerson" on type "Mutation". Did you mean "createPerson"?`.
   Аргумент называется `person`, а не `input`.

5. **Ошибки GraphQL неоднородны по коду ответа.** Неизвестное поле приходит как `200 OK` с
   `errors`, а переменная неверного типа — как `400`. Поэтому `GraphqlNegativeScenarioTest`
   зафиксировал код для каждого случая, а не полагается на один общий.

## Структура проекта

```
pom.xml
postman/
  lab-2-api.postman_collection.json   # GraphQL query/mutation, subscription (SSE), REST login
src/test/java/ru/mts/itmo/aqa/api/
  ApiInfrastructureTest.java
  graphql/
    GraphqlQueryTest.java
    GraphqlMutationTest.java
    GraphqlNegativeScenarioTest.java
  rest/
    ChainedPostPatchGetTest.java
    AuthTokenScenarioTest.java
    RestNegativeScenarioTest.java
  support/
    ApiConfig.java
    ApiSpecs.java
    ApiTestBase.java
    ApiDiagnostics.java
    ExchangeRecorderFilter.java
    LastExchange.java
    SandboxSessionFilter.java
src/test/resources/
  simplelogger.properties
```

Исходники лежат в `src/test/java`: проверяемого кода, кроме тестов, в задании нет, а основной
класс создавать не из чего.
