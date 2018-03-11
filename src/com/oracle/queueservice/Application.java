package com.oracle.queueservice;

import com.oracle.queueservice.actors.Consumer;
import com.oracle.queueservice.actors.Producer;
import com.oracle.queueservice.service.impl.HighlyConcurrentQueue;

public class Application {
    public static void main(String[] args) {
        HighlyConcurrentQueue<Integer> queue = new HighlyConcurrentQueue<>(5);

        Producer<Integer> p1 = new Producer<>(1, 10 , queue);
        Producer<Integer> p2 = new Producer<>(2, 20 , queue);
        Producer<Integer> p3 = new Producer<>(3, 30 , queue);

        Consumer<Integer> c1 = new Consumer<>(1, queue);
        Consumer<Integer> c2 = new Consumer<>(2, queue);
        Consumer<Integer> c3 = new Consumer<>(3, queue);

        Thread tp1 = new Thread(p1);
        Thread tp2 = new Thread(p2);
        Thread tp3 = new Thread(p3);
        Thread tc1 = new Thread(c1);
        Thread tc2 = new Thread(c2);
        Thread tc3 = new Thread(c3);

        tp1.start();
        tp2.start();
        tp3.start();
        tc1.start();
        tc2.start();
        tc3.start();
    }
}
