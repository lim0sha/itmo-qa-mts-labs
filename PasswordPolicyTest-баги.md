# Баг-репорты: PasswordPolicy по спецификации

Тест-сьют: `ru.mts.itmo.aqa.junit5.PasswordPolicyTest`
Окружение: проект `demo-junit`, класс `ru.mts.itmo.aqa.junit5.PasswordPolicy`, JDK 21.0.6, `mvn test -Dtest=PasswordPolicyTest`

Прогон: 35 тестов, 7 падений, все по трём дефектам ниже.

---

## Баг 1. Пароль из 64 символов помечается как TOO_LONG

**Окружение:** проект `demo-junit`, класс `ru.mts.itmo.aqa.junit5.PasswordPolicy`, JDK 21.0.6, `mvn test -Dtest=PasswordPolicyTest`

**Шаги:**
1. Взять валидный по остальным правилам пароль из 64 символов (`"Ni2#" + "ga".repeat(30)`)
2. Вызвать `new PasswordPolicy().check(password)`
3. Посмотреть список нарушений

**Фактически:** список содержит `TOO_LONG`

**Ожидаемо:** `TOO_LONG` нет. Цитата из спецификации: «длина больше 64 - TOO_LONG. Длина ровно 64 - ещё допустима»

**Severity:** Major
**Обоснование:** ломает валидацию всех корректных паролей длиной 64

**Priority:** High
**Обоснование:** граница длины в приоритетных классах эквивалентности

**Код падающего теста:**
```java
@ParameterizedTest(name = "длина {1}: пароль \"{0}\"")
@MethodSource("lengthBoundaries")
@DisplayName("Границы длины")
void lengthBoundariesShouldReturnExpectedViolation(
        String password, int length, PasswordPolicy.Violation expected) {
    List<PasswordPolicy.Violation> violations = policy.check(password).violations();
    if (expected != null) {
        assertThat(violations).contains(expected);
    } else {
        assertThat(violations).doesNotContain(TOO_SHORT, TOO_LONG);
    }
}

static Stream<Arguments> lengthBoundaries() {
    return Stream.of(
            Arguments.of("Aa1!bcd", 7, TOO_SHORT),
            Arguments.of("Aa1!bcde", 8, null),
            Arguments.of(VALID_64, 64, null),
            Arguments.of(VALID_64 + "x", 65, TOO_LONG)
    );
}

static final String VALID_64 = "Ni2#" + "ga".repeat(30);
```

---

## Баг 2. Серия из трёх символов не даёт REPEATED_RUN

**Окружение:** проект `demo-junit`, класс `ru.mts.itmo.aqa.junit5.PasswordPolicy`, JDK 21.0.6, `mvn test -Dtest=PasswordPolicyTest`

**Шаги:**
1. Взять пароль с серией из трёх одинаковых символов (`Aa1!bcDaaa`)
2. Вызвать `new PasswordPolicy().check(password)`
3. Посмотреть список нарушений

**Фактически:** список пуст, `REPEATED_RUN` нет

**Ожидаемо:** есть `REPEATED_RUN`. Цитата из спецификации: «три и более одинаковых символа подряд - REPEATED_RUN. „aaa“ - нарушение, „aa“ - нет»

**Severity:** Major
**Обоснование:** пропускает пароли с серией из трёх повторов

**Priority:** High
**Обоснование:** граница 2/3 выделена в спеке, порог неверный (4 вместо 3)

**Код падающего теста:**
```java
@ParameterizedTest(name = "серия в \"{0}\" - срабатывает: {1}")
@MethodSource("runs")
@DisplayName("Границы серии")
void runLengthShouldReturnExpectedViolation(String password, boolean fires) {
    List<PasswordPolicy.Violation> violations = violations(password);
    if (fires) {
        assertThat(violations).contains(REPEATED_RUN);
    } else {
        assertThat(violations).doesNotContain(REPEATED_RUN);
    }
}

static Stream<Arguments> runs() {
    return Stream.of(
            Arguments.of("Aa1!bcDaa", false),
            Arguments.of("Aa1!bcDaaa", true)
    );
}
```

Тот же дефект падает в `CompositeTests.passwordWithManyViolationsShouldReturnAllErrors` (пароль `abc  aaa`, нет `REPEATED_RUN`).

---

## Баг 3. Блеклист проверяется с учётом регистра

**Окружение:** проект `demo-junit`, класс `ru.mts.itmo.aqa.junit5.PasswordPolicy`, JDK 21.0.6, `mvn test -Dtest=PasswordPolicyTest`

**Шаги:**
1. Взять слово из блеклиста в другом регистре (`PASSWORD`, `QWERTY`, `ADMIN`, `WELCOME`)
2. Вызвать `new PasswordPolicy().check(password)`
3. Посмотреть список нарушений

**Фактически:** `BLACKLISTED` нет

**Ожидаемо:** есть `BLACKLISTED`. Цитата из спецификации: «пароль совпадает с одним из простых: password, qwerty, 123456, admin, welcome, без учёта регистра»

**Severity:** Major
**Обоснование:** обход простым изменением регистра

**Priority:** High
**Обоснование:** легко воспроизводится, обход политики

**Код падающего теста:**
```java
@ParameterizedTest(name = "\"{0}\" другим регистром")
@MethodSource("blacklistedWords")
@DisplayName("другой регистр")
void blacklistedPasswordOtherCaseShouldReturnBlacklistedError(String word) {
    assertThat(violations(word.toUpperCase())).contains(BLACKLISTED);
}
```