package io.github.denistsyplakov.logbag;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;

import java.util.Iterator;

/**
 * LogBag Adapter for logback framework.
 *
 */
public class LogBagAppender extends AppenderBase<ILoggingEvent> implements AppenderAttachable<ILoggingEvent> {

    AppenderAttachableImpl<ILoggingEvent> aai = new AppenderAttachableImpl<>();

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        aai.appendLoopOnAppenders(iLoggingEvent);
    }

    @Override
    public void addAppender(Appender<ILoggingEvent> appender) {
        aai.addAppender(appender);
    }

    @Override
    public Iterator<Appender<ILoggingEvent>> iteratorForAppenders() {
        return aai.iteratorForAppenders();
    }

    @Override
    public Appender<ILoggingEvent> getAppender(String s) {
        return aai.getAppender(s);
    }

    @Override
    public boolean isAttached(Appender<ILoggingEvent> appender) {
        return aai.isAttached(appender);
    }

    @Override
    public void detachAndStopAllAppenders() {
        aai.detachAndStopAllAppenders();
    }

    @Override
    public boolean detachAppender(Appender<ILoggingEvent> appender) {
        return aai.detachAppender(appender);
    }

    @Override
    public boolean detachAppender(String s) {
        return aai.detachAppender(s);
    }
}
