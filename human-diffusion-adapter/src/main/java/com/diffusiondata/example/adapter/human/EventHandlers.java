package com.diffusiondata.example.adapter.human;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventHandlers<T> {
    private List<Consumer<T>> consumers = new ArrayList<>();

    void dispatch(T event) {
        consumers.forEach(l -> l.accept(event));
    }

    public void add(Consumer<T> handler) {
        consumers.add(handler);
    }
}
