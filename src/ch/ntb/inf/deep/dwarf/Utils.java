package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Utils {
	/**
	 * Reads an unsigned integer from {@code in}.
	 */
	public static int readUnsignedLeb128(ByteBuffer in) {
		int result = 0;
		int cur;
		int count = 0;

		do {
			cur = in.get() & 0xff;
			result |= (cur & 0x7f) << (count * 7);
			count++;
		} while (((cur & 0x80) == 0x80) && count < 5);

		if ((cur & 0x80) == 0x80) {
			throw new RuntimeException("invalid LEB128 sequence");
		}

		return result;
	}
	
    public static int readSignedLeb128(ByteBuffer in) {
        int result = 0;
        int cur;
        int count = 0;
        int signBits = -1;
        do {
            cur = in.get() & 0xff;
            result |= (cur & 0x7f) << (count * 7);
            signBits <<= 7;
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);
        if ((cur & 0x80) == 0x80) {
            throw new RuntimeException("invalid LEB128 sequence");
        }
        // Sign extend if appropriate
        if (((signBits >> 1) & result) != 0 ) {
            result |= signBits;
        }
        return result;
    }

	public static void writeUnsignedLeb128(ByteBuffer out, int value) {
		int remaining = value >>> 7;
		while (remaining != 0) {
			out.put((byte) ((value & 0x7f) | 0x80));
			value = remaining;
			remaining >>>= 7;
		}
		out.put((byte) (value & 0x7f));
	}
	
    public static void writeSignedLeb128(ByteBuffer out, int value) {
        int remaining = value >> 7;
        boolean hasMore = true;
        int end = ((value & Integer.MIN_VALUE) == 0) ? 0 : -1;
        while (hasMore) {
            hasMore = (remaining != end)
                    || ((remaining & 1) != ((value >> 6) & 1));
            out.put((byte) ((value & 0x7f) | (hasMore ? 0x80 : 0)));
            value = remaining;
            remaining >>= 7;
        }
    }

	public static List<String> parseStringTable(ByteBuffer buf) {
		List<String> strings = new ArrayList<String>();
		while (buf.get(buf.position()) != 0) {
			strings.add(parseString(buf));
		}
		buf.get(); // Read 0
		return strings;
	}

	public static String parseString(ByteBuffer buf) {
		String str = "";
		byte val = buf.get();
		while (val != 0) {
			str += (char) val;
			val = buf.get();
		}
		return str;
	}

	public static byte[] serialize(String... strs) {
		if (strs == null || strs.length == 0) {
			return new byte[] { 0 };
		}
		String joinedString = String.join("\0", strs) + '\0';
		return serialize(joinedString);
	}
	
	public static byte[] serialize(String str) {
		return (str + '\0').getBytes();
	}
}