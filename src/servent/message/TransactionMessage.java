package servent.message;

import app.AppConfig;
import app.ServentInfo;
import app.causality.VectorClock;
import app.snapshot_bitcake.bitcake_manager.BitcakeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bitcake transaction. We are sending some bitcakes to another node.
 * 
 * @author bmilojkovic
 *
 */
public class TransactionMessage extends CausalMessage {

	private static final long serialVersionUID = -333251402058492901L;

	private transient BitcakeManager bitcakeManager;
	
	public TransactionMessage(ServentInfo sender, ServentInfo receiver, int amount, BitcakeManager bitcakeManager, VectorClock senderVectorClock) {
		super(MessageType.TRANSACTION, sender, receiver, String.valueOf(amount),  senderVectorClock);
		this.bitcakeManager = bitcakeManager;
	}
	public TransactionMessage(ServentInfo sender, ServentInfo receiver, List<ServentInfo> routeList, String message, VectorClock senderVectorClock, int messageId,BitcakeManager bitcakeManager) {
		super(MessageType.TRANSACTION, sender, receiver, routeList,message,  senderVectorClock, messageId);
		this.bitcakeManager = bitcakeManager;
	}

	@Override
	public Message makeMeASender() {
		ServentInfo newRouteItem = AppConfig.myServentInfo;

		List<ServentInfo> newRouteList = new ArrayList<>(super.getRoute());
		newRouteList.add(newRouteItem);
		return new TransactionMessage(getOriginalSenderInfo(), getReceiverInfo(), newRouteList, getMessageText(), getSenderVectorClock(), getMessageId(), this.getBitcakeManager());
	}

	/**
	 * We want to take away our amount exactly as we are sending, so our snapshots don't mess up.
	 * This method is invoked by the sender just before sending, and with a lock that guarantees
	 * that we are white when we are doing this in Chandy-Lamport.
	 */

	@Override
	public void sendEffect() {
		int amount = Integer.parseInt(getMessageText());
		bitcakeManager.takeSomeBitcakes(amount, this);
	}

	public BitcakeManager getBitcakeManager() {
		return bitcakeManager;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TransactionMessage) {
			TransactionMessage other = (TransactionMessage)obj;

			if (getMessageId() == other.getMessageId() &&
					getOriginalSenderInfo().getId() == other.getOriginalSenderInfo().getId()) {
				return true;
			}
		}

		return false;
	}

	public void setBitcakeManager(BitcakeManager bitcakeManager) {
		this.bitcakeManager = bitcakeManager;
	}
}
