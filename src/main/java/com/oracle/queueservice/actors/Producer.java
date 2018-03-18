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
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return the element to be produced if success.
     */
    @Override
    public T call() {
        System.out.println("Producer " + Thread.currentThread().getName());
        queue.enqueue(element);
//        System.out.println("Producer " + id + " producing data...Data: " + element);
        return element;
    }
}
