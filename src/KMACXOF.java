public class KMACXOF {

    /**
     * Used to mask individual bits of a byte.
     */
    private static final byte[] mask = new byte[] {1, 2, 4, 8, 16, 32, 64, -128};

    /**
     * Returns the number a encoded in an unambiguous way to be used with Sha3 derived functions.
     * Specification for left encode taken from https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-185.pdf
     * @param a Number to be encoded
     * @param n such that 2^{8n} > a
     * @return byte[] of encoded number
     */
    public static final byte[] leftEncode(int a, int n) {
        byte[] left = new byte[n + 1];
        left[0] = enc8((byte) n);
        for(int i = n; i > 0; i--) {
            left[i] = enc8((byte) (a >>> ((n - i) * 8)));
        }
        return left;
    }

    public static final byte enc8(byte b) {
        if (b > 255) throw new IllegalArgumentException("enc8 argument too large. " + b);
        byte result = 0;
        for (int i = 0; i < 8; i++) {
            result |= ((((b & mask[i]) << (7 - i)) & 0xFF) >>> i);
        }
        return result;
    }

    public static final int determineN(int a) {
        if (a < 0) throw new IllegalArgumentException("a = " + a + ". Violation of a > -1 for determineN.");
        int n = 1;
        while (Math.pow(2, 8 * n) <= a) {
            n++;
        }
        return n;
    }

    public static final byte[] encodeString(byte [] a) {
        int n = determineN(a.length);
        byte[] temp = new byte[a.length + n + 1];
        byte[] left = leftEncode(a.length, n);
        for(int i = 0; i < left.length; i++) {
            temp[i] = left[i];
        }
        for (int i = left.length; i < left.length + a.length; i++) {
            temp[i] = a[i - left.length];
        }
        return temp;
    }

    public static final byte[] bytepad(byte[] a, int w) {
        if (w < 1) throw new IllegalArgumentException("Invalid w for bytepad.");

        return a;
    }
}
