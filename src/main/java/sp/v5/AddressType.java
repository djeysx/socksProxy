package sp.v5;

public enum AddressType {
	IPV4(1), DOMAIN(3), IPV6(4);

	private int addressCode;

	private AddressType(int code) {
		this.addressCode = code;
	}

	public int getAddressCode() {
		return addressCode;
	}

	public static AddressType parseInt(int v) {
		switch (v) {
		case 1:
			return IPV4;
		case 3:
			return DOMAIN;
		case 4:
			return IPV6;

		default:
			return null;
		}
	}

}
