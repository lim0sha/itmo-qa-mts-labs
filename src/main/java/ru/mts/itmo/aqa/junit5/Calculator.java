package ru.mts.itmo.aqa.junit5;

/**
 * Простой калькулятор — «система под тестом» для демонстрации JUnit 5.
 * Методы намеренно минимальны, чтобы фокус был на тестах, а не на логике.
 */
public class Calculator {

    /** Сложение двух чисел. */
    public int add(int a, int b) {
        return a + b;
    }

    /** Умножение двух чисел. */
    public int multiplication(int a, int b) {
        return a * b;
    }

    /** Разность двух чисел. */
    public int minus(int a, int b) {
        return a - b;
    }

    /**
     * Деление двух чисел.
     * @throws IllegalArgumentException при делении на ноль — негативный сценарий для теста.
     */
    public int div(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("Divide by zero error");
        }
        return a / b;
    }
}
