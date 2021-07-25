package azstudio;

import java.io.DataOutputStream;
import java.io.IOException;

public class StringUtils {
	public static void writeString(DataOutputStream out, String s) throws IOException{
		out.write(s.getBytes("UTF-8"));
		out.write(0);
	}
}
