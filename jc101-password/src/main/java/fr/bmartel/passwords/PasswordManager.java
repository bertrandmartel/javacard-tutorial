package fr.bmartel.passwords;

import javacard.framework.*;

public class PasswordManager extends Applet {

    /**
     * INS for command that adds a password entry
     */
    public final static byte INS_ADD_PASSWORD_ENTRY = (byte) 0x30;

    /**
     * INS for command that retrieves a password entry
     */
    public final static byte INS_RETRIEVE_PASSWORD_ENTRY = (byte) 0x32;

    /**
     * INS for command that deletes a password entry
     */
    public final static byte INS_DELETE_PASSWORD_ENTRY = (byte) 0x34;

    /**
     * INS for command that lists all defined identifiers
     */
    public final static byte INS_LIST_IDENTIFIERS = (byte) 0x36;

    /**
     * Status word for a duplicate identifier
     */
    public final static short SW_DUPLICATE_IDENTIFIER = (short) 0x6A8A;

    /**
     * Status word for a failed allocation
     */
    public final static short SW_NOT_ENOUGH_MEMORY = (short) 0x6A84;

    public final static short SW_IDENTIFIER_NOT_FOUND = (short) 0x6A82;

    /**
     * Tag byte for identifiers
     */
    public final static byte TAG_IDENTIFIER = (byte) 0xF1;

    /**
     * Tag byte for user name records
     */
    public final static byte TAG_USERNAME = (byte) 0xF2;

    /**
     * Tag byte for password records
     */
    public final static byte TAG_PASSWORD = (byte) 0xF3;

    private PasswordEntry current;

    private PasswordManager() {
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new PasswordManager().register();
    }

    public void process(APDU apdu) {
        // Nothing particular to do on SELECT
        if (selectingApplet()) {
            return;
        }

        byte[] buf = apdu.getBuffer();
        switch (buf[ISO7816.OFFSET_INS]) {
            case INS_ADD_PASSWORD_ENTRY:
                processAddPasswordEntry();
                break;
            case INS_RETRIEVE_PASSWORD_ENTRY:
                processRetrievePasswordEntry();
                break;
            case INS_DELETE_PASSWORD_ENTRY:
                processDeletePasswordEntry();
                break;
            case INS_LIST_IDENTIFIERS:
                processListIdentifiers();
                break;
            default:
                // good practice: If you don't know the INS, say so:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    short checkTLV(byte[] buffer, short inOfs,
                   byte tag, short maxLen) {
        if (buffer[inOfs++] != tag)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        short len = buffer[inOfs++];
        if (len > maxLen)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        return (short) (inOfs + len);
    }

    void processAddPasswordEntry() {
        // Retrieves references to the APDU and its buffer
        APDU apdu = APDU.getCurrentAPDU();
        byte[] buf = APDU.getCurrentAPDUBuffer();

        // Checks the value of P1&P2
        if (Util.getShort(buf, ISO7816.OFFSET_P1) != 0)
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        // Checks the minimum length
        if ((short) (buf[ISO7816.OFFSET_LC] & 0xFF) < 3)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        // Receives data and checks its length
        if (apdu.setIncomingAndReceive() !=
                (short) (buf[ISO7816.OFFSET_LC] & 0xFF))
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        // Checks the identifier
        short ofsId = ISO7816.OFFSET_CDATA;
        short ofsUserName = checkTLV(buf, ofsId, TAG_IDENTIFIER, PasswordEntry.SIZE_ID);

        if (buf[ISO7816.OFFSET_LC] < (short) (ofsUserName - 3))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        // Checks the user name
        short ofsPassword = checkTLV(buf, ofsUserName, TAG_USERNAME, PasswordEntry.SIZE_USERNAME);

        if (buf[ISO7816.OFFSET_LC] < (short) (ofsPassword - 3))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        // Checks the password
        if (checkTLV(buf, ofsPassword, TAG_PASSWORD, PasswordEntry.SIZE_PASSWORD) !=
                (short) (ISO7816.OFFSET_CDATA + (short) (buf[ISO7816.OFFSET_LC] & 0xFF)))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        // Search the identifier in the current base
        if (PasswordEntry.search(buf, (short) (ofsId + 2), buf[(short) (ofsId + 1)]) != null)
            ISOException.throwIt(SW_DUPLICATE_IDENTIFIER);

        // Allocates and initializes a password entry
        JCSystem.beginTransaction();
        PasswordEntry pe = PasswordEntry.getInstance();
        pe.setId(buf, (short) (ofsId + 2), buf[(short) (ofsId + 1)]);
        pe.setUserName(buf, (short) (ofsUserName + 2), buf[(short) (ofsUserName + 1)]);
        pe.setPassword(buf, (short) (ofsPassword + 2), buf[(short) (ofsPassword + 1)]);
        JCSystem.commitTransaction();
    }

    void processDeletePasswordEntry() {
        // Retrieves references to the APDU and its buffer
        APDU apdu = APDU.getCurrentAPDU();
        byte[] buf = APDU.getCurrentAPDUBuffer();

        // Checks the value of P1&P2
        if (Util.getShort(buf, ISO7816.OFFSET_P1) != 0)
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        // Checks the minimum length
        if ((short) (buf[ISO7816.OFFSET_LC] & 0xFF) < 3)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        // Receives data and checks its length
        if (apdu.setIncomingAndReceive() != (short) (buf[ISO7816.OFFSET_LC] & 0xFF))
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        // Checks the identifier
        short ofsId = ISO7816.OFFSET_CDATA;
        if (checkTLV(buf, ISO7816.OFFSET_CDATA, TAG_IDENTIFIER, PasswordEntry.SIZE_ID) !=
                (short) (ISO7816.OFFSET_CDATA + (short) (buf[ISO7816.OFFSET_LC] & 0xFF)))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        // Search the identifier in the current base
        PasswordEntry pe = PasswordEntry.search(buf,
                (short) (ISO7816.OFFSET_CDATA + 2),
                buf[ISO7816.OFFSET_CDATA + 1]);
        if (pe == null)
            ISOException.throwIt(SW_IDENTIFIER_NOT_FOUND);

        PasswordEntry.delete(buf, (short) (ofsId + 2), buf[(short) (ofsId + 1)]);
    }

    void processRetrievePasswordEntry() {
        // Retrieves references to the APDU and its buffer
        APDU apdu = APDU.getCurrentAPDU();
        byte[] buf = APDU.getCurrentAPDUBuffer();

        // INITIAL CHECKS

        // Checks the value of P1&P2
        if (Util.getShort(buf, ISO7816.OFFSET_P1) != 0)
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        // Checks the minimum length
        if ((short) (buf[ISO7816.OFFSET_LC] & 0xFF) < 3)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        // Receives data and checks its length
        if (apdu.setIncomingAndReceive() !=
                (short) (buf[ISO7816.OFFSET_LC] & 0xFF))
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);


        // INTERPRETS AND CHECKS THE DATA

        // Checks the identifier
        if (checkTLV(buf, ISO7816.OFFSET_CDATA, TAG_IDENTIFIER, PasswordEntry.SIZE_ID) !=
                (short) (ISO7816.OFFSET_CDATA + (short) (buf[ISO7816.OFFSET_LC] & 0xFF)))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        // Search the identifier in the current base
        PasswordEntry pe = PasswordEntry.search(buf, (short) (ISO7816.OFFSET_CDATA + 2), buf[ISO7816.OFFSET_CDATA + 1]);
        if (pe == null)
            ISOException.throwIt(SW_IDENTIFIER_NOT_FOUND);

        // Builds the result, starting with the user name
        short outOfs = 0;
        buf[outOfs++] = TAG_USERNAME;
        byte len = pe.getUserName(buf, (short) (outOfs + 1));
        buf[outOfs++] = len;
        outOfs += len;

        // Builds the result, continuing with the password
        buf[outOfs++] = TAG_PASSWORD;
        len = pe.getPassword(buf, (short) (outOfs + 1));
        buf[outOfs++] = len;
        outOfs += len;

        // Sends the result
        apdu.setOutgoingAndSend((short) 0, outOfs);
    }

    void processListIdentifiers() {
        // Retrieves references to the APDU and its buffer
        APDU apdu = APDU.getCurrentAPDU();
        byte[] buf = APDU.getCurrentAPDUBuffer();

        // Checks P1 and initializes the "current" value
        if (buf[ISO7816.OFFSET_P1] == 0)
            current = PasswordEntry.getFirst();
        else if ((buf[ISO7816.OFFSET_P1] != 1) || (current == null))
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);


        // Builds the response
        short offset = 0;
        while (current != null) {
            // Checks that the identifier record fits in the APDU
            // WARNING: assumes a 256-byte APDU buffer
            byte len = current.getIdLength();
            if ((short) ((short) (offset + len) + 2) > 255)
                break;

            // Copies the identifier in the buffer
            buf[offset++] = TAG_IDENTIFIER;
            buf[offset++] = len;
            current.getId(buf, offset);

            // Gest to the next record
            offset += len;
            current = current.getNext();
        }
        apdu.setOutgoingAndSend((short) 0, offset);
    }
}