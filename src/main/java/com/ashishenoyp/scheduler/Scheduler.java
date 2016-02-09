package com.ashishenoyp.scheduler;

public interface Scheduler {
    public void start();

    public void stop();

    public void addTask(Task task);
}
