package app.snapshot_bitcake.result;

import servent.message.TransactionMessage;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ABSnapshotResult implements Serializable {
    private final int serventId;
    private final int recordedAmount;
    private Map<Integer, List<TransactionMessage>> giveHistory;
    private Map<Integer, List<TransactionMessage>> getHistory;


    public ABSnapshotResult(int serventId, int recordedAmount, Map<Integer, List<TransactionMessage>> giveHistory, Map<Integer, List<TransactionMessage>> getHistory) {
        this.serventId = serventId;
        this.recordedAmount = recordedAmount;
        this.giveHistory = giveHistory;
        this.getHistory = getHistory;
    }


    public int getServentId() {
        return serventId;
    }

    public int getRecordedAmount() {
        return recordedAmount;
    }

    public Map<Integer, List<TransactionMessage>> getGiveHistory() {
        return giveHistory;
    }

    public Map<Integer, List<TransactionMessage>> getGetHistory() {
        return getHistory;
    }

    @Override
    public String toString() {
        return "ABSnapshotResult{" +
                "\nserventId=" + serventId +
                "\nrecordedAmount=" + recordedAmount +
                "\ngiveHistory  =" + giveHistory +
                "\ngetHistory size =" + getHistory +
                "\n}";
    }
}
