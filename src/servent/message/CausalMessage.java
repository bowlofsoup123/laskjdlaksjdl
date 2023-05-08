package servent.message;

import app.AppConfig;
import app.ServentInfo;
import app.causality.VectorClock;

import java.util.ArrayList;
import java.util.List;

public class CausalMessage extends BasicMessage{

    private static final long serialVersionUID = 7952273798396080816L;
    private VectorClock senderVectorClock;

    public CausalMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo, VectorClock senderVectorClock) {
        super(type, originalSenderInfo, receiverInfo);

        this.senderVectorClock = senderVectorClock;
    }
    public CausalMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo, String messageText, VectorClock senderVectorClock){
        super(type, originalSenderInfo, receiverInfo, messageText);

        this.senderVectorClock = senderVectorClock;
    }
    public CausalMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo, List<ServentInfo> routeList,String messageText, VectorClock senderVectorClock, Integer messageId){
        super(type, originalSenderInfo, receiverInfo, routeList, messageText, messageId);

        this.senderVectorClock = senderVectorClock;
    }


    @Override
    public Message makeMeASender() {
        ServentInfo newRouteItem = AppConfig.myServentInfo;

        List<ServentInfo> newRouteList = new ArrayList<>(super.getRoute());
        newRouteList.add(newRouteItem);
        return new CausalMessage(getMessageType(), getOriginalSenderInfo(), getReceiverInfo(), newRouteList, getMessageText(), getSenderVectorClock(), getMessageId());
    }

    @Override
    public Message changeReceiver(Integer newReceiverId) {
        if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)) {
            ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);

            return new CausalMessage(getMessageType(), getOriginalSenderInfo(),
                    newReceiverInfo, getRoute(), getMessageText(), getSenderVectorClock(),getMessageId());
        } else {
            AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");

            return null;
        }
    }

    public VectorClock getSenderVectorClock() {
        return senderVectorClock;
    }

    public void setSenderVectorClock(VectorClock senderVectorClock) {
        this.senderVectorClock = senderVectorClock;
    }
}
