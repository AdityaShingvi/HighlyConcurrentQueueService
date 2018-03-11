package com.oracle.queueservice.actors;

import com.oracle.queueservice.service.impl.HighlyConcurrentQueue;

public class Producer<T> implements Runnable {

    private int id;
    private HighlyConcurrentQueue<T> queue;
    private T element;

    public Producer(int id, T element, HighlyConcurrentQueue<T> queue) {
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
    public void run() {
        System.out.println("Producer " + id + " producing data...Status: " + queue.enqueue(element));
    }
}
