package fr.bmartel.passwords;

import javacard.framework.*;
import org.globalplatform.GPSystem;

public class PasswordPinManager extends Applet {

    private OwnerPIN pin;

    private final static short SW_WRONG_PIN = (short) 0x63c0;

    /**
     * INS byte for the command that verifies the PIN
     * (from ISO7816-4)
     */
    public final static byte INS_VERIFY = (byte) 0x20;

    /**
     * PIN try limit
     */
    public final static byte PIN_TRY_LIMIT = (byte) 3;

    /**
     * PIN Maximum size
     */
    public final static byte PIN_MAX_SIZE = (byte) 16;

    /**
     * INS byte for the command that changes the PIN
     * (from ISO7816-4)
     */
    public final static byte INS_CHANGE_REFERENCE_DATA = (byte) 0x24;

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

    public static final short SW_IDENTIFIER_NOT_FOUND = (short) 0x6A82;

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

    private PasswordPinEntry current;

    private PasswordPinManager() {
        pin = new OwnerPIN(PIN_TRY_LIMIT, PIN_MAX_SIZE);
    }

    public static void install
            (byte[] bArray, short bOffset, byte bLength) {
        new PasswordPinManager().register();
    }

    public void process(APDU apdu) {
        // Nothing particular to do on SELECT
        if (selectingApplet()) {
            return;
        }

        byte[] buf = apdu.getBuffer();

        // First, switches on the application's state,
        // as managed by GlobalPlatform
        switch (GPSystem.getCardContentState()) {

            // In the SELECTABLE state,
            // the application is not personalized yet
            case GPSystem.APPLICATION_SELECTABLE:
                switch (buf[ISO7816.OFFSET_INS]) {
                    case INS_CHANGE_REFERENCE_DATA:
                        processChangeReferenceData();
                        break;
                    default:
                        // If you don't know the INStruction, say so:
                        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                }
                break;
            case GPSystem.CARD_SECURED:
                //Resets the identifier list's current reference if required
                if (buf[ISO7816.OFFSET_INS] != INS_LIST_IDENTIFIERS)
                    current = null;

                switch (buf[ISO7816.OFFSET_INS]) {
                    case INS_ADD_PASSWORD_ENTRY:
                        checkAuthentication();
                        processAddPasswordEntry();
                        break;
                    case INS_RETRIEVE_PASSWORD_ENTRY:
                        checkAuthentication();
                        processRetrievePasswordEntry();
                        break;
                    case INS_DELETE_PASSWORD_ENTRY:
                        checkAuthentication();
                        processDeletePasswordEntry();
                        break;
                    case INS_LIST_IDENTIFIERS:
                        checkAuthentication();
                        processListIdentifiers();
                        break;
                    case INS_VERIFY:
                        processVerify();
                        break;
                    case INS_CHANGE_REFERENCE_DATA:
                        processChangeReferenceData();
                        break;
                    default:
                        // good practice: If you don't know the INStruction, say so:
                        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                }
                break;
            default:
                ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
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
        short ofsUserName = checkTLV(buf, ofsId, TAG_IDENTIFIER, PasswordPinEntry.SIZE_ID);

        if (buf[ISO7816.OFFSET_LC] < (short) (ofsUserName - 3))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        // Checks the user name
        short ofsPassword = checkTLV(buf, ofsUserName, TAG_USERNAME, PasswordPinEntry.SIZE_USERNAME);
        if (buf[ISO7816.OFFSET_LC] < (short) (ofsPassword - 3))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        // Checks the password
        if (checkTLV(buf, ofsPassword, TAG_PASSWORD, PasswordPinEntry.SIZE_PASSWORD) !=
                (short) (ISO7816.OFFSET_CDATA + (short) (buf[ISO7816.OFFSET_LC] & 0xFF)))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        // Search the identifier in the current base
        if (PasswordPinEntry.search(buf, (short) (ofsId + 2), buf[(short) (ofsId + 1)]) != null)
            ISOException.throwIt(SW_DUPLICATE_IDENTIFIER);

        // Allocates and initializes a password entry
        JCSystem.beginTransaction();
        PasswordPinEntry pe = PasswordPinEntry.getInstance();
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
        if (checkTLV(buf, ISO7816.OFFSET_CDATA, TAG_IDENTIFIER, PasswordPinEntry.SIZE_ID) !=
                (short) (ISO7816.OFFSET_CDATA + (short) (buf[ISO7816.OFFSET_LC] & 0xFF)))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        // Search the identifier in the current base
        PasswordPinEntry pe = PasswordPinEntry.search(buf, (short) (ISO7816.OFFSET_CDATA + 2), buf[ISO7816.OFFSET_CDATA + 1]);
        if (pe == null)
            ISOException.throwIt(SW_IDENTIFIER_NOT_FOUND);

        PasswordPinEntry.delete(buf, (short) (ofsId + 2), buf[(short) (ofsId + 1)]);
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
        if (apdu.setIncomingAndReceive() != (short) (buf[ISO7816.OFFSET_LC] & 0xFF))
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);


        // INTERPRETS AND CHECKS THE DATA

        // Checks the identifier
        if (checkTLV(buf, ISO7816.OFFSET_CDATA, TAG_IDENTIFIER, PasswordPinEntry.SIZE_ID) !=
                (short) (ISO7816.OFFSET_CDATA + (short) (buf[ISO7816.OFFSET_LC] & 0xFF)))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        // Search the identifier in the current base
        PasswordPinEntry pe = PasswordPinEntry.search(buf, (short) (ISO7816.OFFSET_CDATA + 2), buf[ISO7816.OFFSET_CDATA + 1]);
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
            current = PasswordPinEntry.getFirst();
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

    void processVerify() {
        // Retrieves references to the APDU and its buffer
        APDU apdu = APDU.getCurrentAPDU();
        byte[] buf = APDU.getCurrentAPDUBuffer();

        // INITIAL CHECKS
        // Expect P1=00 and P2=80
        if (buf[ISO7816.OFFSET_P2] != (byte) 0x80)
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);

        // First verifies that the PIN is not blocked
        if (pin.getTriesRemaining() == 0)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        // If no input data, simply return the number of remaining tries
        if (buf[ISO7816.OFFSET_LC] == 0) {
            if (pin.isValidated())
                return;
            else
                ISOException.throwIt((short) (SW_WRONG_PIN + pin.getTriesRemaining()));
        }

        // Receives data and checks it
        short len = apdu.setIncomingAndReceive();
        verifyPIN(buf, ISO7816.OFFSET_CDATA, (byte) len);
    }

    void verifyPIN(byte[] buffer, short index, byte len) {
        if (len > PIN_MAX_SIZE)
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        if (!pin.check(buffer, index, len)) {
            if (pin.getTriesRemaining() == 0)
                ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            else
                ISOException.throwIt((short) (SW_WRONG_PIN + pin.getTriesRemaining()));
        }
    }

    void processChangeReferenceData() {
        // Retrieves references to the APDU and its buffer
        APDU apdu = APDU.getCurrentAPDU();
        byte[] buf = APDU.getCurrentAPDUBuffer();

        // INITIAL CHECKS

        // Checks the value of P2
        if (buf[ISO7816.OFFSET_P2] != (byte) 0x80)
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);

        byte p1 = buf[ISO7816.OFFSET_P1];
        switch (p1) {
            case 0:
                if (GPSystem.getCardContentState() != GPSystem.APPLICATION_SELECTABLE)
                    ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                break;
            case 1:
                if (GPSystem.getCardContentState() != GPSystem.CARD_SECURED)
                    ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        // Receives data and checks it
        short len = apdu.setIncomingAndReceive();
        if (len < 2)
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        short index = ISO7816.OFFSET_CDATA;

        // If there is a PIN presentation, checks it
        if (p1 == 1) {
            byte oldPinLen = buf[index++];
            if (len < (short) (oldPinLen + 3))
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            verifyPIN(buf, index, oldPinLen);
            index += oldPinLen;
        }

        // In all cases, checks the new PIN value
        byte newPinLen = buf[index++];
        if (len != (short) (index + (short) (newPinLen - ISO7816.OFFSET_CDATA)))
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        if (newPinLen > PIN_MAX_SIZE)
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);

        // If everything is OK, update the PIN value
        pin.update(buf, index, newPinLen);

        // If everything went fine, update the state
        if (p1 == 0) {
            GPSystem.setCardContentState(GPSystem.CARD_SECURED);
        }
    }

    void checkAuthentication() {
        if (!pin.isValidated())
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
    }

    /**
     * Resets the PIN's validated flag upon deselection of the applet.
     */
    public void deselect() {
        pin.reset();
    }
}