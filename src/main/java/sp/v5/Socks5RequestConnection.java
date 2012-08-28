package sp.v5;

import java.net.InetAddress;
import java.net.UnknownHostException;

import sp.SocksCommandType;

public class Socks5RequestConnection {

	public byte rawCommand;
	public byte[] rawPortNumber = new byte[2];
	public byte rawAddressType;
	public byte[] rawDestionationAddress;

	public SocksCommandType command;
	public String destinationAddress;
	public String destinationDomain;
	public AddressType destinationType;
	public int destinationPort;

	//public byte[] rawRequestFrame;

	public void parseRawData() throws UnknownHostException {
		command = SocksCommandType.parseInt(rawCommand);
		destinationPort = 0x0000FF00 & ((int) rawPortNumber[0] << 8) | 0x000000FF & (int) rawPortNumber[1];
		destinationType = AddressType.parseInt(rawAddressType);
		switch (destinationType) {
		case IPV4:
			destinationAddress = InetAddress.getByAddress(rawDestionationAddress).getHostAddress();
			break;

		case IPV6:
			destinationAddress = InetAddress.getByAddress(rawDestionationAddress).getHostAddress();
			break;

		default:
			break;
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(command).append("(").append(destinationType).append(" ");
		switch (destinationType) {
		case IPV4:
		case IPV6:
			buf.append(destinationAddress);
			break;
		case DOMAIN:
			buf.append(destinationDomain);
			break;
		default:
			break;
		}
		buf.append(":").append(destinationPort).append(")");
		return buf.toString();
	}
}
