package app.snapshot_bitcake.snapshot_colector;

import app.snapshot_bitcake.bitcake_manager.BitcakeManager;
import app.snapshot_bitcake.result.ABSnapshotResult;
import app.snapshot_bitcake.snapshot_colector.SnapshotCollector;

/**
 * This class is used if the user hasn't specified a snapshot type in config.
 * 
 * @author bmilojkovic
 *
 */
public class NullSnapshotCollector implements SnapshotCollector {

	@Override
	public void run() {}

	@Override
	public void stop() {}

	@Override
	public BitcakeManager getBitcakeManager() {
		return null;
	}

	@Override
	public void addABSnapshotResult(Integer serventId, ABSnapshotResult abSnapshotResult) {}


	@Override
	public void startCollecting() {}

}
