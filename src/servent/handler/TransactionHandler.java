package servent.handler;

import app.AppConfig;
import app.snapshot_bitcake.bitcake_manager.ABBitcakeManager;
import app.snapshot_bitcake.bitcake_manager.BitcakeManager;
import app.snapshot_bitcake.snapshot_colector.SnapshotCollector;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TransactionMessage;

public class TransactionHandler implements MessageHandler {

	private Message clientMessage;
	private SnapshotCollector snapshotCollector;
	
	public TransactionHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
		this.clientMessage = clientMessage;
		this.snapshotCollector = snapshotCollector;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.TRANSACTION) {
			String amountString = clientMessage.getMessageText();
			
			int amountNumber = 0;
			try {
				amountNumber = Integer.parseInt(amountString);
			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Couldn't parse amount: " + amountString);
				return;
			}
			BitcakeManager bitcakeManager = snapshotCollector.getBitcakeManager();

			bitcakeManager.addSomeBitcakes(amountNumber, clientMessage);



			AppConfig.timestampedStandardPrint("Transaction handler got: " + clientMessage);
		}
	}

}
