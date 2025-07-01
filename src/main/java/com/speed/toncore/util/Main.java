package com.speed.toncore.util;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

	private static final Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private static final int THREAD_COUNT = 300;
	private static final int IDS_PER_THREAD = 100_000;

	public static void main(String[] args) throws InterruptedException {
		long startTime = System.currentTimeMillis();

		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
		AtomicInteger duplicateCount = new AtomicInteger(0);

		for (int i = 0; i < THREAD_COUNT; i++) {
			executorService.submit(() -> {
				for (int j = 0; j < IDS_PER_THREAD; j++) {
					String identifier = "sweep_" + System.currentTimeMillis() + "_" + Long.toHexString(ThreadLocalRandom.current().nextLong());

					if (!set.add(identifier)) {
						duplicateCount.incrementAndGet();
						System.out.println("Duplicate found: " + identifier);
					}
				}
				latch.countDown();
			});
		}

		latch.await();
		executorService.shutdown();

		long endTime = System.currentTimeMillis();
		int totalCount = THREAD_COUNT * IDS_PER_THREAD;
		double duplicateRatio = (duplicateCount.get() * 100.0) / totalCount;

		System.out.println("\n=== Stats ===");
		System.out.println("Start Time   : " + startTime + " ms");
		System.out.println("End Time     : " + endTime + " ms");
		System.out.println("Total Time   : " + (endTime - startTime) + " ms");
		System.out.println("Total IDs    : " + totalCount);
		System.out.println("Duplicates   : " + duplicateCount.get());
		System.out.println("Duplicate %  : " + String.format("%.6f", duplicateRatio) + " %");
	}
}
