package com.qaware.mcp.tools.knowledge;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RecyclerTest {

    @Test
    public void test() {
        AtomicInteger atomicInteger = new AtomicInteger();

        Recycler<Integer> recycler = new Recycler<>(() -> atomicInteger.getAndIncrement(),
                i -> { if (i == 13) throw new RuntimeException("unlucky number"); });

        assertEquals(0, recycler.get());
        assertEquals(1, recycler.get());

        recycler.recycle(0);
        recycler.recycle(1);
        recycler.recycle(2);

        assertEquals(3, recycler.size());

        assertEquals(2, recycler.get());

        recycler.recycle(null); // ignored

        assertEquals("[1, 0]", recycler.drain().toString());

        assertThrows(RuntimeException.class, () -> recycler.recycle(13));
    }

}
