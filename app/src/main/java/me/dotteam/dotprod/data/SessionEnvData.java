package me.dotteam.dotprod.data;


import java.util.List;

/**
 * Data structure class to maintain an extended list of recorded samples in a session
 * Presently not in use.
 */
public class SessionEnvData extends EnvData {

    /**
     * 
     */
    protected List<Float> recordedTemp;

    /**
     * 
     */
    protected List<Float> recordedPressure;

    /**
     * 
     */
    protected List<Float> recordedHumidity;

    /**
     * Default constructor
     */
    public SessionEnvData() {
    }
}