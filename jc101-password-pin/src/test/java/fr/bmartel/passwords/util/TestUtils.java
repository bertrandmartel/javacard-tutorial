package fr.bmartel.helloworld.util;

import com.licel.jcardsim.smartcardio.CardSimulator;
import fr.bmartel.passwords.JavaCardTest;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestUtils {

    private final static byte[] CMD_SET_PIN_CODE = new byte[]{0x00, 0x24, 0x00, (byte) 0x80};

    public final static byte[] TEST_PIN_CODE = new byte[]{0x61, 0x42, 0x63, 0x31, 0x32, 0x45, 0x34};

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

    /**
     * Add n x 0x00 offset to a byte array (at the beginning)
     *
     * @param offset number of byte to set to 0x00 before the data
     * @param data   the data
     * @return byte array with offset before data
     */
    public static byte[] addOffset(short offset, byte[] data) {
        byte[] resp = new byte[data.length + offset];
        for (int i = 0; i < offset; i++) {
            resp[i] = 0x00;
        }
        System.arraycopy(resp, offset, data, 0, data.length);
        return resp;
    }

    public static byte[] concatByteArray(byte[]... data) {
        int length = 0;
        for (byte[] item : data) {
            length += item.length;
        }
        byte[] resp = new byte[length];
        int offset = 0;
        for (byte[] item : data) {
            System.arraycopy(item, 0, resp, offset, item.length);
            offset += item.length;
        }
        return resp;
    }

    /**
     * Get class field by reflection.
     *
     * @param object class
     * @param name   field name
     * @return field
     */
    public static Field getField(Class object, String name) {
        for (Field f : object.getDeclaredFields()) {
            f.setAccessible(true);
            if (f != null && f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    public static void sendCmdBatch(JavaCardTest card, byte[] cmd, byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        CommandAPDU commandAPDU = new CommandAPDU(TestUtils.buildApdu(cmd, data));
        ResponseAPDU response = card.transmitCommand(commandAPDU);
        assertEquals(expectedSw, response.getSW());
        assertArrayEquals(expectedResponse, response.getData());
    }

    public static void setPinCode(CardSimulator card, byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        CommandAPDU commandAPDU = new CommandAPDU(TestUtils.buildApdu(CMD_SET_PIN_CODE, data));
        ResponseAPDU response = card.transmitCommand(commandAPDU);
        assertEquals(expectedSw, response.getSW());
        assertArrayEquals(expectedResponse, response.getData());
    }

    public static void setPinCode(Card card, byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        CommandAPDU commandAPDU = new CommandAPDU(TestUtils.buildApdu(CMD_SET_PIN_CODE, data));
        ResponseAPDU response = card.getBasicChannel().transmit(commandAPDU);
        //assertEquals(expectedSw, response.getSW());
        //assertArrayEquals(expectedResponse, response.getData());
    }
}
