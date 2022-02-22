package io.openliberty.spacerover.game;

public interface GameEventListener {
    void update(GameEvent eventType, long value);
}