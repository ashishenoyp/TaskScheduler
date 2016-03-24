package com.ashishenoyp.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;

@Component
public class SchedulerImpl implements Scheduler {
    private static final Log logger = LogFactory.getLog(SchedulerImpl.class);

    private ExecutorService executorService;
    private ScheduledExecutorService eventLoop;
    private PriorityQueue<Task> taskQueue;
    private List<Future<?>> futures;

    private class EventLoop implements Runnable {
        @Override
        public void run() {
            synchronized (taskQueue) {
                if (taskQueue.isEmpty()) {
                    return;
                }
                logger.debug("Inside Eventloop run()");
                Task task = taskQueue.peek();
                while (task != null && task.getScheduleTime() <= (System.currentTimeMillis() / 1000)) {
                    futures.add(executorService.submit(taskQueue.poll()));
                    task = taskQueue.peek();
                }
            }
            logger.debug("Leaving event loop");
        }
    }

    @Override
    public void start() {
        eventLoop = Executors.newScheduledThreadPool(1);
        executorService = Executors.newFixedThreadPool(2);
        taskQueue = new PriorityQueue<>();
        futures = new ArrayList<>();
        eventLoop.scheduleAtFixedRate(new EventLoop(), 0, 1, TimeUnit.SECONDS);
        logger.info("Scheduler started");
    }

    @Override
    public void stop() {
        futures.forEach(f -> f.cancel(true));
        futures.clear();

        eventLoop.shutdownNow();
        executorService.shutdownNow();
        try {
            if (!executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS) ||
                    !eventLoop.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)) {
                taskQueue.clear();
                return;
            }
        } catch (InterruptedException ex) {
            logger.info("Caught interrupted exception");
        }
        logger.info("Shutting down scheduler");
    }

    @Override
    public void addTask(Task task) {
        synchronized (taskQueue) {
            taskQueue.add(task);
        }
    }
}
