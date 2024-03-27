package io.github.denistsyplakov.logbag;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;

enum BagState{
    OPEN,
    CLOSED
}

class LogBagContainer {

    public BagState state = BagState.CLOSED;

    public long createdAt = System.currentTimeMillis();

    public int nestLevel = 1;

    public ArrayList<ILoggingEvent> events = new ArrayList<>();

}
