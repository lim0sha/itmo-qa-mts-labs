package ru.mts.itmo.aqa.junit5;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ТЕМА: аннотации JUnit 5 — @DisplayName, @Disabled, @Tag.
 * Запуск по тегам: mvn test -Dgroups=smoke  (или в IDE по тегу).
 */
@DisplayName("Аннотации JUnit 5")
class AnnotationsDemoTest {

    private final Calculator calc = new Calculator();

    @Test
    @Tag("smoke")
    @DisplayName("Умножение 3 × 4 = 12")
    void multiplication() {
        assertEquals(12, calc.multiplication(3, 4));
    }

    @Test
    @Tag("slow")
    @Disabled("Демонстрация @Disabled: тест временно отключён с указанием причины")
    void disabledExample() {
        assertEquals(0, calc.minus(5, 5));
    }
}
