package servent.handler.snapshot;

import app.AppConfig;
import app.snapshot_bitcake.result.ABSnapshotResult;
import app.snapshot_bitcake.snapshot_colector.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.ABSnapshotMessage;
import servent.message.Message;
import servent.message.MessageType;

public class ABSnapshotHandler implements MessageHandler {
    private Message clientMessage;
    private SnapshotCollector snapshotCollector;

    public ABSnapshotHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.AB_SNAPSHOT){
            ABSnapshotResult snapshotResult = ((ABSnapshotMessage)clientMessage).getSnapshotResult();
            snapshotCollector.addABSnapshotResult(snapshotResult.getServentId(), snapshotResult);

            AppConfig.timestampedStandardPrint("AB snapshot handler got snapshot from: " + snapshotResult.getServentId());
        }
        else
            AppConfig.timestampedStandardPrint("Snaphost not handled!");
    }
}
