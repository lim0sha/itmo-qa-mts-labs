package ru.mts.itmo.aqa.junit5;

/**
 * Валидатор возраста: допустимы значения 18..65 включительно.
 * Идеальный кейс для параметризованных тестов, классов эквивалентности
 * и граничных значений (техники тест-дизайна из занятия 1).
 */
public class AgeValidator {

    public static final int MIN = 18;
    public static final int MAX = 65;

    /** @return true, если возраст в допустимом диапазоне [18, 65]. */
    public static boolean isValid(int age) {
        return age >= MIN && age <= MAX;
    }
}
