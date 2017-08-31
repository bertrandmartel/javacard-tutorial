package fr.bmartel.passwords;

import fr.bmartel.helloworld.util.TestUtils;
import javacard.framework.ISO7816;
import org.junit.Before;
import org.junit.Test;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PasswordManagerTest extends JavaCardTest {

    private final static byte[] CMD_ADD_PASSWORD = new byte[]{0x00, 0x30, 0x00, 0x00};
    private final static byte[] CMD_GET_PASSWORD = new byte[]{0x00, 0x32, 0x00, 0x00};
    private final static byte[] CMD_DELETE_PASSWORD = new byte[]{0x00, 0x34, 0x00, 0x00};
    private final static byte[] CMD_LIST_ID = new byte[]{0x00, 0x36, 0x00, 0x00};

    private final static Password DATA_ENTRY_VALID = new Password(
            new byte[]{(byte) 0xF1, 0x04, 0x48, 0x6F, 0x6D, 0x65},
            new byte[]{(byte) 0xF2, 0x03, 0x62, 0x6F, 0x62},
            new byte[]{(byte) 0xF3, 0x04, 0x70, 0x61, 0x73, 0x73});

    private final static Password DATA_ENTRY_VALID1 = new Password(
            new byte[]{(byte) 0xF1, 0x04, 0x49, 0x6F, 0x6D, 0x65},
            new byte[]{(byte) 0xF2, 0x03, 0x62, 0x6F, 0x62},
            new byte[]{(byte) 0xF3, 0x04, 0x70, 0x61, 0x73, 0x73});

    private final static Password DATA_ENTRY_VALID2 = new Password(
            new byte[]{(byte) 0xF1, 0x04, 0x50, 0x6F, 0x6D, 0x65},
            new byte[]{(byte) 0xF2, 0x03, 0x62, 0x6F, 0x62},
            new byte[]{(byte) 0xF3, 0x04, 0x70, 0x61, 0x73, 0x73});

    private final static Password DATA_ENTRY_VALID3 = new Password(
            new byte[]{(byte) 0xF1, 0x04, 0x51, 0x6F, 0x6D, 0x65},
            new byte[]{(byte) 0xF2, 0x03, 0x62, 0x6F, 0x62},
            new byte[]{(byte) 0xF3, 0x04, 0x70, 0x61, 0x73, 0x73});

    //invalid data for addition
    private final static byte[] DATA_ENTRY_INVALID_TAG1 = new byte[]{0x01, 0x02, 0x03, 0x04};
    private final static byte[] DATA_ENTRY_INVALID_TAG2 = new byte[]{(byte) 0xF1, 0x04, 0x48, 0x6F, 0x6D, 0x65};
    private final static byte[] DATA_ENTRY_INVALID_TAG3 = new byte[]{(byte) 0xF1, 0x04, 0x48, 0x6F, 0x6D, 0x65, (byte) 0xF2, 0x03, 0x62, 0x6F, 0x62};
    private final static byte[] DATA_ENTRY_INVLID_LENGTH1 = new byte[]{(byte) 0xF1, 0x03, 0x48, 0x6F, 0x6D, 0x65, (byte) 0xF2, 0x03, 0x62, 0x6F, 0x62, (byte) 0xF3, 0x04, 0x70, 0x61, 0x73, 0x73};
    private final static byte[] DATA_ENTRY_INVLID_LENGTH2 = new byte[]{(byte) 0xF1, 0x04, 0x48, 0x6F, 0x6D, 0x65, (byte) 0xF2, 0x02, 0x62, 0x6F, 0x62, (byte) 0xF3, 0x04, 0x70, 0x61, 0x73, 0x73};
    private final static byte[] DATA_ENTRY_INVLID_LENGTH3 = new byte[]{(byte) 0xF1, 0x04, 0x48, 0x6F, 0x6D, 0x65, (byte) 0xF2, 0x03, 0x62, 0x6F, 0x62, (byte) 0xF3, 0x02, 0x70, 0x61, 0x73, 0x73};
    private final static byte[] DATA_ENTRY_ID_OVERFLOW = new byte[]{(byte) 0xF1, 0x11, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x11, 0x12, (byte) 0xF2, 0x03, 0x62, 0x6F, 0x62, (byte) 0xF3, 0x04, 0x70, 0x61, 0x73, 0x73};
    private final static byte[] DATA_ENTRY_USERNAME_OVERFLOW = new byte[]{(byte) 0xF1, 0x04, 0x48, 0x6F, 0x6D, 0x65, (byte) 0xF2, 0x19, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, (byte) 0xF3, 0x04, 0x70, 0x61, 0x73, 0x73};
    private final static byte[] DATA_ENTRY_PASSWORD_OVERFLOW = new byte[]{(byte) 0xF1, 0x04, 0x48, 0x6F, 0x6D, 0x65, (byte) 0xF2, 0x03, 0x62, 0x6F, 0x62, (byte) 0xF3, 0x11, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x11, 0x12};

    private void sendAddPassword(byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        TestUtils.sendCmdBatch(this, CMD_ADD_PASSWORD, data, expectedSw, expectedResponse);
    }

    private void sendGetPassword(byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        TestUtils.sendCmdBatch(this, CMD_GET_PASSWORD, data, expectedSw, expectedResponse);
    }

    private void sendDeletePassword(byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        TestUtils.sendCmdBatch(this, CMD_DELETE_PASSWORD, data, expectedSw, expectedResponse);
    }

    private void sendListId(byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        TestUtils.sendCmdBatch(this, CMD_LIST_ID, data, expectedSw, expectedResponse);
    }

    private void deleteAllPassword() throws CardException {
        CommandAPDU commandAPDU = new CommandAPDU(TestUtils.buildApdu(CMD_LIST_ID, new byte[]{}));
        ResponseAPDU response = transmitCommand(commandAPDU);
        assertEquals(0x9000, response.getSW());
        if (response.getData().length > 0) {
            List<byte[]> ids = new ArrayList<>();
            int state = 0;
            int length = 0;
            int index = 0;
            byte[] current = new byte[]{};

            for (int i = 0; i < response.getData().length; i++) {
                switch (state) {
                    case 0:
                        if ((response.getData()[i] & 0xFF) == 0xF1) {
                            state = 1;
                        }
                        break;
                    case 1:
                        //set length
                        length = response.getData()[i] & 0xFF;
                        current = new byte[length + 2];
                        current[0] = (byte) 0xF1;
                        current[1] = response.getData()[i];
                        index = 2;
                        state = 2;
                        break;
                    case 2:
                        //set data
                        current[index++] = response.getData()[i];
                        length--;
                        if (length == 0) {
                            ids.add(current);
                            state = 0;
                        }
                }

            }

            for (int i = 0; i < ids.size(); i++) {
                commandAPDU = new CommandAPDU(TestUtils.buildApdu(CMD_DELETE_PASSWORD, ids.get(i)));
                response = transmitCommand(commandAPDU);
                assertEquals(0x9000, response.getSW());
            }

            commandAPDU = new CommandAPDU(TestUtils.buildApdu(CMD_LIST_ID, new byte[]{}));
            response = transmitCommand(commandAPDU);
            assertEquals(0x9000, response.getSW());
            assertEquals(0, response.getData().length);
        }
    }

    @Before
    public void initTest() throws NoSuchFieldException, IllegalAccessException, CardException {
        TestSuite.setup();
        deleteAllPassword();
    }

    @Test
    public void ListEmptyTest() throws CardException {
        sendListId(new byte[]{}, 0x9000, new byte[]{});
    }

    @Test
    public void addPasswordTest() throws CardException {
        sendListId(new byte[]{}, 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_VALID.getFullApdu(), 0x9000, new byte[]{});
        sendListId(new byte[]{}, 0x9000, DATA_ENTRY_VALID.getId());
    }

    @Test
    public void multipleAddPasswordTest() throws CardException {
        sendListId(new byte[]{}, 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_VALID.getFullApdu(), 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_VALID1.getFullApdu(), 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_VALID2.getFullApdu(), 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_VALID3.getFullApdu(), 0x9000, new byte[]{});
        sendListId(new byte[]{}, 0x9000, TestUtils.concatByteArray(DATA_ENTRY_VALID3.getId(),
                DATA_ENTRY_VALID2.getId(),
                DATA_ENTRY_VALID1.getId(),
                DATA_ENTRY_VALID.getId()));
    }

    @Test
    public void invalidAddTagTest() throws CardException {
        sendListId(new byte[]{}, 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_INVALID_TAG1, ISO7816.SW_DATA_INVALID, new byte[]{});
        sendAddPassword(DATA_ENTRY_INVALID_TAG2, ISO7816.SW_DATA_INVALID, new byte[]{});
        sendAddPassword(DATA_ENTRY_INVALID_TAG3, ISO7816.SW_DATA_INVALID, new byte[]{});
        sendListId(new byte[]{}, 0x9000, new byte[]{});
    }

    @Test
    public void invalidAddDataLengthTest() throws CardException {
        sendListId(new byte[]{}, 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_INVLID_LENGTH1, ISO7816.SW_DATA_INVALID, new byte[]{});
        sendAddPassword(DATA_ENTRY_INVLID_LENGTH2, ISO7816.SW_DATA_INVALID, new byte[]{});
        sendAddPassword(DATA_ENTRY_INVLID_LENGTH3, ISO7816.SW_DATA_INVALID, new byte[]{});
        sendListId(new byte[]{}, 0x9000, new byte[]{});
    }

    @Test
    public void invalidAddDataOverflowTest() throws CardException {
        sendListId(new byte[]{}, 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_ID_OVERFLOW, ISO7816.SW_DATA_INVALID, new byte[]{});
        sendAddPassword(DATA_ENTRY_USERNAME_OVERFLOW, ISO7816.SW_DATA_INVALID, new byte[]{});
        sendAddPassword(DATA_ENTRY_PASSWORD_OVERFLOW, ISO7816.SW_DATA_INVALID, new byte[]{});
        sendListId(new byte[]{}, 0x9000, new byte[]{});
    }

    @Test
    public void duplicateAddTest() throws CardException {
        sendListId(new byte[]{}, 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_VALID.getFullApdu(), 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_VALID.getFullApdu(), PasswordManager.SW_DUPLICATE_IDENTIFIER, new byte[]{});
        sendAddPassword(DATA_ENTRY_VALID.getFullApdu(), PasswordManager.SW_DUPLICATE_IDENTIFIER, new byte[]{});
        sendListId(new byte[]{}, 0x9000, DATA_ENTRY_VALID.getId());
    }

    @Test
    public void getPasswordTest() throws CardException {
        sendListId(new byte[]{}, 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_VALID.getFullApdu(), 0x9000, new byte[]{});
        sendGetPassword(DATA_ENTRY_VALID.getId(), 0x9000, DATA_ENTRY_VALID.getData());
        sendAddPassword(DATA_ENTRY_VALID1.getFullApdu(), 0x9000, new byte[]{});
        sendGetPassword(DATA_ENTRY_VALID1.getId(), 0x9000, DATA_ENTRY_VALID1.getData());
    }

    @Test
    public void getInvalidIdTest() throws CardException {
        sendListId(new byte[]{}, 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_VALID.getFullApdu(), 0x9000, new byte[]{});
        sendGetPassword(DATA_ENTRY_VALID1.getId(), PasswordManager.SW_IDENTIFIER_NOT_FOUND, new byte[]{});
    }

    @Test
    public void deleteIdTest() throws CardException {
        sendListId(new byte[]{}, 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_VALID.getFullApdu(), 0x9000, new byte[]{});
        sendDeletePassword(DATA_ENTRY_VALID.getId(), 0x9000, new byte[]{});
    }

    @Test
    public void deleteIdInvalidTest() throws CardException {
        sendListId(new byte[]{}, 0x9000, new byte[]{});
        sendAddPassword(DATA_ENTRY_VALID.getFullApdu(), 0x9000, new byte[]{});
        sendDeletePassword(DATA_ENTRY_VALID1.getId(), PasswordManager.SW_IDENTIFIER_NOT_FOUND, new byte[]{});
    }

    @Test
    public void intertwinedAddDeleteTest() throws CardException {
        for (int i = 0; i < 10; i++) {
            sendListId(new byte[]{}, 0x9000, new byte[]{});
            sendAddPassword(DATA_ENTRY_VALID.getFullApdu(), 0x9000, new byte[]{});
            sendListId(new byte[]{}, 0x9000, DATA_ENTRY_VALID.getId());
            sendDeletePassword(DATA_ENTRY_VALID.getId(), 0x9000, new byte[]{});
        }
    }
}