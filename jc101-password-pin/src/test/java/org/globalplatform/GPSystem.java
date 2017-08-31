package org.globalplatform;

public class GPSystem {

    public static final byte APPLICATION_INSTALLED = 3;
    public static final byte APPLICATION_SELECTABLE = 7;
    public static final byte SECURITY_DOMAIN_PERSONALIZED = 15;
    public static final byte CARD_OP_READY = 1;
    public static final byte CARD_INITIALIZED = 7;
    public static final byte CARD_SECURED = 15;
    public static final byte CARD_LOCKED = 127;
    public static final byte CARD_TERMINATED = -1;
    public static final byte CVM_GLOBAL_PIN = 17;

    private static byte state = APPLICATION_SELECTABLE;

    public GPSystem() {
    }

    public static byte getCardContentState() {
        return state;
    }

    public static byte getCardState() {
        return 0;
    }

    public static CVM getCVM(byte var0) {
        return null;
    }

    public static SecureChannel getSecureChannel() {
        return null;
    }

    public static boolean lockCard() {
        return false;
    }

    public static boolean terminateCard() {
        return false;
    }

    public static boolean setATRHistBytes(byte[] var0, short var1, byte var2) {
        return false;
    }

    public static boolean setCardContentState(byte state) {
        GPSystem.state = state;
        return true;
    }
}
