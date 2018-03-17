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
import java.util.stream.IntStream;

public class Application {
    public static void main(String[] args) {
        IConcurrentQueue<Integer> queue = new HighThroughputConcurrentQueue<>();
        Set<Callable<Integer>> workers = new HashSet<>();

        IntStream.range(0, Constants.THREADS)
                .forEach(i -> {
                    workers.add(new Producer<>(i, i*10, queue));
                    workers.add(new Consumer<>(i, queue, Constants.TIMEOUT));
                });

        ExecutorService executor = Executors.newFixedThreadPool(Constants.THREAD_POOL);

        try {
            executor.invokeAll(workers);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
}
