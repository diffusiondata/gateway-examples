package com.diffusiondata.example.adapter.human;

import java.util.ArrayList;
import java.util.function.Consumer;

public class EventHandlers<T> extends ArrayList<Consumer<T>> {
    public EventHandlers(int initialCapacity) {
        super(initialCapacity);
    }

    public EventHandlers() {
        this(1);
    }

    void dispatch(T event) {
        this.forEach(l -> l.accept(event));
    }
}
