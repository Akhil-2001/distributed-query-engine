package org.orchestrator.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskPlan {
    
    private List<String> dataNodes;
    private Map<String, String> workerPool;

    public TaskPlan() {
        this.dataNodes = new ArrayList<>();
        this.workerPool = new HashMap<>();
        initPlan();
    }

    public List<String> getDataNodes() {
        return dataNodes;
    }
    public void setDataNodes(List<String> dataNodes) {
        this.dataNodes = dataNodes;
    }
    public Map<String, String> getworkerPool() {
        return workerPool;
    }
    public void setworkerPool(Map<String, String> workerPool) {
        this.workerPool = workerPool;
    }

    private void initPlan() {
        Path storageDir = Path.of("../../student_scores");
        
        try {
            Files.list(storageDir)
                .filter(p -> p.toString().endsWith(".csv"))
                .forEach(p -> this.dataNodes.add(p.getFileName().toString()));
            } catch (IOException e) {
                e.printStackTrace();
        }

        // Hard coded for now, determine in runtime using some args maybe
        this.workerPool = org.orchestrator.config.WorkerConfig.getWorkerPool();

    }

}
