# ДЗ №2: REST и GraphQL API

## Запуск

JDK 21, Maven 3.9+. Из корня проекта:

```bash
mvn test
```

Тесты идут против публичных стендов, дополнительная настройка не требуется.

## API

REST - `https://playground.nileslabs.com/api/v1`

| Запрос | Проверки |
| --- | --- |
| `POST /posts` | `201`, сгенерированный `id`, эхо `title` / `body` / `user_id` |
| `PATCH /posts/{id}` | `200`, тот же `id`, изменённый `title` |
| `GET /posts/{id}` | `200`, совпадение `id`, новый `title`, сохранённые `body` и `user_id` |
| `POST /auth/login` | `200`, непустой JWT `access_token` |
| `GET /auth/me` | `200`, совпадение `id` / `username` / `email` с ответом логина |
| `GET /auth/me` без корректного Bearer | `401` |

Логин стенда: `Bret` / `Password@123`. Пароль из задания (`password123`) стенд не принимает.

GraphQL - `https://graphql.postman-echo.com/graphql`

| Операция | Проверки |
| --- | --- |
| `{ hello }` | `200`, `data.hello`, отсутствие `errors` |
| `mutation createPerson(person: PersonInput!)` | `200`, `id` / `name` / `age`, отсутствие `errors` |
| `subscription greetings` | поток SSE, выполняется в Postman |

## Сценарии

| Тест | Сценарий |
| --- | --- |
| `ApiInfrastructureTest` | доступность стендов, изоляция сессии, дефолты конфигурации |
| `rest/ChainedPostPatchGetTest` | `POST` => `id` => `PATCH` => `GET` |
| `rest/AuthTokenScenarioTest` | логин => токен => `GET /auth/me`, негативный сценарий |
| `rest/RestNegativeScenarioTest` | негативные REST-кейсы, параметризованные |
| `graphql/GraphqlQueryTest` | query |
| `graphql/GraphqlMutationTest` | mutation, включая необязательный `age` |
| `graphql/GraphqlNegativeScenarioTest` | негативные GraphQL-кейсы, параметризованные |

Шаги одного сценария выполняются внутри одного теста, порядок запуска тестов на результат не влияет.

## Subscription

В RestAssured не автоматизируется. Коллекция - `postman/lab-2-api.postman_collection.json`.

1. Postman => Import => файл коллекции.
2. Выполнить `REST / Login` - запрос сам подставляет полученный токен в переменную `access_token`.
3. Отправить `GraphQL / Subscription - greetings (SSE)`.
