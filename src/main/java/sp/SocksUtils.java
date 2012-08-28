package sp;

public class SocksUtils {

	public static String bytes2String(byte[] ba) {
		StringBuilder sb = new StringBuilder();
		for (byte b : ba) {
			sb.append(0x000000FF & (int) b).append(",");
		}
		return sb.toString();
	}

}
