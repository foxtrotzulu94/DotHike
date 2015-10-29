package ca.concordia.sensortag.datasample;

import java.util.List;

/**
 * Created by EricTremblay on 15-10-29.
 */
public class DBData {
    private RecordingData.Status mStatus;
    private long mRecDuration;
    private int mRecSamples;
    private long mElapsed;
    private List<Long> mEventTimestamps;

    public DBData(RecordingData.Status mStatus, long mRecDuration, int mRecSamples, long mElapsed, List<Long> mEventTimestamps) {
        this.mStatus = mStatus;
        this.mRecDuration = mRecDuration;
        this.mRecSamples = mRecSamples;
        this.mElapsed = mElapsed;
        this.mEventTimestamps = mEventTimestamps;
    }

    public DBData() {}

    public RecordingData.Status getStatus() {
        return mStatus;
    }

    public long getRecDuration() {
        return mRecDuration;
    }

    public int getRecSamples() {
        return mRecSamples;
    }

    public long getElapsed() {
        return mElapsed;
    }

    public List<Long> getEventTimestamps() {
        return mEventTimestamps;
    }

    public void setStatus(RecordingData.Status mStatus) {
        this.mStatus = mStatus;
    }

    public void setRecDuration(long mRecDuration) {
        this.mRecDuration = mRecDuration;
    }

    public void setRecSamples(int mRecSamples) {
        this.mRecSamples = mRecSamples;
    }

    public void setElapsed(long mElapsed) {
        this.mElapsed = mElapsed;
    }

    public void setEventTimestamps(List<Long> mEventTimestamps) {
        this.mEventTimestamps = mEventTimestamps;
    }
}
