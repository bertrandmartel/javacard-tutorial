package fr.bmartel.passwords;

import fr.bmartel.helloworld.util.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class PasswordEntryTest {

    private PasswordEntry entry;

    private final static byte[] ID_BASIC = new byte[]{0x01, 0x02};
    private final static byte[] USERNAME_BASIC = new byte[]{0x03, 0x04, 0x05};
    private final static byte[] PASSWORD_BASIC = new byte[]{0x05, 0x06, 0x07, 0x08};

    private final static byte[] ID_BASIC1 = new byte[]{0x11, 0x12};
    private final static byte[] USERNAME_BASIC1 = new byte[]{0x13, 0x14, 0x15};
    private final static byte[] PASSWORD_BASIC1 = new byte[]{0x15, 0x16, 0x17, 0x18};

    private final static byte[] ID_BASIC2 = new byte[]{0x21, 0x22};
    private final static byte[] USERNAME_BASIC2 = new byte[]{0x23, 0x24, 0x25};
    private final static byte[] PASSWORD_BASIC2 = new byte[]{0x25, 0x26, 0x27, 0x28};

    private final static byte[] ID_BASIC3 = new byte[]{0x31, 0x32};
    private final static byte[] USERNAME_BASIC3 = new byte[]{0x33, 0x34, 0x35};
    private final static byte[] PASSWORD_BASIC3 = new byte[]{0x35, 0x36, 0x37, 0x38};

    private final static byte[] ID_BASIC4 = new byte[]{0x41, 0x42};

    /**
     * Get static field "first" by reflection
     *
     * @return first PasswordEntry
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private PasswordEntry getFirst() throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(PasswordEntry.class, "first");
        if (f == null)
            throw new NoSuchFieldException();
        return (PasswordEntry) f.get(null);
    }

    /**
     * Get static field "deleted" by reflection
     *
     * @return item to recycle
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private PasswordEntry getDeleted() throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(PasswordEntry.class, "deleted");
        if (f == null)
            throw new NoSuchFieldException();
        return (PasswordEntry) f.get(null);
    }

    /**
     * Get the next password entry for a specific instance
     *
     * @param entry password entry instance
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private PasswordEntry getNext(PasswordEntry entry) throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(entry.getClass(), "next");
        if (f == null)
            throw new NoSuchFieldException();
        return (PasswordEntry) f.get(entry);
    }

    /**
     * Get id length property by reflection.
     *
     * @param entry Password Entry instance
     * @return id length
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private byte getIdLength(PasswordEntry entry) throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(entry.getClass(), "idLength");
        if (f == null)
            throw new NoSuchFieldException();
        return f.getByte(entry);
    }

    /**
     * Get username length property by reflection.
     *
     * @param entry Password entry instance
     * @return username length
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private byte getUsernameLength(PasswordEntry entry) throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(entry.getClass(), "userNameLength");
        if (f == null)
            throw new NoSuchFieldException();
        return f.getByte(entry);
    }

    /**
     * Get password length property by reflection.
     *
     * @param entry Password entry instance
     * @return password length
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private byte getPasswordLength(PasswordEntry entry) throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(entry.getClass(), "passwordLength");
        if (f == null)
            throw new NoSuchFieldException();
        return f.getByte(entry);
    }

    /**
     * Get byte array property
     *
     * @param object Object instance
     * @param name   field name
     * @return byte array object matching the specified field name
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    private byte[] getByteArray(Object object, String name) throws IllegalAccessException, NoSuchFieldException {
        Field f = TestUtils.getField(object.getClass(), name);
        if (f == null)
            throw new NoSuchFieldException();
        return (byte[]) f.get(object);
    }

    /**
     * Check size of byte array properties
     *
     * @param entry Password entry instance
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    private void checkDataSize(PasswordEntry entry) throws IllegalAccessException, NoSuchFieldException {
        assertNotNull("id array not null", getByteArray(entry, "id"));
        assertEquals("id array size check", getByteArray(entry, "id").length, PasswordEntry.SIZE_ID);
        assertNotNull("username array not null", getByteArray(entry, "username"));
        assertEquals("username array size check", getByteArray(entry, "username").length, PasswordEntry.SIZE_USERNAME);
        assertNotNull("password array not null", getByteArray(entry, "password"));
        assertEquals("password array size check", getByteArray(entry, "password").length, PasswordEntry.SIZE_PASSWORD);
    }

    private void addItem(byte[] id, byte[] username, byte[] password) throws NoSuchFieldException, IllegalAccessException {
        entry = PasswordEntry.getInstance();

        assertNotNull("created instance exist", entry);

        PasswordEntry firstElement = getFirst();

        assertNotNull("first element exists", firstElement);
        assertEquals("first element is the created element", firstElement, entry);

        assertNull("no item to recycle", getDeleted());

        assertEquals("next getter valid", getNext(entry), entry.getNext());
        assertEquals("first getter valid", getFirst(), PasswordEntry.getFirst());

        assertEquals("check id length getter", 0, entry.getIdLength());

        assertEquals("check empty password length", 0, entry.getPassword(new byte[PasswordEntry.SIZE_PASSWORD], (short) 0));
        assertEquals("check empty username length", 0, entry.getUserName(new byte[PasswordEntry.SIZE_USERNAME], (short) 0));
        assertEquals("check empty id length", 0, entry.getId(new byte[PasswordEntry.SIZE_ID], (short) 0));

        assertEquals("check password length via reflection", 0, getPasswordLength(entry));
        assertEquals("check username length via reflection", 0, getUsernameLength(entry));
        assertEquals("check id length via reflection", 0, getIdLength(entry));

        entry.setId(id, (short) 0, (byte) id.length);
        entry.setUserName(username, (short) 0, (byte) username.length);
        entry.setPassword(password, (short) 0, (byte) password.length);

        checkDataSize(entry);
    }

    private void deleteItem(byte[] id) {
        PasswordEntry.delete(id, (short) 0, (byte) id.length);
    }

    private void checkData(PasswordEntry entry, byte[] expectedId, byte[] expectedUsername, byte[] expectedPassword) {
        byte[] passwordOut = new byte[expectedPassword.length];
        byte[] usernameOut = new byte[expectedUsername.length];
        byte[] idOut = new byte[expectedId.length];

        assertEquals("check password length", expectedPassword.length, entry.getPassword(passwordOut, (short) 0));
        assertEquals("check username length", expectedUsername.length, entry.getUserName(usernameOut, (short) 0));
        assertEquals("check id length", expectedId.length, entry.getId(idOut, (short) 0));

        assertArrayEquals("check password data", expectedPassword, passwordOut);
        assertArrayEquals("check username data", expectedUsername, usernameOut);
        assertArrayEquals("check id data", expectedId, idOut);
    }

    private int getLength() {
        int length = 0;
        PasswordEntry current = PasswordEntry.getFirst();
        while (current != null) {
            length++;
            current = current.getNext();
        }
        return length;
    }

    @Before
    public void initTest() throws NoSuchFieldException, IllegalAccessException {
        Field f = TestUtils.getField(PasswordEntry.class, "first");
        if (f == null)
            throw new NoSuchFieldException();
        f.set(null, null);
        f = TestUtils.getField(PasswordEntry.class, "deleted");
        if (f == null)
            throw new NoSuchFieldException();
        f.set(null, null);
        assertNull("no first element", getFirst());
        assertNull("no item to recycle", getDeleted());
    }

    @Test
    public void setPropertiesTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(ID_BASIC, USERNAME_BASIC, PASSWORD_BASIC);
        assertNull("no next element", entry.getNext());

        entry.setId(ID_BASIC, (short) 0, (byte) ID_BASIC.length);
        entry.setUserName(USERNAME_BASIC, (short) 0, (byte) USERNAME_BASIC.length);
        entry.setPassword(PASSWORD_BASIC, (short) 0, (byte) PASSWORD_BASIC.length);

        assertEquals("check id length getter", ID_BASIC.length, entry.getIdLength());

        checkData(entry, ID_BASIC, USERNAME_BASIC, PASSWORD_BASIC);
    }

    @Test
    public void setPropertiesOffsetTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(ID_BASIC, USERNAME_BASIC, PASSWORD_BASIC);
        assertNull("no next element", entry.getNext());

        short offset = 2;

        entry.setId(TestUtils.addOffset(offset, ID_BASIC), offset, (byte) ID_BASIC.length);
        entry.setUserName(TestUtils.addOffset(offset, USERNAME_BASIC), offset, (byte) USERNAME_BASIC.length);
        entry.setPassword(TestUtils.addOffset(offset, PASSWORD_BASIC), offset, (byte) PASSWORD_BASIC.length);

        assertEquals("check id length getter", ID_BASIC.length, entry.getIdLength());

        checkData(entry, ID_BASIC, USERNAME_BASIC, PASSWORD_BASIC);
    }

    @Test
    public void addMultipleEntries() throws NoSuchFieldException, IllegalAccessException {
        assertEquals(0, getLength());
        addItem(ID_BASIC, USERNAME_BASIC, PASSWORD_BASIC);
        assertEquals(1, getLength());
        addItem(ID_BASIC, USERNAME_BASIC, PASSWORD_BASIC);
        assertEquals(2, getLength());
        addItem(ID_BASIC, USERNAME_BASIC, PASSWORD_BASIC);
        assertEquals(3, getLength());
    }

    private PasswordEntry checkSearchedItem(byte[] id, byte[] username, byte[] password) {
        PasswordEntry searchEntry = PasswordEntry.search(id, (short) 0, (byte) id.length);
        assertNotNull("search result not null", searchEntry);
        checkData(searchEntry, id, username, password);
        return searchEntry;
    }

    @Test
    public void searchIdValues() throws NoSuchFieldException, IllegalAccessException {
        addItem(ID_BASIC, USERNAME_BASIC, PASSWORD_BASIC);
        addItem(ID_BASIC1, USERNAME_BASIC1, PASSWORD_BASIC1);
        addItem(ID_BASIC2, USERNAME_BASIC2, PASSWORD_BASIC2);
        addItem(ID_BASIC3, USERNAME_BASIC3, PASSWORD_BASIC3);

        assertEquals("init length", 4, getLength());

        assertNull("search value check next null (eg first added item)", checkSearchedItem(ID_BASIC, USERNAME_BASIC, PASSWORD_BASIC).getNext());
        assertNotNull("search value next not null", checkSearchedItem(ID_BASIC1, USERNAME_BASIC1, PASSWORD_BASIC1).getNext());
        assertNotNull("search value next not null", checkSearchedItem(ID_BASIC2, USERNAME_BASIC2, PASSWORD_BASIC2).getNext());
        assertNotNull("search value next not null", checkSearchedItem(ID_BASIC3, USERNAME_BASIC3, PASSWORD_BASIC3).getNext());

        PasswordEntry searchEntry = PasswordEntry.search(ID_BASIC4, (short) 0, (byte) ID_BASIC4.length);
        assertNull("search value invalid", searchEntry);
    }

    @Test
    public void deleteTest() throws NoSuchFieldException, IllegalAccessException {
        assertEquals("init length", 0, getLength());
        addItem(ID_BASIC, USERNAME_BASIC, PASSWORD_BASIC);
        assertEquals("length after addition", 1, getLength());
        deleteItem(ID_BASIC);
        assertEquals("length after deletion", 0, getLength());
    }

    @Test
    public void deleteMultipleTest() throws NoSuchFieldException, IllegalAccessException {
        addItem(ID_BASIC, USERNAME_BASIC, PASSWORD_BASIC);
        addItem(ID_BASIC1, USERNAME_BASIC1, PASSWORD_BASIC1);
        addItem(ID_BASIC2, USERNAME_BASIC2, PASSWORD_BASIC2);
        addItem(ID_BASIC3, USERNAME_BASIC3, PASSWORD_BASIC3);
        assertEquals("init length", 4, getLength());
        deleteItem(ID_BASIC);
        deleteItem(ID_BASIC1);
        deleteItem(ID_BASIC2);
        deleteItem(ID_BASIC3);
        assertEquals("length after deletion", 0, getLength());
    }

    @Test
    public void checkRecycledItem() throws NoSuchFieldException, IllegalAccessException {
        assertNull("no item to recycle", getDeleted());
        addItem(ID_BASIC, USERNAME_BASIC, PASSWORD_BASIC);
        deleteItem(ID_BASIC);
        assertNotNull("1 item to recycle", getDeleted());
    }

    @Test
    public void interTwinedAddDelete() throws NoSuchFieldException, IllegalAccessException {
        for (int i = 0; i < 10; i++) {
            assertEquals("init length, iteration n°" + i, 0, getLength());
            addItem(ID_BASIC, USERNAME_BASIC, PASSWORD_BASIC);
            assertEquals("length after addition, iteration n°" + i, 1, getLength());
            deleteItem(ID_BASIC);
            assertEquals("length after deletion, iteration n°" + i, 0, getLength());
        }
    }
}