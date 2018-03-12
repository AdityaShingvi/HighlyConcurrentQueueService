package com.oracle.queueservice;

import com.oracle.queueservice.actors.Consumer;
import com.oracle.queueservice.actors.Producer;
import com.oracle.queueservice.service.IConcurrentQueue;
import com.oracle.queueservice.service.impl.HighThroughputConcurrentQueue;
import com.oracle.queueservice.util.Constants;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application {
    public static void main(String[] args) {
        IConcurrentQueue<Integer> queue = new HighThroughputConcurrentQueue<>();
        Set<Callable<Integer>> producers = new HashSet<>();
        Set<Callable<Integer>> consumers = new HashSet<>();

        for (int i = 0; i < Constants.THREADS; i++) {
            producers.add(new Producer<>(i, i * 10, queue));
            consumers.add(new Consumer<>(i, queue, 5000));
        }

        ExecutorService executorP = Executors.newFixedThreadPool(Constants.THREAD_POOL);
        ExecutorService executorC = Executors.newFixedThreadPool(Constants.THREAD_POOL);
        try {
            executorP.invokeAll(producers);
            executorC.invokeAll(consumers);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorP.shutdown();
            executorC.shutdown();
        }
    }
}
