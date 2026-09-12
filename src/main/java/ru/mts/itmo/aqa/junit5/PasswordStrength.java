package ru.mts.itmo.aqa.junit5;

/**
 * Оценка пароля по длине — «система под тестом» для практик СЕМИНАРА занятия 4
 * («JUnit 5 на практике»).
 * <p>
 * <b>Тестов на этот класс в проекте нет</b> — в отчёте Jacoco он красный (0 %). Тесты на него
 * вы пишете сами: практика 1 (первые тесты), практика 2 (подготовка и аннотации),
 * практика 4 (границы через параметризацию), практика 5 (добор непокрытой ветки).
 * <p>
 * <b>Классы эквивалентности и границы</b> (материал занятия 1):
 * <ul>
 *   <li>длина &lt; 0 — {@link IllegalArgumentException} (невалидный ввод);</li>
 *   <li>0…7 — {@link Level#WEAK};</li>
 *   <li>8…11 — {@link Level#MEDIUM};</li>
 *   <li>12…64 — {@link Level#STRONG};</li>
 *   <li>65 и больше — {@link Level#TOO_LONG}.</li>
 * </ul>
 * Внутренние границы: 7/8, 11/12, 64/65 — плюс 0 и −1.
 * <p>
 * Метод {@link #of(int)} сделан ЭКЗЕМПЛЯРНЫМ (в отличие от статического
 * {@link AgeValidator#isValid(int)}) намеренно: тогда в тесте появляется настоящий шаг Arrange,
 * а в практике 2 есть что вынести в поле с {@code @BeforeEach}.
 */
public class PasswordStrength {

    /** Короче этого пароль считаем слабым. */
    public static final int MIN_LENGTH = 8;
    /** С этой длины пароль считаем надёжным. */
    public static final int STRONG_LENGTH = 12;
    /** Длиннее этого пароль не принимаем. */
    public static final int MAX_LENGTH = 64;

    /** Уровень надёжности пароля. */
    public enum Level {
        /** Слишком короткий. */
        WEAK,
        /** Приемлемый. */
        MEDIUM,
        /** Надёжный. */
        STRONG,
        /** Длиннее допустимого — такой пароль не принимаем. */
        TOO_LONG
    }

    /**
     * Уровень надёжности пароля по его длине.
     *
     * @param length длина пароля в символах
     * @return уровень надёжности
     * @throws IllegalArgumentException если длина отрицательная
     */
    public Level of(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Длина пароля не может быть отрицательной: " + length);
        }
        if (length > MAX_LENGTH) {
            return Level.TOO_LONG;
        }
        if (length >= STRONG_LENGTH) {
            return Level.STRONG;
        }
        if (length >= MIN_LENGTH) {
            return Level.MEDIUM;
        }
        return Level.WEAK;
    }
}
