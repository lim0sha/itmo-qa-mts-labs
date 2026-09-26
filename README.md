# demo-junit — ДЗ №1: тестирование PasswordPolicy по спецификации

Ветка с готовым заданием: [`lab-1`](https://github.com/lim0sha/itmo-qa-mts-labs/tree/lab-1)

## Состав сдачи

- `PasswordPolicyTest.java` — тест-сьют по спецификации
- `PasswordPolicyTest-баги.md` — баг-репорты на три найденных дефекта
- `PasswordPolicy.java` — исправленная реализация с регрессионными тестами
- Порядок «тест до фикса» виден в истории ветки
- [Скриншот дерева отчёта Allure](allure.png)
- [Скриншот строки PasswordPolicy в отчёте Jacoco](jacoco.png)

## Команды

Из каталога `code/demo-junit`:

```bash
mvn test -Dtest=PasswordPolicyTest      # прогон тест-сьюта
mvn clean test                          # полный прогон с пересчётом покрытия
```

Отчёты:

- Jacoco: `target/site/jacoco/index.html`
- Allure: `allure serve target/allure-results`

Покрытие ветвей `PasswordPolicy`: ≈ 98 % (требование ≥ 90 %).