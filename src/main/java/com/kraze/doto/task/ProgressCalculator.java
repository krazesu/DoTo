package com.kraze.doto.task;

public final class ProgressCalculator {
    private ProgressCalculator() {}

    public static int forTask(TaskStatus status, int subtasksTotal, int subtasksDone) {
        if(status == TaskStatus.DONE){
            return 100;
        }
        if(subtasksTotal > 0){
            return (int) Math.round(100.0 * subtasksDone / subtasksTotal);
        }

        return switch(status) {
            case TODO -> 0;
            case IN_PROGRESS -> 50;
            case IN_REVIEW -> 80;
            case DONE -> 100;
        };
    }
}
