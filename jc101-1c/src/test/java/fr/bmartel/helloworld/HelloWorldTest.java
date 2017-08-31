package fr.bmartel.helloworld;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class HelloWorldTest extends JavaCardTest {

    private final static byte[] hello = {(byte) 0x48, (byte) 0x65, (byte) 0x6c, (byte) 0x6c, (byte) 0x6f};

    @BeforeClass
    public static void setup() throws CardException {
        TestSuite.setup();
    }

    @Test
    public void helloTest() throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0x40, 0x00, 0x00);
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        assertArrayEquals(hello, response.getData());
    }
}