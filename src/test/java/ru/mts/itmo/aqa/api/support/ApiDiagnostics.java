package ru.mts.itmo.aqa.api.support;

import java.util.Optional;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApiDiagnostics implements TestWatcher, BeforeEachCallback {

    private static final Logger LOGGER = LogManager.getLogger(ApiDiagnostics.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        SandboxSessionFilter.reset();
        ExchangeRecorderFilter.reset();
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        LastExchange exchange = ExchangeRecorderFilter.last();
        String header = "Test failed: " + context.getDisplayName();
        if (exchange == null) {
            LOGGER.error("{}{}no HTTP exchange was recorded before the failure",
                    header, System.lineSeparator());
            return;
        }
        LOGGER.error("{}{}{}", header, System.lineSeparator(), exchange.render());
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        logOutcome("aborted", context, cause);
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        LOGGER.info("Test disabled: {} ({})", context.getDisplayName(),
                reason.map(String::valueOf).orElse("no reason reported"));
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        LOGGER.info("Test passed: {}", context.getDisplayName());
    }

    private void logOutcome(String outcome, ExtensionContext context, Throwable cause) {
        LOGGER.warn("Test {}: {} ({})",
                outcome,
                context.getDisplayName(),
                cause == null ? "no reason reported" : cause.toString());
    }
}
