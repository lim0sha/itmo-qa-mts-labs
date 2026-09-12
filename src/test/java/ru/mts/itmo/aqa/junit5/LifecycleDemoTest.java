package ru.mts.itmo.aqa.junit5;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ТЕМА: жизненный цикл теста.
 * Запустите и посмотрите порядок в консоли:
 *   @BeforeAll → (@BeforeEach → @Test → @AfterEach) × N → @AfterAll
 */
@DisplayName("Жизненный цикл теста")
class LifecycleDemoTest {

    @BeforeAll
    static void beforeAll() {
        System.out.println("@BeforeAll — один раз до всех тестов");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("@AfterAll — один раз после всех тестов");
    }

    @BeforeEach
    void beforeEach() {
        System.out.println("  @BeforeEach — перед каждым тестом");
    }

    @AfterEach
    void afterEach() {
        System.out.println("  @AfterEach — после каждого теста");
    }

    @Test
    void first() {
        System.out.println("    @Test first");
    }

    @Test
    void second() {
        System.out.println("    @Test second");
    }
}
