package io.github.denistsyplakov.logbag;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.List;

public class TestAppender extends AppenderBase<ILoggingEvent> {

    static List<ILoggingEvent> logs = new ArrayList<>();

    public static void clean() {
        logs.clear();
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        logs.add(iLoggingEvent);
    }
}
