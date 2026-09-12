package ru.mts.itmo.aqa.junit5;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ТЕМА: параметризованные тесты — одна логика, много данных.
 * Прямая стыковка с тест-дизайном (занятие 1): классы эквивалентности и границы.
 */
@Epic("Демо JUnit 5")
@Feature("Параметризованные тесты")
@DisplayName("Параметризованные тесты")
class ParameterizedDemoTest {

    // @ValueSource — валидные представители класса эквивалентности
    @ParameterizedTest(name = "возраст {0} — валиден")
    @ValueSource(ints = {18, 30, 65})
    void validAges(int age) {
        assertTrue(AgeValidator.isValid(age));
    }

    // @CsvSource — граничные значения (17/18 и 65/66)
    @ParameterizedTest(name = "возраст {0} → ожидаем {1}")
    @CsvSource({"17,false", "18,true", "65,true", "66,false"})
    void boundaries(int age, boolean expected) {
        assertEquals(expected, AgeValidator.isValid(age));
    }

    // @MethodSource — сложные наборы данных из метода-поставщика
    static Stream<Arguments> fizzBuzzCases() {
        return Stream.of(
                Arguments.of(1, "1"),
                Arguments.of(3, "Fizz"),
                Arguments.of(5, "Buzz"),
                Arguments.of(15, "FizzBuzz")
        );
    }

    @ParameterizedTest(name = "convert({0}) = {1}")
    @MethodSource("fizzBuzzCases")
    void fizzBuzz(int n, String expected) {
        assertEquals(expected, FizzBuzz.convert(n));
    }
}
