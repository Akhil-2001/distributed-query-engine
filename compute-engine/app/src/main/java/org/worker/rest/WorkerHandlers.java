package org.worker.rest;

import java.util.List;

import org.worker.core.DataParser;
import org.worker.core.ExecutionEngine;
import org.worker.model.Row;
import org.worker.model.TransientVolatileTable;
import org.worker.utils.HttpUtils;
import org.worker.utils.PathUtils;

import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.nio.charset.StandardCharsets;

import org.worker.runtime.HostConfig;
import org.worker.runtime.RuntimeData;

/**
 * Small holder for worker-related HTTP handlers. These are offloaded from the
 * controller to keep routing thin and isolate handler logic for easier testing.
 */
public class WorkerHandlers {
    private final java.util.concurrent.ExecutorService executor;
    private final String orchestratorUrl;

    public WorkerHandlers(java.util.concurrent.ExecutorService executor, String orchestratorUrl) {
        this.executor = executor;
        this.orchestratorUrl = orchestratorUrl;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void invokeRead(Context ctx) {
        String blobId = PathUtils.decodePathParam(ctx.pathParam("blob_id"));
        executor.submit(() -> {
            try {
                handleInvokeRead(blobId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ctx.contentType("text/plain");
        ctx.result("accepted: invokeRead scheduled for blob_id=" + blobId);
    }

    public void invokeShuffle(Context ctx) {
        String workerId = PathUtils.decodePathParam(ctx.pathParam("worker_id"));
        executor.submit(() -> {
            try {
                handleInvokeShuffle(workerId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ctx.contentType("text/plain");
        ctx.result("accepted: invokeShuffle scheduled for worker_id=" + workerId);
    }

    /**
     * Return the current transient volatile table as JSON.
     */
    public void getTransientData(Context ctx) {
        try {
            Object table = RuntimeData.INSTANCE.getTransientVolatileTable();
            String json = objectMapper.writeValueAsString(table);
            ctx.contentType("application/json");
            ctx.result(json);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("error: " + e.getMessage());
        }
    }

    /**
     * Trigger printing of the sorted transient table to stdout and return a short status.
     */
    public void writeResult(Context ctx) {
        try {
            RuntimeData.INSTANCE.getTransientVolatileTable().printSortedTable();
            ctx.contentType("text/plain");
            ctx.result("ok: printed transient table");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("error: " + e.getMessage());
        }
    }

    // Internal business logic extracted from the controller
    private void handleInvokeRead(String blobId) {
        DataParser parser = new DataParser(blobId);
        try {
            List<Row> rows = parser.parse();
            ExecutionEngine engine = new ExecutionEngine(rows);
            engine.invokeTask();
            String workerId = org.worker.App.getWorkerId();
            notifyOrchestrator(workerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleInvokeShuffle(String workerId) {
        // placeholder: implement shuffle logic when ready
        String destHost = HostConfig.getHostMap().get(workerId);
        System.out.println("handleInvokeShuffle called for " + workerId + " -> host=" + destHost);
        if (destHost == null || destHost.isBlank()) {
            System.err.println("No host mapping found for workerId=" + workerId);
            return;
        }

        String url = HttpUtils.buildUrl(destHost, "/getTransientData");
        try {
            HttpResponse<String> resp = HttpUtils.GET(url);
            if (resp != null && resp.statusCode() == 200) {
                String body = resp.body();
                // deserialize into TransientVolatileTable and set into runtime
                TransientVolatileTable table = objectMapper.readValue(body, TransientVolatileTable.class);
                RuntimeData.INSTANCE.getTransientVolatileTable().mergeTable(table);
                System.out.println("Fetched and merged transient table from " + workerId);
                String currentWorker = org.worker.App.getWorkerId();
                notifyOrchestrator(currentWorker);
            } else {
                System.err.println("Failed to fetch transient data from " + url + " status=" + resp.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error fetching transient data from " + url + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void notifyOrchestrator(String workerId) {
        if (workerId == null || workerId.isBlank()) return;
        String url = orchestratorUrl + "/completeTask/" + workerId;
        HttpUtils.callGet(url);
    }
}
