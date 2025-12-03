package org.orchestrator.config;

import java.util.HashMap;
import java.util.Map;

public class WorkerConfig {
    
    private static Map<String, String> workerPool = new HashMap<>();
    private static int count = 0;

    public static void addWorker(String port) {
        count++;
        String workerId = "W" + count;
        workerPool.put(workerId, "http://localhost:" + port);
    }

    public static Map<String, String> getWorkerPool() {
        return workerPool;
    }
}
