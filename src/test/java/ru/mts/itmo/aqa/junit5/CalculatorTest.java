package ru.mts.itmo.aqa.junit5;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ТЕМА: базовый тест и паттерн AAA (Arrange–Act–Assert).
 * Плюс негативный сценарий через assertThrows.
 * <p>
 * Оба теста помечены {@code @Tag("sanity")} — это самый лёгкий «проверка на вменяемость»
 * набор (быстрее и уже, чем smoke): базовая арифметика вообще работает.
 * Запуск набора: {@code mvn test -Dgroups=sanity} (тема 27-28).
 */
@Epic("Демо JUnit 5")
@Feature("Калькулятор")
@DisplayName("Калькулятор — базовые операции")
class CalculatorTest {

    @Test
    @Tag("sanity")
    @DisplayName("2 + 3 = 5")
    void add_returnsSum() {
        Calculator calc = new Calculator();   // Arrange — подготовка
        int result = calc.add(2, 3);          // Act — действие
        assertEquals(5, result);              // Assert — проверка
    }

    @Test
    @Tag("sanity")
    @DisplayName("Деление на ноль бросает исключение")
    void div_byZero_throws() {
        Calculator calc = new Calculator();
        assertThrows(IllegalArgumentException.class, () -> calc.div(10, 0));
    }
}
