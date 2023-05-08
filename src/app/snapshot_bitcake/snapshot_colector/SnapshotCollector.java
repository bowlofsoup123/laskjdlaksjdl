package app.snapshot_bitcake.snapshot_colector;

import app.Cancellable;
import app.snapshot_bitcake.bitcake_manager.BitcakeManager;
import app.snapshot_bitcake.result.ABSnapshotResult;

/**
 * Describes a snapshot collector. Made not-so-flexibly for readability.
 * 
 * @author bmilojkovic
 *
 */
public interface SnapshotCollector extends Runnable, Cancellable {

	BitcakeManager getBitcakeManager();

	void addABSnapshotResult(Integer serventId, ABSnapshotResult snapshotResult);

	void startCollecting();

}