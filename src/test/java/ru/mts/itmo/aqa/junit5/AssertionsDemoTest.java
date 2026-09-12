package ru.mts.itmo.aqa.junit5;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ТЕМА: виды проверок.
 * 1) Стандартные JUnit Jupiter assertions.
 * 2) AssertJ — «текучие» (fluent) проверки: assertThat(...).
 * 3) SoftAssertions — выполнить все проверки, не падая на первой.
 */
@DisplayName("Виды проверок (assertions)")
class AssertionsDemoTest {

    private final Calculator calc = new Calculator();

    @Test
    @DisplayName("JUnit assertions: equals, true, throws, all")
    void junitAssertions() {
        assertEquals(7, calc.add(3, 4));
        assertTrue(AgeValidator.isValid(30));
        assertThrows(IllegalArgumentException.class, () -> calc.div(1, 0));
        assertAll(
                () -> assertEquals(6, calc.multiplication(2, 3)),
                () -> assertEquals(1, calc.minus(3, 2))
        );
    }

    @Test
    @DisplayName("AssertJ — fluent-проверки")
    void assertJ() {
        assertThat(calc.add(2, 2)).isEqualTo(4).isPositive();
        assertThat("FizzBuzz").startsWith("Fizz").contains("Buzz");
    }

    @Test
    @DisplayName("SoftAssertions — все проверки без остановки на первой")
    void softAssertions() {
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(calc.add(2, 3)).isEqualTo(5);
        soft.assertThat(AgeValidator.isValid(17)).isFalse();
        soft.assertAll();
    }
}
