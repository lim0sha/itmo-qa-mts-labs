package ru.mts.itmo.aqa.junit5;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ТЕМА: отображение результатов в Allure.
 * Метки (@Epic/@Feature/@Story/@Severity/@Owner), шаги (@Step / Allure.step),
 * параметры и вложения — всё это попадает в наглядный HTML-отчёт.
 */
@Epic("Демо JUnit 5")
@Feature("Отчётность Allure")
@DisplayName("Allure — шаги, метки, вложения")
class AllureDemoTest {

    private final Calculator calc = new Calculator();

    @Test
    @Story("Сложение с шагами")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("ishumilov")
    @Description("Показывает шаги, параметры и текстовое вложение в отчёте Allure")
    @DisplayName("2 + 3 = 5 — с шагами и вложением")
    void addWithSteps() {
        int a = 2;
        int b = 3;
        Allure.parameter("a", a);
        Allure.parameter("b", b);

        int result = step("Складываем " + a + " и " + b, () -> calc.add(a, b));
        attachResult(a, b, result);

        step("Проверяем результат", () -> assertEquals(5, result));
    }

    @Test
    @Story("Обработка ошибки")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Деление на ноль — шаг с вложением исключения")
    void divByZero() {
        divByZeroStep();
    }

    @Step("Проверяем деление на ноль и логируем исключение")
    void divByZeroStep() {
        try {
            calc.div(1, 0);
        } catch (IllegalArgumentException expected) {
            Allure.addAttachment("Пойманное исключение", expected.getMessage());
        }
    }

    @Attachment(value = "Лог вычисления", type = "text/plain")
    String attachResult(int a, int b, int result) {
        return a + " + " + b + " = " + result;
    }
}
