package servent.message;

import app.ServentInfo;
import app.causality.VectorClock;

import java.util.Map;

public class TokenMessage extends CausalMessage {
    public TokenMessage(ServentInfo originalSenderInfo, ServentInfo receiverInfo, VectorClock senderVectorClock, int collectorId) {
        super(MessageType.TOKEN, originalSenderInfo, receiverInfo, String.valueOf(collectorId), senderVectorClock);
    }
}
