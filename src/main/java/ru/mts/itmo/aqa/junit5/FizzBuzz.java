package ru.mts.itmo.aqa.junit5;

/**
 * Классическая задача FizzBuzz — удобна для демонстрации @MethodSource:
 * много входов, одна логика.
 */
public class FizzBuzz {

    public static String convert(int n) {
        if (n % 15 == 0) {
            return "FizzBuzz";
        }
        if (n % 3 == 0) {
            return "Fizz";
        }
        if (n % 5 == 0) {
            return "Buzz";
        }
        return String.valueOf(n);
    }
}
