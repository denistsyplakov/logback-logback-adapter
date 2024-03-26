package io.github.denistsyplakov.logbag;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class LogBagAppenderTest
{

    Logger log;

    @BeforeEach
    public void setup() {
        log = LoggerFactory.getLogger(LogBagAppenderTest.class);
    }

    @Test
    public void shouldAnswerWithTrue()
    {
        log.info("Test message");
        assertTrue( true );
    }
}
