package sp;

public enum SocksCommandType {
	TCP_STREAM(1), TCP_BINDING(2), UDP(3);

	private int commandCode;

	private SocksCommandType(int commandCode) {
		this.commandCode = commandCode;
	}

	public int getCommandCode() {
		return commandCode;
	}

	public static SocksCommandType parseInt(int v) {
		switch (v) {
		case 1:
			return TCP_STREAM;
		case 2:
			return TCP_BINDING;
		case 3:
			return UDP;

		default:
			return null;
		}
	}

}
