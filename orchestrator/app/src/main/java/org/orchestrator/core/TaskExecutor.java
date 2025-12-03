package org.orchestrator.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.orchestrator.utils.HttpUtils;

/**
 * TaskExecutor coordinates reads, shuffles and writes by calling worker endpoints.
 *
 * Assumptions:
 * - `workers` is a map from workerId (e.g. "W1") to worker host (e.g. "http://localhost:9001").
 * - This is class is the "brain of your query execution. This executor is a singleton, only one query can run at a time.
 * - Change it for scalability.
 */
public enum TaskExecutor {
    INSTANCE;

    // Queue of BLOB files
    private List<String> dataQueue;

    // Map of workerId to worker host
    private Map<String, String> workers;

    // Active worker Pool
    private List<String> activeWorkers = new ArrayList<>();

    // Completed worker Pool
    private List<String> workerCompletePool = new ArrayList<>();
    private AtomicBoolean running = new AtomicBoolean(false); // Use this to ensure query deduplication

    private static Lock lock = new ReentrantLock();

    public void startTask(TaskPlan plan) {
        if (running.get()) {
            throw new IllegalStateException("Query is already running");
        }
        this.dataQueue = new ArrayList<>(plan.getDataNodes() == null ? List.of() : plan.getDataNodes());
        this.workers = plan.getworkerPool();
        if (this.workers == null) {
            throw new IllegalArgumentException("worker pool cannot be null");
        }
        if (this.activeWorkers == null) this.activeWorkers = new ArrayList<>();
        this.workerCompletePool.clear();
        this.running.set(true);

        // START initial reads across workers
        for (Map.Entry<String, String> entry : workers.entrySet()) {
            String workerId = entry.getKey();
            invokeRead(workerId);
        }
    }

    /**
     * Tell a worker to read a data node by calling its /invokeRead endpoint.
     */
    private void invokeRead(String workerId) {
        lock.lock();
        try {
            if (dataQueue != null && dataQueue.size() > 0) {
                // pop one data node and mark worker active
                String blobName = dataQueue.remove(dataQueue.size() - 1);
                activeWorkers.add(workerId);
                System.out.println("Added active worker: " + workerId + " and activeWorker list is " + activeWorkers.toString());

                String host = workers.get(workerId);
                String url = HttpUtils.buildUrl(host, "/invokeRead/" + blobName);
                System.out.println("invokeRead -> " + url + " for data=" + blobName + " worker=" + workerId);
                HttpUtils.callGet(url);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Decide whether to invoke shuffle between completed workers or to trigger a write.
     */
    private void invokeShuffleOrComplete() {
        // If at least two workers have completed, ask one to shuffle with the other
        if (workerCompletePool.size() >= 2) {
            String w1 = workerCompletePool.remove(workerCompletePool.size() - 1);
            String w2 = workerCompletePool.remove(workerCompletePool.size() - 1);

            String w1Host = workers.get(w1);
            String url = HttpUtils.buildUrl(w1Host, "/invokeShuffle/" + w2);
            System.out.println("invokeShuffle -> " + url + " (w1=" + w1 + ", w2=" + w2 + ")");
            HttpUtils.callGet(url);

            // mark w1 as active again (it will be working on the shuffle). it will get the data from w2 and merge it
            activeWorkers.add(w1);

        } else if ((dataQueue == null || dataQueue.size() == 0) && (activeWorkers == null || activeWorkers.size() == 0)
                && workerCompletePool.size() > 0) {
            // No more data and nobody active: pick the completed worker to write results
            String worker = workerCompletePool.get(0);
            String url = HttpUtils.buildUrl(workers.get(worker), "/writeResult");
            this.running.set(false);
            
            System.out.println("invokeWrite -> " + url + " (worker=" + worker + ")");
            HttpUtils.callGet(url);
        }
    }

    /**
     * Called by external code (e.g. controller) when a worker reports task completion.
     */
    public void onTaskComplete(String worker) {
        lock.lock();
        try {
            if (activeWorkers != null) {
                // remove by object if present
                activeWorkers.remove(worker);
                System.out.println("Process complete for: " + worker + " and activeWorker list is " + activeWorkers.toString());
            }

            if (dataQueue != null && dataQueue.size() > 0) {
                invokeRead(worker);
            } else {
                workerCompletePool.add(worker);
                // attempt to schedule shuffle or final write
                invokeShuffleOrComplete();
            }
        } finally {
            lock.unlock();
        }
    }

}
