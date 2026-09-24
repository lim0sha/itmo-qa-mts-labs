package ru.mts.itmo.aqa.junit5;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.BLACKLISTED;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.CONTAINS_LOGIN;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.HAS_WHITESPACE;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.NO_DIGIT;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.NO_LOWER;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.NO_SPECIAL;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.NO_UPPER;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.REPEATED_RUN;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.TOO_LONG;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.TOO_SHORT;

@DisplayName("PasswordPolicy по спецификации")
class PasswordPolicyTest {

    private final PasswordPolicy policy = new PasswordPolicy();

    private List<PasswordPolicy.Violation> violations(String password) {
        return policy.check(password).violations();
    }

    private List<PasswordPolicy.Violation> violations(String password, String login) {
        return policy.check(password, login).violations();
    }

    @Test
    @DisplayName("null - исключение")
    void nullPasswordShouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> policy.check(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Nested
    @DisplayName("TOO_SHORT / TOO_LONG")
    class LengthTests {

        static final String VALID_64 = "Ni2#" + "ga".repeat(30);

        static Stream<Arguments> lengthBoundaries() {
            return Stream.of(
                    Arguments.of("Aa1!bcd", 7, TOO_SHORT),
                    Arguments.of("Aa1!bcde", 8, null),
                    Arguments.of(VALID_64, 64, null),
                    Arguments.of(VALID_64 + "x", 65, TOO_LONG)
            );
        }

        @ParameterizedTest(name = "длина {1}: пароль \"{0}\"")
        @MethodSource("lengthBoundaries")
        @DisplayName("Границы длины")
        void lengthBoundariesShouldReturnExpectedViolation(String password, int length, PasswordPolicy.Violation expected) {
            List<PasswordPolicy.Violation> violations = policy.check(password).violations();
            if (expected != null) {
                assertThat(violations).contains(expected);
            } else {
                assertThat(violations).doesNotContain(TOO_SHORT, TOO_LONG);
            }
        }
    }

    @Nested
    @DisplayName("NO_DIGIT")
    class NoDigitTests {

        @Test
        @DisplayName("без цифр")
        void passwordWithoutDigitsShouldReturnNoDigitError() {
            assertThat(violations("Aab!cdef")).contains(NO_DIGIT);
        }

        @Test
        @DisplayName("с цифрой")
        void passwordWithDigitShouldNotReturnNoDigitError() {
            assertThat(violations("Aa1!bcde")).doesNotContain(NO_DIGIT);
        }
    }

    @Nested
    @DisplayName("NO_UPPER")
    class NoUpperTests {

        @Test
        @DisplayName("без заглавной")
        void passwordWithoutUppercaseShouldReturnNoUpperError() {
            assertThat(violations("ab1!cdef")).contains(NO_UPPER);
        }

        @Test
        @DisplayName("с заглавной")
        void passwordWithUppercaseShouldNotReturnNoUpperError() {
            assertThat(violations("Aa1!bcde")).doesNotContain(NO_UPPER);
        }
    }

    @Nested
    @DisplayName("NO_LOWER")
    class NoLowerTests {

        @Test
        @DisplayName("без строчной")
        void passwordWithoutLowercaseShouldReturnNoLowerError() {
            assertThat(violations("AA1!BCDE")).contains(NO_LOWER);
        }

        @Test
        @DisplayName("со строчной")
        void passwordWithLowercaseShouldNotReturnNoLowerError() {
            assertThat(violations("Aa1!bcde")).doesNotContain(NO_LOWER);
        }
    }

    @Nested
    @DisplayName("NO_SPECIAL")
    class NoSpecialTests {

        @Test
        @DisplayName("без спецсимволов")
        void passwordWithoutSpecialCharsShouldReturnNoSpecialError() {
            assertThat(violations("Aa1bcdef")).contains(NO_SPECIAL);
        }

        @Test
        @DisplayName("первый символ набора")
        void passwordWithFirstSpecialCharShouldNotReturnNoSpecialError() {
            assertThat(violations("Aa1!bcde")).doesNotContain(NO_SPECIAL);
        }
    }

    @Nested
    @DisplayName("HAS_WHITESPACE")
    class HasWhitespaceTests {

        @Test
        @DisplayName("пробел")
        void passwordWithSpaceShouldReturnHasWhitespaceError() {
            assertThat(violations("Aa1 bcde")).contains(HAS_WHITESPACE);
        }

        @Test
        @DisplayName("табуляция")
        void passwordWithTabShouldReturnHasWhitespaceError() {
            assertThat(violations("Aa1\tbcde")).contains(HAS_WHITESPACE);
        }

        @Test
        @DisplayName("без пробельных")
        void passwordWithoutWhitespaceShouldNotReturnHasWhitespaceError() {
            assertThat(violations("Aa1!bcde")).doesNotContain(HAS_WHITESPACE);
        }

        @Test
        @DisplayName("перенос строки")
        void passwordWithNewlineShouldReturnHasWhitespaceError() {
            assertThat(violations("Aa1\nbcde")).contains(HAS_WHITESPACE);
        }

        @Test
        @DisplayName("возврат каретки")
        void passwordWithCarriageReturnShouldReturnHasWhitespaceError() {
            assertThat(violations("Aa1\rbcde")).contains(HAS_WHITESPACE);
        }

        @Test
        @DisplayName("перевод страницы")
        void passwordWithFormFeedShouldReturnHasWhitespaceError() {
            assertThat(violations("Aa1\fbcde")).contains(HAS_WHITESPACE);
        }
    }

    @Nested
    @DisplayName("REPEATED_RUN")
    class RepeatedRunTests {

        static Stream<Arguments> runs() {
            return Stream.of(
                    Arguments.of("Aa1!bcDaa", false),
                    Arguments.of("Aa1!bcDaaa", true)
            );
        }

        @ParameterizedTest(name = "серия в \"{0}\" - срабатывает: {1}")
        @MethodSource("runs")
        @DisplayName("Границы серии")
        void runLengthShouldReturnExpectedViolation(String password, boolean fires) {
            List<PasswordPolicy.Violation> violations = violations(password);
            if (fires) {
                assertThat(violations).contains(REPEATED_RUN);
            } else {
                assertThat(violations).doesNotContain(REPEATED_RUN);
            }
        }
    }

    @Nested
    @DisplayName("CONTAINS_LOGIN")
    class ContainsLoginTests {

        private static final String PASSWORD_WITH_LOGIN = "abcIvAn1!";

        @Test
        @DisplayName("логин в пароле")
        void passwordWithLoginShouldReturnContainsLoginError() {
            assertThat(violations(PASSWORD_WITH_LOGIN, "ivan")).contains(CONTAINS_LOGIN);
        }

        @Test
        @DisplayName("логина нет")
        void passwordWithoutLoginShouldNotReturnContainsLoginError() {
            assertThat(violations("Aa1!bcde", "ivan")).doesNotContain(CONTAINS_LOGIN);
        }

        @Test
        @DisplayName("пустой логин")
        void emptyLoginShouldNotReturnContainsLoginError() {
            assertThat(violations(PASSWORD_WITH_LOGIN, "")).doesNotContain(CONTAINS_LOGIN);
        }

        @Test
        @DisplayName("логин не передан")
        void missingLoginShouldNotReturnContainsLoginError() {
            assertThat(violations(PASSWORD_WITH_LOGIN)).doesNotContain(CONTAINS_LOGIN);
        }
    }

    @Nested
    @DisplayName("BLACKLISTED")
    class BlacklistTests {

        static Stream<String> blacklistedWords() {
            return Stream.of("password", "qwerty", "123456", "admin", "welcome");
        }

        @ParameterizedTest(name = "\"{0}\"")
        @MethodSource("blacklistedWords")
        @DisplayName("слово из списка")
        void blacklistedPasswordShouldReturnBlacklistedError(String word) {
            assertThat(violations(word)).contains(BLACKLISTED);
        }

        @ParameterizedTest(name = "\"{0}\" другим регистром")
        @MethodSource("blacklistedWords")
        @DisplayName("другой регистр")
        void blacklistedPasswordOtherCaseShouldReturnBlacklistedError(String word) {
            assertThat(violations(word.toUpperCase())).contains(BLACKLISTED);
        }

        @Test
        @DisplayName("подстрока без равенства")
        void wordInsidePasswordShouldNotReturnBlacklistedError() {
            assertThat(violations("password123")).doesNotContain(BLACKLISTED);
        }
    }

    @Nested
    @DisplayName("Сквозные проверки")
    class CompositeTests {

        @Test
        @DisplayName("валидный пароль")
        void validPasswordShouldBeValid() {
            PasswordPolicy.Result result = policy.check("Aa1!bcde");
            assertThat(result.isValid()).isTrue();
            assertThat(result.violations()).isEmpty();
        }

        @Test
        @DisplayName("все нарушения сразу")
        void passwordWithManyViolationsShouldReturnAllErrors() {
            assertSoftly(soft -> {
                List<PasswordPolicy.Violation> violations = violations("abc  aaa");
                soft.assertThat(violations)
                        .containsExactlyInAnyOrder(NO_DIGIT, NO_UPPER, NO_SPECIAL, HAS_WHITESPACE, REPEATED_RUN);
            });
        }
    }
}