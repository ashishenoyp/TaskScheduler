package com.ashishenoyp.scheduler;

import java.util.concurrent.Callable;

public interface Task extends Callable {
    public int getTaskID();

    public long getScheduleTime();

    public boolean isPeriodic();

    @Override
    Object call() throws Exception;

    public boolean hasExecuted();
}
