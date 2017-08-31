package fr.bmartel.passwords;

import fr.bmartel.helloworld.util.TestUtils;

public class Password {

    private byte[] id;
    private byte[] username;
    private byte[] password;

    public Password(byte[] id, byte[] username, byte[] password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public byte[] getFullApdu() {
        return TestUtils.concatByteArray(id, username, password);
    }

    public byte[] getId() {
        return id;
    }

    public byte[] getData() {
        return TestUtils.concatByteArray(username, password);
    }
}
