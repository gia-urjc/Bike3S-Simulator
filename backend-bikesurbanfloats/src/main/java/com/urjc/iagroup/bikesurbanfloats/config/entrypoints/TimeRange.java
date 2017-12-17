package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

public class TimeRange {
    
    private int start;
    private int end;

    public TimeRange() {}
    
    public TimeRange(int start, int end) {
        this.start = start;
        this.end = end;
    }
    
    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }


}
