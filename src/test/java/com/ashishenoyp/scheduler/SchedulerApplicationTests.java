package com.ashishenoyp.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SchedulerApplication.class)
@WebAppConfiguration
public class SchedulerApplicationTests {
    private static final Log logger = LogFactory.getLog(SchedulerApplicationTests.class);
    @Autowired
    private Scheduler scheduler;

    @Before
    public void setup() {
        scheduler.start();
    }

	@Test
	public void testTaskAdd() throws Exception {
        TestTask task1 = new TestTask(1, (System.currentTimeMillis()+ 1000)/1000);
        TestTask task2 = new TestTask(2, (System.currentTimeMillis()+ 1000)/1000);
        scheduler.addTask(task1);
        scheduler.addTask(task2);
        Thread.sleep(6000);
        Assert.assertTrue(task1.hasExecuted());
        Assert.assertTrue(task2.hasExecuted());
	}

    @Test
    public void testTask2() throws Exception {
        TestTask task1 = new TestTask(1, (System.currentTimeMillis()+ 1000)/1000);
        TestTask task2 = new TestTask(2, (System.currentTimeMillis()+ 8000)/1000);
        scheduler.addTask(task1);
        scheduler.addTask(task2);
        Thread.sleep(5000);
        Assert.assertTrue(task1.hasExecuted());
        Assert.assertTrue(!task2.hasExecuted());
    }

    @Test
    public void testTask3() throws Exception {
        TestTask task1 = new TestTask(4, (System.currentTimeMillis()+ 1000)/1000);
        TestTask task2 = new TestTask(5, (System.currentTimeMillis()+ 6000)/1000);
        TestTask task3 = new TestTask(6, (System.currentTimeMillis()+ 8000)/1000);
        scheduler.addTask(task1);
        scheduler.addTask(task2);
        scheduler.addTask(task3);
        Thread.sleep(5000);
        Assert.assertTrue(task1.hasExecuted());
        Assert.assertTrue(!task2.hasExecuted());
        Assert.assertTrue(!task3.hasExecuted());
    }

    @After
    public void shutdown() {
        scheduler.stop();
    }
}
