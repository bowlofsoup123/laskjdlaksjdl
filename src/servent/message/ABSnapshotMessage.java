package servent.message;

import app.AppConfig;
import app.ServentInfo;
import app.causality.VectorClock;
import app.snapshot_bitcake.result.ABSnapshotResult;

import java.util.ArrayList;
import java.util.List;

public class ABSnapshotMessage extends CausalMessage{

    private ABSnapshotResult snapshotResult;

    public ABSnapshotMessage(ServentInfo originalSenderInfo, ServentInfo receiverInfo, VectorClock senderVectorClock, ABSnapshotResult snapshotResult) {
        super(MessageType.AB_SNAPSHOT, originalSenderInfo, receiverInfo, senderVectorClock);
        this.snapshotResult = snapshotResult;
    }
    public ABSnapshotMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo, List<ServentInfo> routeList,String messageText, VectorClock senderVectorClock, Integer messageId, ABSnapshotResult snapshotResult){
        super(type, originalSenderInfo, receiverInfo, routeList, messageText, senderVectorClock,messageId);

        this.snapshotResult = snapshotResult;
    }
    @Override
    public Message makeMeASender() {
        ServentInfo newRouteItem = AppConfig.myServentInfo;

        List<ServentInfo> newRouteList = new ArrayList<>(super.getRoute());
        newRouteList.add(newRouteItem);
        return new ABSnapshotMessage(getMessageType(), getOriginalSenderInfo(), getReceiverInfo(), newRouteList, getMessageText(), getSenderVectorClock(), getMessageId(), getSnapshotResult());
    }

    public ABSnapshotResult getSnapshotResult() {
        return snapshotResult;
    }

}
