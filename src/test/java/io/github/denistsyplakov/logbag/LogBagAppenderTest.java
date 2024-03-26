package io.github.denistsyplakov.logbag;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for logbag appender.
 */
public class LogBagAppenderTest {

    Logger log;

    @BeforeEach
    public void setup() {
        TestAppender.clean();
        log = LoggerFactory.getLogger(LogBagAppenderTest.class);
    }

    @Test
    public void testConfigurationWorks() {
        log.info("Test message");
        assertEquals(1, TestAppender.logs.size());
        assertEquals("Test message", TestAppender.logs.getFirst().getFormattedMessage());
    }
}
