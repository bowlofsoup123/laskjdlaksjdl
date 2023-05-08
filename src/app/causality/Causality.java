package app.causality;

import app.AppConfig;
import app.snapshot_bitcake.snapshot_colector.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.handler.NullHandler;
import servent.handler.TokenHandler;
import servent.handler.TransactionHandler;
import servent.handler.snapshot.ABSnapshotHandler;
import servent.message.CausalMessage;
import servent.message.Message;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;

public class Causality {
    public static VectorClock vectorClock = new VectorClock(AppConfig.myServentInfo.getId());
    public static Queue<Message> pendingMessages = new ConcurrentLinkedQueue<>();
    private static final Object pendingMessagesLock = new Object();
    public static Set<Message> receivedBroadcasts = Collections.newSetFromMap(new ConcurrentHashMap<Message, Boolean>());
    private final SnapshotCollector snapshotCollector;
    public static final Object incLock = new Object();
    private final ExecutorService threadPool = Executors.newWorkStealingPool();

    public Causality(SnapshotCollector snapshotCollector) {
        this.snapshotCollector = snapshotCollector;
    }

    public static void incrementReceiveClock(int serventId) {
        vectorClock.getReceivedMap().computeIfPresent(serventId, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer key, Integer oldValue) {
                return oldValue+1;
            }
        });
    }
    public static void incrementSendClock(int serventId) {
        vectorClock.getSentMap().computeIfPresent(serventId, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer key, Integer oldValue) {
                return oldValue+1;
            }
        });
    }

    public static VectorClock copyClock(){
        VectorClock copy = new VectorClock(AppConfig.myServentInfo.getId());
        for(int key = 0; key < AppConfig.getServentCount(); key++){
            int recValue = vectorClock.getReceivedMap().get(key);
            copy.getReceivedMap().put(key, recValue);
            int sendValue = vectorClock.getSentMap().get(key);
            copy.getSentMap().put(key, sendValue);
        }
        return copy;
    }

    public static VectorClock getVectorClock() {
        return vectorClock;
    }
    public void addPendingMessage(Message msg) {
        pendingMessages.add(msg);
    }

    private boolean otherClockGreater(VectorClock clock1, VectorClock clock2) {
        if (clock1.getReceivedMap().size() != clock2.getReceivedMap().size() ||
                clock1.getSentMap().size() != clock2.getSentMap().size()) {
            throw new IllegalArgumentException("Clocks are not same size how why");
        }
        if(AppConfig.myServentInfo.getNeighbors().contains(clock2.getServentId()))
            if(clock2.getSentMap().get(clock1.getServentId()) > clock1.getReceivedMap().get(clock2.getServentId()))
                return true;

        return false;
    }

    private void handleMessage(CausalMessage message){
//        AppConfig.timestampedErrorPrint("Vector clock in received message: " + message.getSenderVectorClock() +
//                ", " + message);
//        AppConfig.timestampedErrorPrint("My vector clock for message: " + vectorClock +
//                ", " + message);

        incrementReceiveClock(message.getRoute().get(message.getRoute().size()-1).getId());

        MessageHandler messageHandler = new NullHandler(message);
        switch (message.getMessageType()){
            case TRANSACTION:
                messageHandler = new TransactionHandler(message, snapshotCollector);
                break;
            case TOKEN:
                messageHandler = new TokenHandler(message, snapshotCollector);
                break;
            case AB_SNAPSHOT:
                messageHandler = new ABSnapshotHandler(message, snapshotCollector);
                break;
            case POISON:
                break;
        }
        threadPool.submit(messageHandler);
//        messageHandler.run();
    }

    public void checkPendingMessages() {
        boolean gotWork = true;

        while (gotWork) {
            gotWork = false;

//            synchronized (pendingMessagesLock) {
                Iterator<Message> iterator = pendingMessages.iterator();

                VectorClock myVectorClock = copyClock();
                while (iterator.hasNext()) {
                    Message pendingMessage = iterator.next();
                    CausalMessage causalPendingMessage = (CausalMessage)pendingMessage;
                    if (!otherClockGreater(myVectorClock, causalPendingMessage.getSenderVectorClock())) {
                        gotWork = true;

                        AppConfig.timestampedStandardPrint("Handling " + causalPendingMessage);
                        handleMessage(causalPendingMessage);

                        iterator.remove();

                        break;
                    }
                }
//            }
        }
    }

//    @Override
//    public void run() {
//        boolean gotWork = true;
//
//        while (gotWork) {
//            gotWork = false;
//
//            synchronized (pendingMessagesLock) {
//                Iterator<Message> iterator = pendingMessages.iterator();
//
//                VectorClock myVectorClock = copyClock();
//                while (iterator.hasNext()) {
//                    Message pendingMessage = iterator.next();
//                    CausalMessage causalPendingMessage = (CausalMessage)pendingMessage;
//                    if (!otherClockGreater(myVectorClock, causalPendingMessage.getSenderVectorClock())) {
//                        gotWork = true;
//
//                        AppConfig.timestampedStandardPrint("Handling " + causalPendingMessage);
//                        handleMessage(causalPendingMessage);
//
//                        iterator.remove();
//
//                        break;
//                    }
//                }
//            }
//        }
//    }
}
