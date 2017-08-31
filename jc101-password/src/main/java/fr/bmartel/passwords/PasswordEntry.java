package fr.bmartel.passwords;

import javacard.framework.JCSystem;
import javacard.framework.Util;

public class PasswordEntry {

    public static short SIZE_ID = 16;
    public static short SIZE_USERNAME = 24;
    public static short SIZE_PASSWORD = 16;

    private PasswordEntry next;
    private static PasswordEntry first;
    private static PasswordEntry deleted;

    private byte[] id;
    private byte[] username;
    private byte[] password;

    private byte idLength;
    private byte userNameLength;
    private byte passwordLength;

    private PasswordEntry() {
        // Allocates all fields
        id = new byte[SIZE_ID];
        username = new byte[SIZE_USERNAME];
        password = new byte[SIZE_PASSWORD];
        // The new element is inserted in front of the list
        next = first;
        first = this;
    }

    static PasswordEntry getInstance() {
        if (deleted == null) {
            // There is no element to recycle
            return new PasswordEntry();
        } else {
            // Recycle the first available element
            PasswordEntry instance = deleted;
            deleted = instance.next;
            instance.next = first;
            first = instance;
            return instance;
        }
    }

    static PasswordEntry search(byte[] buf, short ofs, byte len) {
        for (PasswordEntry pe = first; pe != null; pe = pe.next) {
            if (pe.idLength != len) continue;
            if (Util.arrayCompare(pe.id, (short) 0, buf, ofs, len) == 0)
                return pe;
        }
        return null;
    }

    public static PasswordEntry getFirst() {
        return first;
    }

    private void remove() {
        if (first == this) {
            first = next;
        } else {
            for (PasswordEntry pe = first; pe != null; pe = pe.next)
                if (pe.next == this)
                    pe.next = next;
        }
    }

    private void recycle() {
        next = deleted;
        idLength = 0;
        userNameLength = 0;
        passwordLength = 0;
        deleted = this;
    }

    static void delete(byte[] buf, short ofs, byte len) {
        PasswordEntry pe = search(buf, ofs, len);
        if (pe != null) {
            JCSystem.beginTransaction();
            pe.remove();
            pe.recycle();
            JCSystem.commitTransaction();
        }
    }

    byte getId(byte[] buf, short ofs) {
        Util.arrayCopy(id, (short) 0, buf, ofs, idLength);
        return idLength;
    }

    byte getUserName(byte[] buf, short ofs) {
        Util.arrayCopy(username, (short) 0, buf, ofs, userNameLength);
        return userNameLength;
    }

    byte getPassword(byte[] buf, short ofs) {
        Util.arrayCopy(password, (short) 0, buf, ofs, passwordLength);
        return passwordLength;
    }

    public byte getIdLength() {
        return idLength;
    }

    public PasswordEntry getNext() {
        return next;
    }

    public void setId(byte[] buf, short ofs, byte len) {
        Util.arrayCopy(buf, ofs, id, (short) 0, len);
        idLength = len;
    }

    public void setUserName(byte[] buf, short ofs, byte len) {
        Util.arrayCopy(buf, ofs, username, (short) 0, len);
        userNameLength = len;
    }

    public void setPassword(byte[] buf, short ofs, byte len) {
        Util.arrayCopy(buf, ofs, password, (short) 0, len);
        passwordLength = len;
    }
}