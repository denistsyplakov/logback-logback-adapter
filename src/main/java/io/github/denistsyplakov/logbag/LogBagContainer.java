package io.github.denistsyplakov.logbag;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.UUID;

enum BagState{
    OPEN,
    CLOSED
}

class LogBagContainer {

    public UUID id = UUID.randomUUID();

    public BagState state = BagState.CLOSED;

    public long createdAt = System.currentTimeMillis();

    public int nestLevel = 1;

    public ArrayList<ILoggingEvent> events = new ArrayList<>();

    public long maxBagTTLSec;

    public long maxBagSize;
}
