package com.ashishenoyp.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestTask implements Task, Comparable {
    private static final Log logger = LogFactory.getLog(TestTask.class);
    private long scheduleAt;
    private int id;
    private boolean executed = false;

    public TestTask(int taskId, long scheduleAt) {
        this.scheduleAt = scheduleAt;
        this.id = taskId;
    }

    @Override
    public int getTaskID() {
        return id;
    }

    @Override
    public long getScheduleTime() {
        // return 1 second from now:
        return scheduleAt;
    }

    @Override
    public Object call() {
        logger.info("Task ID: " + id + " called at: " + System.currentTimeMillis());
        if (Thread.currentThread().isInterrupted()) {
            logger.info("Task ID: " + id + " interrupted at: " + System.currentTimeMillis());
            return null;
        }
        executed = true;
        return null;
    }

    @Override
    public boolean isPeriodic() {
        return false;
    }

    @Override
    public int compareTo(Object o) {
        Task task = (Task) o;
        if (this.getScheduleTime() < task.getScheduleTime()) {
            return -1;
        } else if (this.getScheduleTime() > task.getScheduleTime()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean hasExecuted() {
        return executed;
    }
}
