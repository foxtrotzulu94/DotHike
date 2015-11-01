package me.dotteam.dotprod.data;
import java.util.*;

/**
 * 
 */
public class PersistentStorageEntity {

    /**
     * Default constructor
     */
    public PersistentStorageEntity() {
    }

    /**
     * 
     */
    private DBAssistant mProvider;



    /**
     * @return
     */
    public List<Hike> getHikesList() {
        // TODO implement here
        return null;
    }

    /**
     * @param specificHike 
     * @return
     */
    public SessionData loadHikeData(Hike specificHike) {
        // TODO implement here
        return null;
    }

    /**
     * @param hikeID 
     * @return
     */
    public SessionData loadHikeData(int hikeID) {
        // TODO implement here
        return null;
    }

    /**
     * @param givenSession 
     * @return
     */
    public boolean saveSession(SessionData givenSession) {
        // TODO implement here
        return false;
    }

}