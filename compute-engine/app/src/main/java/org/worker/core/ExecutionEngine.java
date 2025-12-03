package org.worker.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.worker.model.Row;
import org.worker.runtime.RuntimeData;

public class ExecutionEngine {
    
    private ExecutorService executorService;
    private List<Row> rows;
    
    private static final int CHUNK_SIZE = 10000;

    public ExecutionEngine(List<Row> rows) {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.rows = rows;
    }

    public void invokeTask() {
        try {
            System.out.println("Starting task invocation...");
            List<List<Row>> chunks = new ArrayList<>();
            System.out.println("Splitting rows into chunks... got rows: " + rows.size());
            for (int i = 0; i < rows.size(); i += CHUNK_SIZE) {
                chunks.add(rows.subList(i, Math.min(i + CHUNK_SIZE, rows.size())));
            }

            List<Future<?>> futures = new ArrayList<>();
            for (List<Row> chunk : chunks) {
                futures.add(executorService.submit(() -> computeChunk(chunk)));
            }
            
            // blocks until task is done, can use countDownLatch for better control maybe
            for (Future<?> f : futures) {
                f.get();       
            }
            System.out.println("All tasks completed.");
            System.out.println(RuntimeData.INSTANCE.getTransientVolatileTable().getGroupIdVsRow().toString());
            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void computeChunk(List<Row> chunk) {
        for (Row row : chunk) {
            // Process each row
            RuntimeData.INSTANCE.getTransientVolatileTable()
                .fetchRowByGroupId(row.year())
                .updateRow(row.score());
        }
    }

}
