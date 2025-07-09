package com.bsi.md.agent.engine.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步任务执行线程池
 * @author fish
 */
public class AgAsynchronousApiPool {
	public static ExecutorService apiPool = Executors.newFixedThreadPool(5);
	public static ExecutorService singlePool = Executors.newSingleThreadExecutor();
	private static final ExecutorService cachedPool = Executors.newCachedThreadPool();

	public static void commit(Runnable task) {
		apiPool.submit(task);
	}

	public static void commitSingle(Runnable task) {
		singlePool.submit(task);
	}

	public static void commitCached(Runnable task) {
		cachedPool.submit(task);
	}
}
