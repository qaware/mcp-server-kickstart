package com.qaware.mcp.tools.knowledge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Thread-safe Recycler
 */
public final class Recycler<T> {

    private final Deque<T> pool = new ArrayDeque<>();

    private final Supplier<T> factory;
    private final Consumer<T> cleanup; // can be null


    public Recycler(Supplier<T> aFactory, Consumer<T> aCleanup) {
        factory = aFactory;
        cleanup = aCleanup;
    }


    public T get() {
        T object;

        synchronized (pool) {
            object = pool.poll();
        }

        return object == null ? factory.get() : object;
    }


    public void recycle(T object) {
        if (object == null) return;
        if (cleanup != null) cleanup.accept(object);

        synchronized (pool) {
            pool.push(object);
        }
    }


    public List<T> drain() {
        synchronized (pool) {
            List<T> objects = new ArrayList<>(size());

            objects.addAll(pool);
            pool.clear();

            return objects;
        }
    }


    public int size() {
        synchronized (pool) {
            return pool.size();
        }
    }

}
