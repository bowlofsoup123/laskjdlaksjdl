package cli.command;

import java.util.concurrent.ThreadLocalRandom;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.bitcake_manager.BitcakeManager;
import app.snapshot_bitcake.snapshot_colector.SnapshotCollector;
import servent.message.Message;
import servent.message.TransactionMessage;
import servent.message.util.MessageUtil;

public class TransactionBurstCommand implements CLICommand {

	private static final int TRANSACTION_COUNT = 4;
	private static final int BURST_WORKERS = 5;
	private static final int MAX_TRANSFER_AMOUNT = 10;
	
	//Chandy-Lamport
//	private static final int TRANSACTION_COUNT = 3;
//	private static final int BURST_WORKERS = 5;
//	private static final int MAX_TRANSFER_AMOUNT = 10;
	
	private SnapshotCollector snapshotCollector;
	
	public TransactionBurstCommand(SnapshotCollector snapshotCollector) {
		this.snapshotCollector = snapshotCollector;
	}
	
	private class TransactionBurstWorker implements Runnable {
		
		@Override
		public void run() {
			ThreadLocalRandom rand = ThreadLocalRandom.current();
			BitcakeManager bitcakeManager = snapshotCollector.getBitcakeManager();
			for (int i = 0; i < TRANSACTION_COUNT; i++) {
				for (int neighbor : AppConfig.myServentInfo.getNeighbors()) {
					ServentInfo neighborInfo = AppConfig.getInfoById(neighbor);
					
					int amount = 1 + rand.nextInt(MAX_TRANSFER_AMOUNT);

					Message transactionMessage = new TransactionMessage(
							AppConfig.myServentInfo, neighborInfo, amount, bitcakeManager, null);

					MessageUtil.sendMessage(transactionMessage.makeMeASender());
				}
			}
		}
	}
	
	@Override
	public String commandName() {
		return "transaction_burst";
	}

	@Override
	public void execute(String args) {
		for (int i = 0; i < BURST_WORKERS; i++) {
			Thread t = new Thread(new TransactionBurstWorker());
			
			t.start();
		}
	}

	
}
