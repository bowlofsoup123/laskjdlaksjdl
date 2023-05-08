package app.snapshot_bitcake.bitcake_manager;

import app.snapshot_bitcake.snapshot_colector.SnapshotCollector;
import servent.message.Message;

/**
 * Describes a bitcake manager. These classes will have the methods
 * for handling snapshot recording and sending info to a collector.
 * 
 * @author bmilojkovic
 *
 */
public interface BitcakeManager {

	public void takeSomeBitcakes(int amount, Message message);
	public void addSomeBitcakes(int amount, Message message);
	public int getCurrentBitcakeAmount();
	public void tokenEvent(int collectorId, SnapshotCollector snapshotCollector);
	
}
