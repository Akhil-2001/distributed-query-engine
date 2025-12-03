package org.worker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VolatileRow {
    private String groupId;
    private int minVal;
    private int maxVal;
    private int sum;
    private int count;

    // Using a fine grained lock when multiple threads try to update the same row
    @JsonIgnore
    private Lock fineGrainLock;

    // Default constructor for Jackson deserialization
    public VolatileRow() {
        this.groupId = "0";
        this.minVal = Integer.MAX_VALUE;
        this.maxVal = Integer.MIN_VALUE;
        this.sum = 0;
        this.count = 0;
        this.fineGrainLock = new ReentrantLock();
    }

    public VolatileRow(String groupId) {
        this.groupId = groupId;
        // initialize min/max to sentinel values so first update sets them correctly
        this.minVal = Integer.MAX_VALUE; // acts like +infinity for mins
        this.maxVal = Integer.MIN_VALUE; // acts like -infinity for maxes
        this.sum = 0;
        this.count = 0;
        this.fineGrainLock = new ReentrantLock();
    }

    public void updateRow(int val) {
        fineGrainLock.lock();
        try {
            minVal = Math.min(minVal, val);
            maxVal = Math.max(maxVal, val);
            sum += val;
            count++;
        } finally {
            fineGrainLock.unlock();
        }
    }

    public void merge(VolatileRow other) {
        if (other == null) {
            return;
        }
        fineGrainLock.lock();
        try {
            minVal = Math.min(minVal, other.minVal);
            maxVal = Math.max(maxVal, other.maxVal);
            sum += other.sum;
            count += other.count;
        } finally {
            fineGrainLock.unlock();
        }
    }

    @Override
    public String toString() {
        return groupId+","+minVal+","+maxVal+","+(sum/count);
    }

    public String getGroupId() {
        return groupId;
    }
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    public int getMinVal() {
        return minVal;
    }
    public void setMinVal(int minVal) {
        this.minVal = minVal;
    }
    public int getMaxVal() {
        return maxVal;
    }
    public void setMaxVal(int maxVal) {
        this.maxVal = maxVal;
    }
    public int getSum() {
        return sum;
    }
    public void setSum(int sum) {
        this.sum = sum;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
}
