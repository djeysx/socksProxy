package sp;

import java.nio.charset.Charset;

public class SocksContants {

	public static final byte SOCKS_VERSION_4 = 0x04;
	public static final byte SOCKS_VERSION_5 = 0x05;
	
	public static final byte AUTH_METHOD_NOAUTH = 0x00;
	public static final byte AUTH_METHOD_GSSAPI = 0x01;
	public static final byte AUTH_METHOD_USERPASSWORD = 0x02;
	
	public static final byte ADDRESS_TYPE_IPV4 = 0x01;
	public static final byte ADDRESS_TYPE_DOMAIN = 0x03;
	public static final byte ADDRESS_TYPE_IPV6 = 0x04;
	
	public static final byte COMMAND_TCP_STREAM = 0x01;
	public static final byte COMMAND_TCP_BINDING = 0x02;
	public static final byte COMMAND_UDP = 0x03;

	public static final byte RESPONSE4_STATUS_GRANTED = 0x5a;
	public static final byte RESPONSE4_STATUS_REJECTED = 0x5b; // or failed

	public static final byte RESPONSE5_STATUS_GRANTED = 0x00;
	public static final byte RESPONSE5_STATUS_CONNECTION_NOT_ALLOWED = 0x02; 
	public static final byte RESPONSE5_STATUS_HOST_UNREACHABLE = 0x04; 
	public static final byte RESPONSE5_STATUS_CONNECTION_REFUSED = 0x05; 
	public static final byte RESPONSE5_STATUS_COMMAND_NOT_SUPPORTED = 0x07; 

	public static final Charset CHARSET_ISO8859_1 = Charset.forName("ISO8859-1");
	
}
