package com.ashishenoyp.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SchedulerImpl implements Scheduler {
    private static final Log logger = LogFactory.getLog(SchedulerImpl.class);

    private ExecutorService executorService;
    private ScheduledExecutorService eventLoop;
    private PriorityQueue<Task> taskQueue;

    private class EventLoop implements Runnable {
        @Override
        public void run() {
            if (taskQueue.isEmpty()) {
                return;
            }
            logger.debug("Inside Eventloop run()");
            Task task = taskQueue.peek();
            while (task != null && task.getScheduleTime() <= (System.currentTimeMillis()/1000)) {
                executorService.submit(taskQueue.poll());
                task = taskQueue.peek();
            }
            logger.debug("Leaving event loop");
        }
    }

    @Override
    public void start() {
        eventLoop = Executors.newScheduledThreadPool(1);
        executorService = Executors.newFixedThreadPool(2);
        taskQueue = new PriorityQueue<>();
        eventLoop.scheduleAtFixedRate(new EventLoop(), 0, 1, TimeUnit.SECONDS);
        logger.info("Scheduler started");
    }

    @Override
    public void stop() {
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
    public synchronized void addTask(Task task) {
        taskQueue.add(task);
    }
}
