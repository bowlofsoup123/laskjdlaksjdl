package cli.command;

import app.AppConfig;
import app.snapshot_bitcake.snapshot_colector.SnapshotCollector;
import servent.message.CausalMessage;
import servent.message.TokenMessage;
import servent.message.util.MessageUtil;

public class BitcakeInfoCommand implements CLICommand {

	private SnapshotCollector collector;
	
	public BitcakeInfoCommand(SnapshotCollector collector) {
		this.collector = collector;
	}
	
	@Override
	public String commandName() {
		return "bitcake_info";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("Started collecting!");
		collector.startCollecting();

		CausalMessage tokenMessage = new TokenMessage(AppConfig.myServentInfo, null, null, AppConfig.myServentInfo.getId());

		for(Integer neighbourId: AppConfig.myServentInfo.getNeighbors()){
			tokenMessage = (CausalMessage)tokenMessage.changeReceiver(neighbourId);
			MessageUtil.sendMessage(tokenMessage.makeMeASender());
		}
	}
}
