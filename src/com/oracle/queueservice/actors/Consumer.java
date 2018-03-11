package com.oracle.queueservice.actors;

import com.oracle.queueservice.model.ReadResponse;
import com.oracle.queueservice.service.impl.HighlyConcurrentQueue;

public class Consumer<T> implements Runnable {

    private int id;
    private HighlyConcurrentQueue<T> queue;

    public Consumer(final int id, final HighlyConcurrentQueue queue) {
        this.id = id;
        this.queue = queue;
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
        ReadResponse response = queue.read();
        System.out.println("Consumer " + id + " consuming data...Status: " + response.getObject().toString());
        queue.dequeue();
    }
}
