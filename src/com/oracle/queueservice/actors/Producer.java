package com.oracle.queueservice.actors;

import com.oracle.queueservice.service.IConcurrentQueue;

import java.util.concurrent.Callable;

public class Producer<T> implements Callable<T> {

    private int id;
    private IConcurrentQueue<T> queue;
    private T element;

    public Producer(final int id, final T element, final IConcurrentQueue<T> queue) {
        this.id = id;
        this.queue = queue;
        this.element = element;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public T call() {
        System.out.println("Producer " + Thread.currentThread().getName());
        queue.enqueue(element);
//        System.out.println("Producer " + id + " producing data...Data: " + element);
        return element;
    }
}
