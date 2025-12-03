package org.worker.model;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// Used to store Transient query results in memory
public class TransientVolatileTable {
    Map<String, VolatileRow> groupIdVsRow;

    public TransientVolatileTable() {
        this.groupIdVsRow = new ConcurrentHashMap<String, VolatileRow>();
    }

    /**
     * Write the sorted table (by numeric groupId) to a file named 'output.txt'.
     * The file is overwritten on each call.
     */
    public void printSortedTable() {
        try {
            List<String> lines = groupIdVsRow.values().stream()
                .sorted(Comparator.comparingInt(r -> Integer.parseInt(r.getGroupId())))
                .map(Object::toString)
                .collect(Collectors.toList());

            Path out = Paths.get("output.txt");
            try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                for (String line : lines) {
                    w.write(line);
                    w.newLine();
                }
            }
        } catch (Exception e) {
            // preserve original behavior of printing error to stderr
            e.printStackTrace();
        }
    }

    public VolatileRow fetchRowByGroupId(String groupId) {
        // Thread safe return
        return groupIdVsRow.computeIfAbsent(groupId, key -> new VolatileRow(key));
    }

    public Map<String, VolatileRow> getGroupIdVsRow() {
        return groupIdVsRow;
    }

    public void setGroupIdVsRow(Map<String, VolatileRow> groupIdVsRow) {
        this.groupIdVsRow = groupIdVsRow;
    }

    public void mergeTable(TransientVolatileTable table) {
        if (table == null || table.getGroupIdVsRow() == null) {
            return;
        }
        table.getGroupIdVsRow().forEach((key, value) -> {
            this.groupIdVsRow.merge(key, value, (oldVal, newVal) -> {
                oldVal.merge(newVal);
                return oldVal;
            });
        });
        
    }

}
