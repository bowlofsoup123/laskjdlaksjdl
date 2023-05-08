package app.snapshot_bitcake.bitcake_manager;

import app.AppConfig;
import app.causality.Causality;
import app.snapshot_bitcake.result.ABSnapshotResult;
import app.snapshot_bitcake.snapshot_colector.SnapshotCollector;
import servent.message.ABSnapshotMessage;
import servent.message.Message;
import servent.message.TransactionMessage;
import servent.message.util.MessageUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ABBitcakeManager implements BitcakeManager{

    private final AtomicInteger currentAmount = new AtomicInteger(1000);
    private Object amountLock = new Object();
    private Map<Integer, List<TransactionMessage>> giveHistory = new ConcurrentHashMap<>();
    private Map<Integer, List<TransactionMessage>> getHistory = new ConcurrentHashMap<>();

    public ABBitcakeManager() {
        for(Integer i: AppConfig.myServentInfo.getNeighbors()){
            giveHistory.put(i, new CopyOnWriteArrayList<>());
            getHistory.put(i, new CopyOnWriteArrayList<>());
        }
    }

    public void takeSomeBitcakes(int amount, Message clientMessage) {
//        synchronized (amountLock) {
            currentAmount.getAndAdd(-amount);
//            AppConfig.timestampedErrorPrint(amount+"_sent_"+clientMessage);
            giveHistory
                    .get(clientMessage.getReceiverInfo().getId())
                    .add((TransactionMessage) clientMessage);
//        }
    }

    public void addSomeBitcakes(int amount, Message clientMessage) {
//        synchronized (amountLock) {
            currentAmount.getAndAdd(amount);
//            AppConfig.timestampedErrorPrint(amount+"_received_"+clientMessage);
            getHistory
                    .get(clientMessage.getOriginalSenderInfo().getId())
                    .add((TransactionMessage) clientMessage);
//        }
    }

    public int getCurrentBitcakeAmount() {
//        synchronized (amountLock) {
            return currentAmount.get();
//        }
    }

    public void tokenEvent(int collectorId, SnapshotCollector snapshotCollector){
        int recordedAmount = getCurrentBitcakeAmount();
        AppConfig.timestampedStandardPrint("recored value in servent_" + AppConfig.myServentInfo.getId() + " is: " + recordedAmount);

        Map<Integer, List<TransactionMessage>> tmpGive = copyGiveHistory();
        Map<Integer, List<TransactionMessage>> tmpGet = copyGetHistory();

        ABSnapshotResult snapshotResult = new ABSnapshotResult(
                AppConfig.myServentInfo.getId(), recordedAmount, tmpGive, tmpGet);

        if (collectorId == AppConfig.myServentInfo.getId()) {
            AppConfig.timestampedStandardPrint("Sending snapshot to myself");
            snapshotCollector.addABSnapshotResult(
                    AppConfig.myServentInfo.getId(),
                    snapshotResult);
        }
        else {
            ABSnapshotMessage tellMessage = new ABSnapshotMessage(
                    AppConfig.myServentInfo, AppConfig.getInfoById(collectorId), Causality.copyClock(), snapshotResult);

            MessageUtil.sendMessage(tellMessage.makeMeASender());
        }
//        clearHistory();
    }

    private Map<Integer, List<TransactionMessage>> copyGiveHistory(){
        Map<Integer, List<TransactionMessage>> tmpGive = new HashMap<>();
//        synchronized (giveHistoryLock) {
            for (int neighbourId : AppConfig.myServentInfo.getNeighbors()) {
                tmpGive.put(neighbourId, List.copyOf(giveHistory.get(neighbourId)));
            }
//        }
        return tmpGive;
    }
    private Map<Integer, List<TransactionMessage>> copyGetHistory(){
        Map<Integer, List<TransactionMessage>> tmpGet = new HashMap<>();
//        synchronized (getHistoryLock) {
            for (int neighbourId : AppConfig.myServentInfo.getNeighbors()) {
                tmpGet.put(neighbourId, List.copyOf(getHistory.get(neighbourId)));
            }
//        }
        return tmpGet;
    }

    private void clearHistory(){
        for(int neighbourId: AppConfig.myServentInfo.getNeighbors()){
            giveHistory.get(neighbourId).clear();
            getHistory.get(neighbourId).clear();
        }
    }
    public AtomicInteger getCurrentAmount() {
        return currentAmount;
    }

    public Map<Integer, List<TransactionMessage>> getGiveHistory() {
        return giveHistory;
    }

    public Map<Integer, List<TransactionMessage>> getGetHistory() {
        return getHistory;
    }
}
