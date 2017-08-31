package fr.bmartel.helloworld.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class TestUtils {

    public static byte[] buildApdu(byte[] command, byte[] data) {
        byte[] apdu = new byte[command.length + data.length + 1];
        System.arraycopy(command, 0, apdu, 0, command.length);
        apdu[command.length] = (byte) data.length;
        System.arraycopy(data, 0, apdu, command.length + 1, data.length);
        return apdu;
    }

    public static void logData(byte[] data) {
        System.out.println(Arrays.toString(data));
    }

    public static int getInt(byte[] data) {
        return (((data[0] & 0xff) << 8) | (data[1] & 0xff));
    }

    public static byte[] getByte(int data) {
        byte[] out = ByteBuffer.allocate(4).putInt(data).array();
        return new byte[]{out[2], out[3]};
    }

}
