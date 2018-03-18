package com.oracle.queueservice;

import com.oracle.queueservice.model.ReadResponse;
import com.oracle.queueservice.service.impl.HighThroughputConcurrentQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class HighThroughputConcurrentQueueTest {

    private HighThroughputConcurrentQueue queue;

    @BeforeEach
    public void setUp() {
        queue = new HighThroughputConcurrentQueue(5);
    }

    @AfterEach
    public void tearDown() {
        queue = null;
    }

    @Test
    public void enqueueNotNullTest() {
        Assertions.assertTrue(queue.enqueue(10));
        Assertions.assertTrue(queue.enqueue(20));
        Assertions.assertTrue(queue.enqueue(30));
    }

    @Test
    public void enqueueNullTest() {
        Assertions.assertThrows(NullPointerException.class, () -> queue.enqueue(null));
    }

    @Test
    public void dequeueTest() {
        queue.enqueue(10);

        ReadResponse<Integer> response = queue.read(10);
        queue.dequeue(response.getElementId());
        Assertions.assertNull(queue.read(10));
    }

    @Test
    public void readTest() {
        queue.enqueue(10);
        queue.enqueue(20);
        queue.enqueue(30);
        queue.enqueue(40);

        Assertions.assertEquals(10, queue.read(10).getObject());
        Assertions.assertEquals(20, queue.read(10).getObject());
        Assertions.assertEquals(30, queue.read(10).getObject());
    }
}