package servent.handler;

import app.AppConfig;
import app.causality.Causality;
import app.ServentInfo;
import app.snapshot_bitcake.snapshot_colector.SnapshotCollector;
import servent.message.CausalMessage;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class TokenHandler implements MessageHandler{

    private CausalMessage clientMessage;
    private SnapshotCollector snapshotCollector;


    public TokenHandler(CausalMessage clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() != MessageType.TOKEN) return;
//        AppConfig.timestampedErrorPrint("TOKEN");

        ServentInfo senderInfo = clientMessage.getOriginalSenderInfo();
        ServentInfo lastSenderInfo = clientMessage.getRoute().size() == 0 ?
                clientMessage.getOriginalSenderInfo() :
                clientMessage.getRoute().get(clientMessage.getRoute().size()-1);

        String text = String.format("Got %s from %s token by %s",
                clientMessage.getMessageText(), lastSenderInfo, senderInfo);

        AppConfig.timestampedStandardPrint(text);

        if (senderInfo.getId() == AppConfig.myServentInfo.getId()) {
            AppConfig.timestampedStandardPrint("Got own message back. No rebroadcast.");
        } else {

            boolean didPut = Causality.receivedBroadcasts.add(clientMessage);

            if (didPut) {

                snapshotCollector.getBitcakeManager().tokenEvent(Integer.parseInt(clientMessage.getMessageText()), snapshotCollector);
                AppConfig.timestampedStandardPrint("Rebroadcasting... " + Causality.receivedBroadcasts.size());

                for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    MessageUtil.sendMessage(clientMessage.changeReceiver(neighbor).makeMeASender());
                }


            } else {
                AppConfig.timestampedStandardPrint("Already had this. No rebroadcast.");
            }
        }
    }
}
