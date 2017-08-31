package fr.bmartel.passwords;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class JavaCardTest {

    public ResponseAPDU transmitCommand(CommandAPDU data) throws CardException {
        if (System.getProperty("testMode") != null && System.getProperty("testMode").equals("smartcard")) {
            return TestSuite.getCard().getBasicChannel().transmit(data);
        } else {
            return TestSuite.getSimulator().transmitCommand(data);
        }
    }
}