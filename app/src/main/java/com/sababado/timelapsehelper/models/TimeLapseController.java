package com.sababado.timelapsehelper.models;

import static com.sababado.timelapsehelper.models.TimeLapseItem.PAUSED;
import static com.sababado.timelapsehelper.models.TimeLapseItem.RUNNING;
import static com.sababado.timelapsehelper.models.TimeLapseItem.STOPPED;

/**
 * Created by robert on 3/30/17.
 * Controller class to add logic and meaning to the timestamps in the {@link TimeLapseItem} class.
 */

public class TimeLapseController {
    /**
     * Start a time-lapse. This action can be called when the time-lapse is in a paused or stopped state.
     *
     * @param timeLapseItem The time-lapse to start.
     */
    public static void startTimeLapse(TimeLapseItem timeLapseItem) {
        long now = System.currentTimeMillis();
        @TimeLapseItem.RunState int currentRunState = timeLapseItem.getRunState();
        if (currentRunState == PAUSED) {
            // if starting again from a paused state.
            timeLapseItem.setPauseLength(timeLapseItem.calculatePauseLength());
        } else if (currentRunState == STOPPED) {
            // starting from a stopped state.
            timeLapseItem.setPauseLength(0L);
            timeLapseItem.setStartTime(now);
        }
        timeLapseItem.setPauseTime(0L); // The last time the TL was paused doesn't matter now.
        timeLapseItem.setRunState(RUNNING);
    }

    /**
     * Pause a time-lapse. This action can be called when the time-lapse is in a running state.
     * It does not make sense to call this while the time-lapse has been stopped.
     *
     * @param timeLapseItem The time-lapse to pause.
     */
    public static void pauseTimeLapse(TimeLapseItem timeLapseItem) {
        long now = System.currentTimeMillis();
        @TimeLapseItem.RunState int currentRunState = timeLapseItem.getRunState();
        if (currentRunState == RUNNING) {
            // Pause from a running state.
            timeLapseItem.setPauseTime(now);
        } else if (currentRunState == STOPPED) {
            throw new UnsupportedOperationException("Pausing a time-lapse from a stopped state doesn't make any sense.");
        }
        timeLapseItem.setRunState(PAUSED);
    }

    /**
     * Stop a time-lapse. Putting the time-lapse in a stopped state will essentially freeze the data
     * until the time-lapse is started again.
     *
     * @param timeLapseItem The time-lapse to stop.
     */
    public static void stopTimeLapse(TimeLapseItem timeLapseItem) {
        timeLapseItem.setRunState(STOPPED);
    }

    /**
     * Get the total time that the time-lapse has been in a running state.
     *
     * @param timeLapseItem Time-lapse to get a runtime for.
     * @return Runtime in milliseconds.
     */
    public static long getRunningTime(TimeLapseItem timeLapseItem) {
        long now = System.currentTimeMillis();
        return now - timeLapseItem.getStartTime() - timeLapseItem.calculatePauseLength();
    }

    /**
     * Get the number of frames that has elapsegetRunningTime(timeLapseItem)d based on the start time of the time-lapse.
     *
     * @param timeLapseItem Time-lapse to get the frame count for.
     * @return The number of frames that should have been taken during this timelapse.
     */
    public static int getFramesElapsed(TimeLapseItem timeLapseItem) {
        float secondsElapsed = ((float) getRunningTime(timeLapseItem) / 1000f);
        return (int) (secondsElapsed / timeLapseItem.getSecondsPerFrame());
    }
}
