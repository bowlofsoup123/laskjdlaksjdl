package servent.message.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import app.AppConfig;
import app.ServentInfo;
import app.causality.Causality;
import servent.message.CausalMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TransactionMessage;

/**
 * This worker sends a message asynchronously. Doing this in a separate thread
 * has the added benefit of being able to delay without blocking main or somesuch.
 * 
 * @author bmilojkovic
 *
 */
public class DelayedMessageSender implements Runnable {

	private Message messageToSend;
	
	public DelayedMessageSender(Message messageToSend) {
		this.messageToSend = messageToSend;
	}
	
	public void run() {

		try {
			Thread.sleep((long)(Math.random() * 1000) + 500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		ServentInfo receiverInfo = messageToSend.getReceiverInfo();
		
		if (MessageUtil.MESSAGE_UTIL_PRINTING) {
			AppConfig.timestampedStandardPrint("Sending message " + messageToSend);
		}
		
		try {
				Socket sendSocket = new Socket(receiverInfo.getIpAddress(), receiverInfo.getListenerPort());
				
				ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
				synchronized (Causality.incLock) {
					CausalMessage msg = ((CausalMessage) messageToSend);
					msg.setSenderVectorClock(Causality.copyClock());
					Causality.incrementSendClock(msg.getReceiverInfo().getId());
				}
				oos.writeObject(messageToSend);
				oos.flush();

				sendSocket.close();
//					AppConfig.timestampedErrorPrint("Vector clock in sent message: " + ((CausalMessage) messageToSend).getSenderVectorClock() + ", " + messageToSend);

				if (messageToSend.getMessageType() == MessageType.TRANSACTION) {
					TransactionMessage tmsg = (TransactionMessage) messageToSend;
					tmsg.sendEffect();
				} else {
					messageToSend.sendEffect();
				}
//			}
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Couldn't send message: " + messageToSend.toString());
		}
	}
	
}
