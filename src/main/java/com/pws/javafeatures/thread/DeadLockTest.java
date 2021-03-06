package com.pws.javafeatures.thread;

import com.pws.javafeatures.util.PrintUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 死锁
 *
 * @author panws
 * @since 2017-07-26
 */
public class DeadLockTest {

	public static void main(String[] args) {

		ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
		Runnable dlcheck = () -> {
			long[] threadIds = mbean.findDeadlockedThreads();
			if (threadIds != null) {
				ThreadInfo[] threadInfos = mbean.getThreadInfo(threadIds);
				PrintUtil.println("Detected deadlock threads:");
				for (ThreadInfo threadInfo : threadInfos) {
					PrintUtil.println(threadInfo.getThreadName());
				}
			}
		};

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(dlcheck, 5L, 10L, TimeUnit.SECONDS);

		Object lockA = new Object();
		Object lockB = new Object();

		ThreadA threadA = new ThreadA(lockA, lockB);
		ThreadB threadB = new ThreadB(lockA, lockB);

		threadA.start();
		threadB.start();

	}

}

class ThreadA extends Thread {

	private final Object lockA;

	private final Object lockB;

	ThreadA(Object lockA, Object lockB) {
		this.lockA = lockA;
		this.lockB = lockB;
	}

	@Override
	public void run() {
		synchronized (lockA) {
			PrintUtil.println(this.getClass().getName() + " holds lockA.");
			try {
				sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (lockB) {
				PrintUtil.println(this.getClass().getName() + " holds lockB.");
			}
			PrintUtil.println(this.getClass().getName() + " releases lockB.");
		}
		PrintUtil.println(this.getClass().getName() + " releases lockA.");
	}
}

class ThreadB extends Thread {

	private final Object lockA;

	private final Object lockB;

	ThreadB(Object lockA, Object lockB) {
		this.lockA = lockA;
		this.lockB = lockB;
	}

	@Override
	public void run() {
		synchronized (lockB) {
			PrintUtil.println(this.getClass().getName() + " holds lockB.");
			try {
				sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (lockA) {
				PrintUtil.println(this.getClass().getName() + " holds lockA.");
			}
			PrintUtil.println(this.getClass().getName() + " releases lockA.");
		}
		PrintUtil.println(this.getClass().getName() + " releases lockB.");
	}
}
