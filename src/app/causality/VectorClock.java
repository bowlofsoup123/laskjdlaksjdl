package app.causality;

import app.AppConfig;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VectorClock implements Serializable {

    private int serventId;
    private Map<Integer, Integer> receivedMap = new ConcurrentHashMap<>();
    private Map<Integer, Integer> sentMap = new ConcurrentHashMap<>();

    public VectorClock(int serventId) {
        for(int i=0; i < AppConfig.getServentCount(); i++){
            receivedMap.put(i, 0);
            sentMap.put(i, 0);
        }
        this.serventId = serventId;
    }

    public int getServentId() {
        return serventId;
    }

    public Map<Integer, Integer> getReceivedMap() {
        return receivedMap;
    }

    public Map<Integer, Integer> getSentMap() {
        return sentMap;
    }


    @Override
    public String toString() {
        return "VectorClock{" +
                "serventId=" + serventId +
                ", receivedMap=" + receivedMap +
                ", sentMap=" + sentMap +
                '}';
    }
}
