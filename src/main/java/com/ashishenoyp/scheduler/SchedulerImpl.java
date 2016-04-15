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
    private ExecutorService eventLoop;
    private Future<?> eventLoopFuture;
    private PriorityQueue<Task> taskQueue;
    private List<Future<?>> futures;

    private class EventLoop implements Runnable {
        private long defaultSleepMs = 10000000;

        @Override
        public void run() {
            while (true) {
                logger.info("Inside Eventloop run()");
                if (taskQueue.isEmpty()) {
                    try {
                        logger.info("Sleeping for(ms): " + defaultSleepMs);
                        Thread.currentThread().sleep(defaultSleepMs);
                    } catch (InterruptedException ex) {
                        // If task queue is empty at this point, and we were interrupted
                        // during sleep, then its a signal to shutdown:
                        if (taskQueue.isEmpty()) {
                            break;
                        }
                    }
                }

                synchronized (taskQueue) {
                    // sleep up until the next to be executed task:
                    Task task = taskQueue.peek();
                    try {
                        long currTimeSec = System.currentTimeMillis()/1000;
                        long sleepFor = (task.getScheduleTime() - currTimeSec);
                        if (sleepFor > 0) {
                            logger.info("Sleeping for(seconds): " + (task.getScheduleTime() - currTimeSec));
                            Thread.currentThread().sleep(sleepFor*1000);
                        }
                    } catch (InterruptedException ex) {
                        // A newly added task has to be executed sooner; determine the new
                        // to be slept until time:
                        continue;
                    }

                    futures.add(executorService.submit(taskQueue.poll()));
                }
            }
            logger.debug("Leaving event loop");
        }
    }

    @Override
    public void start() {
        eventLoop = Executors.newFixedThreadPool(1);
        executorService = Executors.newFixedThreadPool(2);
        taskQueue = new PriorityQueue<>();
        futures = new ArrayList<>();
        eventLoopFuture = eventLoop.submit(new EventLoop());
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
            logger.info("Adding task: " + task.getTaskID());

            if (taskQueue.isEmpty() ||
                    (task.getScheduleTime() < taskQueue.peek().getScheduleTime())) {
                logger.info("Interrupting eventLoop !");
                taskQueue.add(task);
                // Interrupt the sleeping thread:
                eventLoopFuture.cancel(true);
            } else {
                taskQueue.add(task);
            }
        }
    }
}
