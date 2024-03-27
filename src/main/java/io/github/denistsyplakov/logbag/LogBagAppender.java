package io.github.denistsyplakov.logbag;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import org.slf4j.event.Level;

import java.util.*;

/**
 * LogBag Adapter for logback framework.
 *
 * Important note: Bags should be closed properly. Otherwise, there will be resource leakage.
 */
public class LogBagAppender extends AppenderBase<ILoggingEvent> implements AppenderAttachable<ILoggingEvent> {

    AppenderAttachableImpl<ILoggingEvent> aai = new AppenderAttachableImpl<>();

    private static final ThreadLocal<LogBagContainer> bags = new ThreadLocal<>();

    private Level bagTriggerLevel = Level.WARN;

    private Level bagPassthroughLevel = Level.INFO;

    private int maxBagSize = 100;

    private int maxBagTTLSec = 60;

    public String getBagPassthroughLevel() {
        return bagPassthroughLevel.toString();
    }

    public void setBagPassthroughLevel(String bagPassthroughLevel) {
        this.bagPassthroughLevel = Level.valueOf(bagPassthroughLevel);
    }

    public int getMaxBagTTLSec() {
        return maxBagTTLSec;
    }

    public void setMaxBagTTLSec(int maxBagTTLSec) {
        this.maxBagTTLSec = maxBagTTLSec;
    }

    public int getMaxBagSize() {
        return maxBagSize;
    }

    public void setMaxBagSize(int maxBagSize) {
        this.maxBagSize = maxBagSize;
    }

    public String getBagTriggerLevel() {
        return bagTriggerLevel.toString();
    }

    public void setBagTriggerLevel(String bagTriggerLevel) {
        this.bagTriggerLevel = Level.valueOf(bagTriggerLevel);
    }

    public static void startBag() {
        if (bags.get() == null) {
            bags.set(new LogBagContainer());
        } else {
            bags.get().nestLevel++;
        }
    }

    public static void finishBag() {
        LogBagContainer bag = bags.get();
        if (bag == null) {
            System.err.println("No bag to finish");
            return;
        }
        if (bag.nestLevel <= 0) {
            System.err.println("Nest level non positive, bug in appender code.");
            return;
        }
        bag.nestLevel--;
        if (bag.nestLevel == 0) {
            bags.remove();
        }
    }

    public static int getNestingLevel() {
        LogBagContainer bag = bags.get();
        return bag == null ? 0 : bag.nestLevel;
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        LogBagContainer bag = bags.get();
        //Should pass through.
        if (iLoggingEvent.getLevel().toInt() >= bagPassthroughLevel.toInt()*1000 ||
                bag == null ||
                bag.nestLevel <= 0 ||
                bag.state == BagState.OPEN) {
            aai.appendLoopOnAppenders(iLoggingEvent);
        }
        //no bag no fun
        if (bag == null||bag.state == BagState.OPEN){
            return;
        }
        //Should be stored in bag.
        if (iLoggingEvent.getLevel().toInt() < bagPassthroughLevel.toInt()*1000 &&
                bag.nestLevel > 0 ) {
            bag.events.add(iLoggingEvent);
            return;
        }
        //Should open bag.
        if (iLoggingEvent.getLevel().toInt() >= bagTriggerLevel.toInt()*1000 ||
                System.currentTimeMillis() - bag.createdAt > maxBagTTLSec * 1000L) {
            bag.events.forEach(aai::appendLoopOnAppenders);
            bag.state = BagState.OPEN;
        }
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
