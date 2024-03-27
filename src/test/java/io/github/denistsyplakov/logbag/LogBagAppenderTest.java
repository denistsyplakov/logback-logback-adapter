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

    @Test
    public void testNestingLevel() {
        LogBagAppender.startBag();
        try{
            assertEquals(1, LogBagAppender.getNestingLevel());
            LogBagAppender.startBag();
            try{
                assertEquals(2, LogBagAppender.getNestingLevel());
            }finally {
                LogBagAppender.finishBag();
            }
            assertEquals(1, LogBagAppender.getNestingLevel());
        }finally {
            LogBagAppender.finishBag();
        }
        assertEquals(0, LogBagAppender.getNestingLevel());
    }

    @Test
    public void testBasicNoWarnings() {
        LogBagAppender.startBag();
        try{
            log.info("Test message1");
            log.debug("Test message2");
            assertEquals(1, TestAppender.logs.size());
            assertEquals("Test message1", TestAppender.logs.getFirst().getFormattedMessage());
        }finally {
            LogBagAppender.finishBag();
        }
        assertEquals(0, LogBagAppender.getNestingLevel());
    }

    @Test
    public void testBasicWithWarnings() {
        LogBagAppender.startBag();
        try{
            assertEquals(1, LogBagAppender.getNestingLevel());
            log.info("Test message1");
            log.debug("Test message2");
            log.warn("Test message3");
            log.debug("Test message4");
            assertEquals(4, TestAppender.logs.size());
            assertEquals("Test message1", TestAppender.logs.getFirst().getFormattedMessage());
            assertEquals("Test message3", TestAppender.logs.get(1).getFormattedMessage());
            assertEquals("Test message2", TestAppender.logs.get(2).getFormattedMessage());
            assertEquals("Test message4", TestAppender.logs.get(3).getFormattedMessage());
        }finally {
            LogBagAppender.finishBag();
        }
        assertEquals(0, LogBagAppender.getNestingLevel());
    }

}
