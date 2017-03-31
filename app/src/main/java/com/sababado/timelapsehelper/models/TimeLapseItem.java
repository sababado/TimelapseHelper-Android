package com.sababado.timelapsehelper.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import com.sababado.ezprovider.Column;
import com.sababado.ezprovider.Id;
import com.sababado.ezprovider.Table;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by robert on 3/30/17.
 * This is a model representing a set of parameters for a time-lapse.
 */
@Table(name = "TimeLapseItem", code = 1)
public class TimeLapseItem implements Parcelable {

    public static final int RUNNING = 1;
    public static final int PAUSED = 2;
    public static final int STOPPED = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RUNNING, PAUSED, STOPPED})
    public @interface RunState {
    }

    @Id
    private long id;

    // The time that the time-lapse started in milliseconds.
    @Column(1)
    private long startTime;

    // The number of seconds between each frame.
    @Column(2)
    private float secondsPerFrame;

    // The state of the time-lapse (running, stopped, paused)
    @Column(3)
    @RunState
    private int runState;

    // The last time that the time-lapse was paused in milliseconds.
    @Column(4)
    private long pauseTime;

    // The amount of time in milliseconds that this time-lapse has been paused.
    @Column(5)
    private long pauseLength;

    public TimeLapseItem() {
        startTime = 0L;
        secondsPerFrame = 5;
        runState = STOPPED;
        pauseTime = 0L;
        pauseLength = 0L;
    }

    public TimeLapseItem(Parcel in) {
        id = in.readLong();
        startTime = in.readLong();
        secondsPerFrame = in.readFloat();
        //noinspection WrongConstant
        runState = in.readInt();
        pauseTime = in.readLong();
        pauseLength = in.readLong();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public float getSecondsPerFrame() {
        return secondsPerFrame;
    }

    public void setSecondsPerFrame(float secondsPerFrame) {
        this.secondsPerFrame = secondsPerFrame;
    }

    @RunState
    public int getRunState() {
        return runState;
    }

    void setRunState(@RunState int runState) {
        this.runState = runState;
    }

    public long getPauseTime() {
        return pauseTime;
    }

    void setPauseTime(long pauseTime) {
        this.pauseTime = pauseTime;
    }

    /**
     * Calculate what the pause length should be based the last time this time-lapse was paused.
     * This will value will be different every time it is called while the time-lapse is in a paused state.
     *
     * @return A length of time that the time-lapse has been paused.
     */
    public long calculatePauseLength() {
        if (runState == PAUSED) {
            return pauseLength + System.currentTimeMillis() - pauseTime;
        }
        return pauseLength;
    }

    void setPauseLength(long pauseLength) {
        this.pauseLength = pauseLength;
    }

    public static final Creator<TimeLapseItem> CREATOR = new Creator<TimeLapseItem>() {
        @Override
        public TimeLapseItem createFromParcel(Parcel parcel) {
            return new TimeLapseItem(parcel);
        }

        @Override
        public TimeLapseItem[] newArray(int i) {
            return new TimeLapseItem[0];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeLong(startTime);
        parcel.writeFloat(secondsPerFrame);
        parcel.writeInt(runState);
        parcel.writeLong(pauseTime);
        parcel.writeLong(pauseLength);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeLapseItem that = (TimeLapseItem) o;

        if (id != that.id) return false;
        if (startTime != that.startTime) return false;
        if (Float.compare(that.secondsPerFrame, secondsPerFrame) != 0) return false;
        if (runState != that.runState) return false;
        if (pauseTime != that.pauseTime) return false;
        return pauseLength == that.pauseLength;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (secondsPerFrame != +0.0f ? Float.floatToIntBits(secondsPerFrame) : 0);
        result = 31 * result + runState;
        result = 31 * result + (int) (pauseTime ^ (pauseTime >>> 32));
        result = 31 * result + (int) (pauseLength ^ (pauseLength >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "TimeLapseItem{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", secondsPerFrame=" + secondsPerFrame +
                ", runState=" + runState +
                ", pauseTime=" + pauseTime +
                ", pauseLength=" + pauseLength +
                '}';
    }
}
