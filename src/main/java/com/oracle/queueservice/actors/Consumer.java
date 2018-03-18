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
        System.out.println("Call " + Thread.currentThread().getName());
        ReadResponse response = queue.read(timeout);
        if (response != null) {
            if (id % 5 == 0) {
                try {
                    System.out.println("Try " + Thread.currentThread().getName());
                    Thread.sleep(2000);
                    queue.dequeue(response.getElementId());
                } catch (InterruptedException e) {
                    System.out.println("Catch " + Thread.currentThread().getName());
                    e.printStackTrace();
                }
            }
//            System.out.println("Consumer " + id + " consuming data " + response.getObject().toString());
        }
        else
            System.out.println("Consumer " + id + ", nothing to Consume ");

        return (T) response.getObject();
    }
}
