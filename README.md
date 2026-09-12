# demo-junit — учебный проект занятия 3-4

Проект, в котором мы работаем на семинаре «JUnit 5 на практике» и в котором делается
домашнее задание. Один и тот же код собирается двумя системами сборки: **Maven** (основной
способ) и **Gradle** (второй вариант — тот же код, другая сборка).

Первое, что нужно сделать: убедиться, что у вас «зелено».

```bash
cd code/demo-junit
mvn clean test        # 22 теста зелёные, 1 пропущен (@Disabled — так и задумано)
./test-summary.sh     # компактный итог: что прогналось и какое покрытие по классам
```

## Что нужно поставить

| Что | Зачем | Проверка |
|---|---|---|
| **JDK 21** (LTS) | на нём собран и проверен проект | `java -version` → 21.x |
| **Maven 3.9+** | основной способ запуска | `mvn -v` |
| VS Code + Extension Pack for Java | работа на занятии | панель Testing видит тесты |
| Allure CLI (по желанию) | наглядный HTML-отчёт | `allure --version` |

Про JDK: берите именно 21. На более новых версиях (24, 25, 26) сборка проходит, но агент
AspectJ, через который работают `@Step` и `@Attachment` в Allure, печатает в консоль
стек-трейс и часть разметки отчёта может не собраться. Gradle-вариант от версии Java на
машине не зависит — он сам скачивает нужный JDK через toolchain.

Allure CLI ставится независимо от проекта: `brew install allure` (macOS), `scoop install allure`
(Windows), `npm i -g allure-commandline` или zip-дистрибутив с GitHub. Если не встал — не
страшно: тесты и покрытие работают без него, отчёт Allure мы разбираем с экрана.

## Как устроен проект

```
src/main/java/ru/mts/itmo/aqa/junit5/   production-код — «система под тестом»
src/test/java/ru/mts/itmo/aqa/junit5/   сами тесты
```

Два корня исходников, пакеты в них одинаковые (`ru.mts.itmo.aqa.junit5`) — поэтому тест
видит свой production-класс напрямую, без импорта. В VS Code удобнее искать класс по имени
через `⌘P` / `Ctrl+P`, а не ходить по дереву.

### Production-классы

| Класс | Что делает | Зачем в проекте |
|---|---|---|
| `Calculator` | сложение, вычитание, умножение, деление (деление на 0 → исключение) | демо: AAA, `assertThrows` |
| `AgeValidator` | `isValid(age)` — допустимы 18…65 включительно | демо: параметризация и границы |
| `FizzBuzz` | `convert(n)` — Fizz / Buzz / FizzBuzz | демо: `@MethodSource` |
| `PasswordStrength` | `of(length)` → `WEAK` / `MEDIUM` / `STRONG` / `TOO_LONG` | **цель практик семинара**, тестов нет |
| `PasswordPolicy` | `check(password[, login])` → список нарушений | **домашнее задание**, тестов нет |

### Тесты, которые уже есть (демо с занятия)

| Класс | Тема |
|---|---|
| `CalculatorTest` | базовый тест, AAA, негативный сценарий |
| `LifecycleDemoTest` | жизненный цикл: `@BeforeAll` → `@BeforeEach` → `@Test` → `@AfterEach` → `@AfterAll` |
| `AnnotationsDemoTest` | `@DisplayName`, `@Tag`, `@Disabled` |
| `AssertionsDemoTest` | JUnit-assertions, AssertJ, `SoftAssertions` |
| `ParameterizedDemoTest` | `@ValueSource`, `@CsvSource`, `@MethodSource` |
| `AllureDemoTest` | `@Epic`/`@Feature`/`@Story`, `@Step`, `@Attachment` — образец разметки для ДЗ |

Свои тесты вы создаёте в том же пакете: `PasswordStrengthTest` (практики семинара) и
`PasswordPolicyTest` (домашнее задание).

## Команды

| Задача | Maven | Gradle |
|---|---|---|
| Все тесты | `mvn test` | `./gradlew test` |
| Полный прогон с пересчётом покрытия | `mvn clean test` | `./gradlew clean test` |
| Только свой класс | `mvn test -Dtest=PasswordStrengthTest` | `./gradlew test --tests '*PasswordStrengthTest'` |
| Только свой тег | `mvn test -Dgroups=my` | `./gradlew test -Pgroups=my` |
| Итог последнего прогона | `./test-summary.sh` | — |
| Отчёт покрытия | `target/site/jacoco/index.html` | `build/reports/jacoco/test/html/index.html` |
| Отчёт Allure | `allure serve target/allure-results` | `allure serve build/allure-results` |

Все команды — из каталога `code/demo-junit`. В VS Code то же самое лежит готовыми задачами:
Terminal → Run Task (⇧⌘P / Ctrl+Shift+P → «Tasks: Run Task»), открывать надо папку-корень
пакета, а не `code/demo-junit`.

**Про `clean`.** Агент Jacoco по умолчанию ДОПИСЫВАЕТ данные в `target/jacoco.exec`. Без
`clean` покрытие копится между прогонами: удалённые и черновые тесты продолжают «покрывать»
код, и процент врёт в вашу пользу. Смотрите покрытие только после `mvn clean test`.

**Кнопка ▷ в правом верхнем углу** (расширение Code Runner) переопределена на запуск
открытого тест-класса через Maven — иначе она делает `javac File.java` без classpath и падает
на `package io.qameta.allure does not exist`. Более привычный способ — ▷ в левом поле рядом
с `@Test` или панель Testing.

## Про цифры покрытия

Сводный процент по проекту низкий (порядка 15 %) — и это нормально: в проекте специально
живут классы без тестов. `PasswordStrength` — цель практик семинара, `PasswordPolicy` —
домашнее задание, оба в отчёте красные (0 %). Отсюда главный вывод занятия: **сводная цифра
покрытия почти всегда врёт, смотреть надо по классам.**

`Calculator` и `AgeValidator` покрыты демо-тестами не полностью (у `Calculator` не проверен
`minus` и «счастливый» путь деления, у `AgeValidator` — не все ветки). На слайде семинара
в примере отчёта стоят другие цифры: слайд показывает отчёт полного преподавательского
проекта, где те же классы используются тестами следующих занятий. Добор этого покрытия —
как раз то, что предложено доделать дома.
