package ru.mts.itmo.aqa.junit5;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Политика пароля: проверяет пароль по набору правил и возвращает СПИСОК нарушений.
 * «Система под тестом» для домашнего задания занятия 4.
 * <p>
 * <b>Спецификация (то, как должно работать).</b> Проверки независимы: собираются ВСЕ нарушения,
 * а не только первое. Порядок нарушений в списке не определён и на него нельзя опираться.
 * <ul>
 *   <li>{@code null} на входе пароля — {@link IllegalArgumentException};</li>
 *   <li>{@link Violation#TOO_SHORT} — длина меньше {@value #MIN_LENGTH};</li>
 *   <li>{@link Violation#TOO_LONG} — длина больше {@value #MAX_LENGTH} (сама
 *       {@value #MAX_LENGTH} — ещё допустима);</li>
 *   <li>{@link Violation#NO_DIGIT} — нет ни одной цифры;</li>
 *   <li>{@link Violation#NO_UPPER} — нет ни одной заглавной латинской буквы;</li>
 *   <li>{@link Violation#NO_LOWER} — нет ни одной строчной латинской буквы;</li>
 *   <li>{@link Violation#NO_SPECIAL} — нет ни одного символа из {@value #SPECIAL};</li>
 *   <li>{@link Violation#HAS_WHITESPACE} — есть пробельный символ (пробел, таб и т. п.);</li>
 *   <li>{@link Violation#REPEATED_RUN} — три и более одинаковых символа подряд
 *       («aaa» — нарушение, «aa» — нет);</li>
 *   <li>{@link Violation#CONTAINS_LOGIN} — пароль содержит логин, БЕЗ учёта регистра;
 *       проверяется только если логин передан и не пустой;</li>
 *   <li>{@link Violation#BLACKLISTED} — пароль совпадает с одним из простых
 *       ({@code password, qwerty, 123456, admin, welcome}), БЕЗ учёта регистра.</li>
 * </ul>
 * Пароль валиден, когда список нарушений пуст.
 * <p>
 * <b>Внимание.</b> Это класс из домашнего задания. Реализация ниже — учебная: она местами
 * НЕ соответствует спецификации выше, и найти эти расхождения тестами по спецификации —
 * и есть задание (постановка — в {@code homework/homework.md}). Отсюда главное правило ДЗ:
 * тесты пишутся по спецификации, а не по коду. Начнёте с чтения реализации — повторите
 * её ошибки и не найдёте ни одного дефекта.
 */
public class PasswordPolicy {

    /** Минимальная длина пароля. */
    public static final int MIN_LENGTH = 8;
    /** Максимальная допустимая длина пароля. */
    public static final int MAX_LENGTH = 64;
    /** Символы, которые считаются специальными. */
    public static final String SPECIAL = "!@#$%^&*()-_=+";

    private static final Set<String> BLACKLIST =
            Set.of("password", "qwerty", "123456", "admin", "welcome");

    /** Виды нарушений политики. */
    public enum Violation {
        TOO_SHORT, TOO_LONG, NO_DIGIT, NO_UPPER, NO_LOWER,
        NO_SPECIAL, HAS_WHITESPACE, REPEATED_RUN, CONTAINS_LOGIN, BLACKLISTED
    }

    /** Результат проверки: валиден ли пароль и что именно нарушено. */
    public static final class Result {
        private final List<Violation> violations;

        Result(List<Violation> violations) {
            this.violations = List.copyOf(violations);
        }

        /** Пароль валиден, когда нарушений нет. */
        public boolean isValid() {
            return violations.isEmpty();
        }

        /** Все найденные нарушения. Порядок не определён. */
        public List<Violation> violations() {
            return violations;
        }

        @Override
        public String toString() {
            return isValid() ? "Result{valid}" : "Result" + violations;
        }
    }

    /** Проверка пароля без учёта логина. */
    public Result check(String password) {
        return check(password, null);
    }

    /**
     * Проверка пароля с учётом логина пользователя.
     *
     * @param password проверяемый пароль
     * @param login    логин; {@code null} или пустой — правило CONTAINS_LOGIN не применяется
     * @return результат со списком нарушений
     * @throws IllegalArgumentException если пароль {@code null}
     */
    public Result check(String password, String login) {
        if (password == null) {
            throw new IllegalArgumentException("Пароль не может быть null");
        }

        List<Violation> found = new ArrayList<>();

        if (password.length() < MIN_LENGTH) {
            found.add(Violation.TOO_SHORT);
        }
        if (password.length() > MAX_LENGTH - 1) {
            found.add(Violation.TOO_LONG);
        }

        boolean digit = false, upper = false, lower = false, special = false, space = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isDigit(c)) {
                digit = true;
            } else if (c >= 'A' && c <= 'Z') {
                upper = true;
            } else if (c >= 'a' && c <= 'z') {
                lower = true;
            }
            if (SPECIAL.indexOf(c) >= 0) {
                special = true;
            }
            if (Character.isWhitespace(c)) {
                space = true;
            }
        }
        if (!digit) {
            found.add(Violation.NO_DIGIT);
        }
        if (!upper) {
            found.add(Violation.NO_UPPER);
        }
        if (!lower) {
            found.add(Violation.NO_LOWER);
        }
        if (!special) {
            found.add(Violation.NO_SPECIAL);
        }
        if (space) {
            found.add(Violation.HAS_WHITESPACE);
        }

        if (hasRepeatedRun(password)) {
            found.add(Violation.REPEATED_RUN);
        }

        if (login != null && !login.isEmpty()
                && password.toLowerCase().contains(login.toLowerCase())) {
            found.add(Violation.CONTAINS_LOGIN);
        }

        if (BLACKLIST.contains(password)) {
            found.add(Violation.BLACKLISTED);
        }

        return new Result(found);
    }

    /** Есть ли в строке серия одинаковых символов подряд. */
    private boolean hasRepeatedRun(String s) {
        int run = 1;
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(i - 1)) {
                run++;
                if (run > 3) {
                    return true;
                }
            } else {
                run = 1;
            }
        }
        return false;
    }
}
