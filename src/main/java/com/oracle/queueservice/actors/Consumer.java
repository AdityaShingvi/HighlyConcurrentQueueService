package com.oracle.queueservice.actors;

import com.oracle.queueservice.model.ReadResponse;
import com.oracle.queueservice.service.IConcurrentQueue;
import lombok.AllArgsConstructor;

import java.util.concurrent.Callable;

@AllArgsConstructor
public class Consumer<T> implements Callable<T> {

    private int id;
    private IConcurrentQueue<T> queue;
    private int timeout;

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return object of type T that was consumed from the queue.
     */
    @Override
    public T call() {
        long startTime = System.currentTimeMillis();
        // Read element from queue
        ReadResponse response = queue.read(timeout);
        long endTime = System.currentTimeMillis();
        System.out.println("Read Latency: " + (endTime - startTime));
        if (response != null) {
            // Dequeue elements if they are divisible by 5
            if (id % 5 == 0) {
                try {
                    Thread.sleep(2000);
                    long startTimeDq = System.currentTimeMillis();
                    // Dequeue element from queue
                    queue.dequeue(response.getElementId());
                    long endTimeDq = System.currentTimeMillis();
                    System.out.println("Dequeue Latency: " + (endTimeDq - startTimeDq));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        else
            System.out.println("Consumer " + id + ", nothing to Consume ");

        return (T) response.getObject();
    }
}
