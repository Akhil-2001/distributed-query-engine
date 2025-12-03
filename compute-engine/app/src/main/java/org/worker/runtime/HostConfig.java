package org.worker.runtime;

import java.util.HashMap;
import java.util.Map;

// Hard coded for now, assume we get these confs from orchestrator
public class HostConfig {
    private static Map<String, String> hostMap;
    
    static {
        hostMap = new HashMap<>();
        hostMap.put("W1","http://localhost:9001");
        hostMap.put("W2","http://localhost:9002");
        hostMap.put("W3","http://localhost:9003");
    }

    public static Map<String, String> getHostMap() {
        return hostMap;
    }
    public static void setHostMap(Map<String, String> hostMap) {
        HostConfig.hostMap = hostMap;
    }
}
