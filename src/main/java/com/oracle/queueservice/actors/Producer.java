package com.oracle.queueservice.actors;

import com.oracle.queueservice.service.IConcurrentQueue;
import lombok.AllArgsConstructor;

import java.util.concurrent.Callable;

@AllArgsConstructor
public class Producer<T> implements Callable<T> {

    private int id;
    private IConcurrentQueue<T> queue;
    private T element;

    /**
     * Produces element to the queue and returns it.
     *
     * @return the element to be produced.
     */
    @Override
    public T call() {
        long startTime = System.currentTimeMillis();
        // Enqueue element to queue
        queue.enqueue(element);
        long endTime = System.currentTimeMillis();
        System.out.println("Enqueue Latency: " + (endTime - startTime));
        return element;
    }
}
