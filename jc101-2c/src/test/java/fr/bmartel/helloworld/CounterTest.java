package fr.bmartel.helloworld;

import fr.bmartel.helloworld.util.TestUtils;
import javacard.framework.ISO7816;
import org.junit.Before;
import org.junit.Test;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CounterTest extends JavaCardTest {

    private final static byte[] CMD_BALANCE = new byte[]{0x00, 0x02, 0x00, 0x00};
    private final static byte[] CMD_CREDIT = new byte[]{0x00, 0x04, 0x00, 0x00};
    private final static byte[] CMD_DEBIT = new byte[]{0x00, 0x06, 0x00, 0x00};

    private final static byte[] MAX_BALANCE = new byte[]{0x27, 0x10};
    private final static byte[] MAX_DEBIT = new byte[]{0x03, (byte) 0xE8};

    private final static byte[] NEGATIVE_CREDIT = new byte[]{(byte) 0xFF, (byte) 0xFF};
    private final static byte[] MAX_CREDIT = new byte[]{0x13, (byte) 0x88};
    private final static byte[] OVERFLOW_CREDIT = new byte[]{0x13, (byte) 0x89};

    private final static byte[] BAD_CMD = new byte[]{0x00, 0x07, 0x00, 0x00};

    private void resetBalance() throws CardException {
        CommandAPDU commandAPDU = new CommandAPDU(CMD_BALANCE);
        ResponseAPDU response = transmitCommand(commandAPDU);
        assertEquals(0x9000, response.getSW());
        assertEquals(2, response.getData().length);
        int currentCredit = ((response.getData()[0] & 0xFF) << 8) + (response.getData()[1] & 0xFF);

        if (currentCredit != 0) {
            int it = currentCredit / (((MAX_DEBIT[0] & 0xFF) << 8) + (MAX_DEBIT[1] & 0xFF));

            for (int i = 0; i < it; i++) {
                commandAPDU = new CommandAPDU(TestUtils.buildApdu(CMD_DEBIT, MAX_DEBIT));
                response = transmitCommand(commandAPDU);
                assertEquals(0x9000, response.getSW());
            }

            int remain = currentCredit % 1000;

            if (remain > 0) {
                byte[] data = ByteBuffer.allocate(4).putInt(remain).array();
                commandAPDU = new CommandAPDU(TestUtils.buildApdu(CMD_DEBIT, new byte[]{data[2], data[3]}));
                response = transmitCommand(commandAPDU);
                assertEquals(0x9000, response.getSW());
            }
            assertArrayEquals(new byte[]{0x00, 0x00}, response.getData());
        }
    }

    @Before
    public void initTest() throws CardException {
        TestSuite.setup();
        resetBalance();
    }

    private ResponseAPDU getBalance() throws CardException {
        CommandAPDU commandAPDU = new CommandAPDU(CMD_BALANCE);
        ResponseAPDU response = transmitCommand(commandAPDU);
        assertEquals(0x9000, response.getSW());
        return response;
    }

    private void sendCmdBatch(byte[] cmd, byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        CommandAPDU commandAPDU = new CommandAPDU(TestUtils.buildApdu(cmd, data));
        ResponseAPDU response = transmitCommand(commandAPDU);
        assertEquals(expectedSw, response.getSW());
        assertArrayEquals(expectedResponse, response.getData());
    }

    private void sendCredit(byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        sendCmdBatch(CMD_CREDIT, data, expectedSw, expectedResponse);
    }

    private void sendDebit(byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        sendCmdBatch(CMD_DEBIT, data, expectedSw, expectedResponse);
    }

    @Test
    public void balanceTest() throws CardException {
        assertArrayEquals(new byte[]{0x00, 0x00}, getBalance().getData());
    }

    @Test
    public void creditTest() throws CardException {
        sendCredit(new byte[]{0x00, 0x05}, 0x9000, new byte[]{0x00, 0x05});
        assertArrayEquals(new byte[]{0x00, 0x05}, getBalance().getData());
    }

    @Test
    public void creditNegativeTest() throws CardException {
        sendCredit(NEGATIVE_CREDIT, ISO7816.SW_WRONG_DATA, new byte[]{});
    }

    @Test
    public void creditOverflowTest() throws CardException {
        sendCredit(OVERFLOW_CREDIT, ISO7816.SW_WRONG_DATA, new byte[]{});
        sendCredit(MAX_CREDIT, 0x9000, MAX_CREDIT);
        assertArrayEquals(MAX_CREDIT, getBalance().getData());
    }

    @Test
    public void balanceOverflowTest() throws CardException {
        sendCredit(MAX_CREDIT, 0x9000, MAX_CREDIT);
        sendCredit(MAX_CREDIT, 0x9000, TestUtils.getByte(TestUtils.getInt(MAX_CREDIT) * 2));
        sendCredit(new byte[]{0x00, 0x01}, ISO7816.SW_WRONG_DATA, new byte[]{});
        assertArrayEquals(MAX_BALANCE, getBalance().getData());
    }

    @Test
    public void debitTest() throws CardException {
        sendCredit(new byte[]{0x00, 0x05}, 0x9000, new byte[]{0x00, 0x05});
        assertArrayEquals(new byte[]{0x00, 0x05}, getBalance().getData());
        sendDebit(new byte[]{0x00, 0x05}, 0x9000, new byte[]{0x00, 0x00});
        assertArrayEquals(new byte[]{0x00, 0x00}, getBalance().getData());
    }

    @Test
    public void debitNegativeTest() throws CardException {
        sendCredit(new byte[]{0x00, 0x05}, 0x9000, new byte[]{0x00, 0x05});
        assertArrayEquals(new byte[]{0x00, 0x05}, getBalance().getData());
        sendDebit(new byte[]{0x00, 0x06}, ISO7816.SW_WRONG_DATA, new byte[]{});
        assertArrayEquals(new byte[]{0x00, 0x05}, getBalance().getData());
    }

    @Test
    public void debitOverflowTest() throws CardException {
        sendDebit(new byte[]{0x00, 0x01}, ISO7816.SW_WRONG_DATA, new byte[]{});
        assertArrayEquals(new byte[]{0x00, 0x00}, getBalance().getData());
    }

    @Test
    public void badCmdTest() throws CardException {
        sendCmdBatch(BAD_CMD, new byte[]{}, ISO7816.SW_INS_NOT_SUPPORTED, new byte[]{});
    }
}