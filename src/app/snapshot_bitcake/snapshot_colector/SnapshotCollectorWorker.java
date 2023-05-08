package app.snapshot_bitcake.snapshot_colector;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import app.AppConfig;
import app.snapshot_bitcake.bitcake_manager.ABBitcakeManager;
import app.snapshot_bitcake.bitcake_manager.BitcakeManager;
import app.snapshot_bitcake.result.ABSnapshotResult;
import servent.message.TransactionMessage;


/**
 * Main snapshot collector class. Has support for Naive, Chandy-Lamport
 * and Lai-Yang snapshot algorithms.
 * 
 * @author bmilojkovic
 *
 */
public class SnapshotCollectorWorker implements SnapshotCollector {

	private volatile boolean working = true;
	
	private AtomicBoolean collecting = new AtomicBoolean(false);
	
	private Map<Integer, ABSnapshotResult> collectedABValues = new ConcurrentHashMap<>();
//	private Map<String, Integer> collectedAVValues = new ConcurrentHashMap<>();
	
	private SnapshotType snapshotType = SnapshotType.NAIVE;
	
	private BitcakeManager bitcakeManager;

	public SnapshotCollectorWorker(SnapshotType snapshotType) {
		this.snapshotType = snapshotType;
		
		switch(snapshotType) {
		case AB:
			bitcakeManager = new ABBitcakeManager();
			break;
		case AV:
			break;
		case NONE:
			AppConfig.timestampedErrorPrint("Making snapshot collector without specifying type. Exiting...");
			System.exit(0);
		}
	}
	
	@Override
	public BitcakeManager getBitcakeManager() {
		return bitcakeManager;
	}
	
	@Override
	public void run() {
		while(working) {
			
			/*
			 * Not collecting yet - just sleep until we start actual work, or finish
			 */
			while (collecting.get() == false) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (working == false) {
					return;
				}
			}
			
			/*
			 * Collecting is done in three stages:
			 * 1. Send messages asking for values
			 * 2. Wait for all the responses
			 * 3. Print result
			 */
			
			//1 send asks
			switch (snapshotType) {
				case AB:
					((ABBitcakeManager)bitcakeManager).tokenEvent(AppConfig.myServentInfo.getId(), this);
					break;
				case AV:
					break;
			case NONE:
				//Shouldn't be able to come here. See constructor. 
				break;
			}
			
			//2 wait for responses or finish
			boolean waiting = true;
			while (waiting) {
				switch (snapshotType) {

				case AB:
					if (collectedABValues.size() == AppConfig.getServentCount()) {
						waiting = false;
					}
					break;
				case NONE:
					//Shouldn't be able to come here. See constructor. 
					break;
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if (working == false) {
					return;
				}
			}
			
			//print
			int sum;
			switch (snapshotType) {
//			case NAIVE:
//				sum = 0;
//				for (Entry<String, Integer> itemAmount : collectedNaiveValues.entrySet()) {
//					sum += itemAmount.getValue();
//					AppConfig.timestampedStandardPrint(
//							"Info for " + itemAmount.getKey() + " = " + itemAmount.getValue() + " bitcake");
//				}
//
//				AppConfig.timestampedStandardPrint("System bitcake count: " + sum);
//
//				collectedNaiveValues.clear(); //reset for next invocation
//				break;
			case AB:
				sum = 0;
				for (Entry<Integer, ABSnapshotResult> nodeResult : collectedABValues.entrySet()) {
					sum += nodeResult.getValue().getRecordedAmount();
					AppConfig.timestampedStandardPrint(
							"Recorded bitcake amount for " + nodeResult.getKey() + " = " + nodeResult.getValue().getRecordedAmount());
//					AppConfig.timestampedErrorPrint("------------------------------------Snapshot results-----------------------------------------------------");
//					AppConfig.timestampedErrorPrint("SnapshotResult: " + nodeResult);
				}
				for(int i = 0; i < AppConfig.getServentCount(); i++) {
					for (int j: AppConfig.getInfoById(i).getNeighbors()) {
						List<TransactionMessage> fromJ = collectedABValues.get(j).getGiveHistory().get(i);
						List<TransactionMessage> inI = collectedABValues.get(i).getGetHistory().get(j);

						if(inI.size() < fromJ.size()){

//							AppConfig.timestampedErrorPrint("Messages in transition. \n" + i + " received: " + collectedABValues.get(i).getGetHistory().get(j)
//								+".\n"+j+" sent: " + collectedABValues.get(j).getGiveHistory().get(i));
//							AppConfig.timestampedErrorPrint("---------------------------------Missing message-------------------------------------------------");

							int unreceived = 0;
							for(TransactionMessage tm: fromJ){
								if(!inI.contains(tm)) {
									unreceived += Integer.parseInt(tm.getMessageText());
//									AppConfig.timestampedErrorPrint("Message: " + tm);
								}
							}

//							AppConfig.timestampedErrorPrint("-----------------------------------------END-----------------------------------------------------");
							String outputString = String.format(
									"Unreceived bitcake amount: %d from servent %d to servent %d",
									unreceived, j, i);
							AppConfig.timestampedStandardPrint(outputString);
							//sum += unreceived;
						}
					}
				}

				AppConfig.timestampedStandardPrint("System bitcake count: " + sum);

				collectedABValues.clear(); //reset for next invocation
				break;
			case AV:
				break;
			case NONE:
				//Shouldn't be able to come here. See constructor. 
				break;
			}
			collecting.set(false);
		}

	}

	@Override
	public void addABSnapshotResult(Integer serventId, ABSnapshotResult abSnapshotResult) {
		collectedABValues.put(serventId, abSnapshotResult);
	}
	
	@Override
	public void startCollecting() {
		boolean oldValue = this.collecting.getAndSet(true);
		
		if (oldValue == true) {
			AppConfig.timestampedErrorPrint("Tried to start collecting before finished with previous.");
		}
	}
	
	@Override
	public void stop() {
		working = false;
	}

}
