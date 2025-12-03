package org.worker.runtime;

import org.worker.model.TransientVolatileTable;

public enum RuntimeData {
    INSTANCE;

    private TransientVolatileTable transientVolatileTable = new TransientVolatileTable();

    public TransientVolatileTable getTransientVolatileTable() {
        return transientVolatileTable;
    }

    public void setTransientVolatileTable(TransientVolatileTable transientVolatileTable) {
        this.transientVolatileTable = transientVolatileTable;
    }
}