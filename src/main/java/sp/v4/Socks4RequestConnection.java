package sp.v4;

import java.net.InetAddress;
import java.net.UnknownHostException;

import sp.SocksCommandType;

public class Socks4RequestConnection {

	public byte rawCommand;
	public byte[] rawPortNumber = new byte[2];
	public byte[] rawDestionationAddress = new byte[4];
	// public byte[] rawUserId;

	public SocksCommandType command;
	public String userId;
	public String destinationAddress;
	public String destinationDomain;
	public int destinationPort;

	public void parseRawData() throws UnknownHostException {
		command = SocksCommandType.parseInt(rawCommand);
		destinationAddress = InetAddress.getByAddress(rawDestionationAddress).getHostAddress();
		destinationPort = 0x0000FF00 & ((int) rawPortNumber[0] << 8) | 0x000000FF & (int) rawPortNumber[1];
	}

	@Override
	public String toString() {
		return command + "(" + destinationAddress + "|" + destinationDomain + ":" + destinationPort + ")";
	}

}
