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
 */
public class LogBagAppender extends AppenderBase<ILoggingEvent> implements AppenderAttachable<ILoggingEvent> {

    private static final Timer checkBags = new Timer("LogBagAppenderTimer", true);

    static {
        checkBags.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (LogBagAppender.class) {
                    allBags.stream()
                            .filter(bag1 -> System.currentTimeMillis() - bag1.createdAt > bag1.maxBagTTLSec * 1000L)
                            .forEach(bag -> {
                                instances.forEach(logBagAppender -> {
                                    System.out.println("open bag: " + bag.id + " baglifetime: " + (System.currentTimeMillis() - bag.createdAt));
                                    logBagAppender.openBag(bag);
                                });
                            });
                }
            }
        }, 0, 1000);
    }

    private static final ThreadLocal<LogBagContainer> bags = new ThreadLocal<>();

    private static final Set<LogBagContainer> allBags = new HashSet<>();

    private static final Set<LogBagAppender> instances = new HashSet<>();

    public LogBagAppender() {
        synchronized (LogBagAppender.class) {
            instances.add(this);
        }
    }

    AppenderAttachableImpl<ILoggingEvent> aai = new AppenderAttachableImpl<>();

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
        if (instances.size() > 1) {
            throw new IllegalStateException("Multiple instances of LogBagAppender detected, you should use start with appender name.");
        }
        if (bags.get() == null) {
            synchronized (LogBagAppender.class) {
                var appender = instances.stream().findFirst().orElseThrow();
                bags.set(new LogBagContainer(appender.maxBagTTLSec, appender.maxBagSize));
                allBags.add(bags.get());
            }
        } else {
            bags.get().nestLevel++;
        }
    }

    //TODO add remove bag to allBags

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
            synchronized (LogBagAppender.class) {
                bags.remove();
                allBags.remove(bag);
            }
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
        if (iLoggingEvent.getLevel().toInt() >= bagPassthroughLevel.toInt() * 1000 ||
                bag == null ||
                bag.nestLevel <= 0 ||
                bag.state == BagState.OPEN) {
            aai.appendLoopOnAppenders(iLoggingEvent);
        }
        //no bag no fun
        if (bag == null || bag.state == BagState.OPEN) {
            return;
        }
        //Should be stored in bag.
        if (iLoggingEvent.getLevel().toInt() < bagPassthroughLevel.toInt() * 1000 &&
                bag.nestLevel > 0) {
            synchronized (bag) {
                bag.events.add(iLoggingEvent);
            }
            if (bag.events.size() <= bag.maxBagSize) {
                return;
            }
        }
        //Should open bag.
        if (iLoggingEvent.getLevel().toInt() >= bagTriggerLevel.toInt() * 1000 ||
                bag.events.size() > bag.maxBagSize ||
                System.currentTimeMillis() - bag.createdAt > maxBagTTLSec * 1000L) {
            synchronized (LogBagAppender.class) {
                openBag(bag);
            }
        }
    }

    void openBag(LogBagContainer bag) {
        bag.events.forEach(aai::appendLoopOnAppenders);
        bag.events.clear();
        bag.state = BagState.OPEN;
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
