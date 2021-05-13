import java.nio.charset.StandardCharsets;

public class KMACXOF256 {

    /**
     * Used to mask individual bits of a byte.
     */
    private static final byte[] kmac = "KMAC".getBytes(StandardCharsets.UTF_8);

    /**
     * Returns a number 'a' left encoded in an unambiguous way to be used with Sha3 derived functions.
     * Specification for left encode taken from https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-185.pdf
     * @param a Number to be encoded
     * @param n Minimum n such that 2^{8n} > a
     * @return byte[] of encoded number
     */
    public static byte[] leftEncode(int a, int n) {
        byte[] left = new byte[n + 1];
        left[0] = (byte) n;
        for(int i = n; i > 0; i--) {
            left[i] = (byte) (a >>> ((n - i) * 8));
        }
        return left;
    }

    /**
     * Returns a number 'a' right encoded in an unambiguous way to be used with Sha3 derived functions.
     * Specification for left encode taken from https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-185.pdf
     * @param a Number to be encoded
     * @param n Minimum n such that 2^{8n} > a
     * @return byte[] of encoded number
     */
    public static byte[] rightEncode(int a, int n) {
        byte[] right = new byte[n + 1];
        right[n] = (byte) n;
        for(int i = n - 1; i > - 1; i--) {
            right[i] = (byte) (a >>> ((n - i - 1) * 8));
        }
        return right;
    }

    /**
     * Determines a minimum n value such that 2^{8n} > a.
     * @param a Must be greater than or equal to 0.
     * @return int n
     */
    public static int determineN(int a) {
        if (a < 0) throw new IllegalArgumentException("a = " + a + ". Violation of a > -1 for determineN.");
        int n = 1;
        while (Math.pow(2, 8 * n) <= a) {
            n++;
        }
        return n;
    }

    /**
     * Encodes a bit string as specified in https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-185.pdf.
     * The bit string here is represented as a byte[] as this implementation is enforcing the optional rule that
     * for a bit string S to be encoded, then len(S) % 8 = 0 must be true.
     * @param a Bit string to be encoded.
     * @return Encoded bit string as a byte[].
     */
    public static byte[] encodeString(byte [] a) {
        int n = determineN(a.length * 8);
        byte[] result = new byte[a.length + n + 1];
        byte[] left = leftEncode(a.length * 8, n);
        System.arraycopy(left, 0, result, 0, left.length);
        System.arraycopy(a, 0, result, left.length,
                a.length);
        return result;
    }

    /**
     * Pads a bitstring with zero bytes until length of (bitstring / 8) % w = 0.
     * Specifications taken from https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-185.pdf.
     * The bit string here is represented as a byte[] as this implementation is enforcing the optional rule that
     * for a bit string S it must have a multiple of 8 number of bits. Thus the bitstring / 8 part of the formula can
     * be ignored.
     * @param a byte[] representing the bitstring to be padded.
     * @param w length of padding.
     * @return padded bitstring.
     */
    public static byte[] bytepad(byte[] a, int w) {
        if (w < 1) throw new IllegalArgumentException("Invalid w for bytepad.");
        int n = determineN(w);
        byte[] left = leftEncode(w, n);
        int extra = (a.length + left.length) % w == 0 ? 0 : w - (a.length + left.length) % w;
        byte[] result = new byte[a.length + left.length + extra];
        System.arraycopy(left, 0, result, 0, left.length);
        System.arraycopy(a, 0, result, left.length, a.length);
        for (int i = left.length + a.length; i < result.length; i++) {
            result[i] = 0;
        }
        return result;
    }

    public static byte[] compute(byte[] k, byte[] x, int L, byte[] s) {
        byte[] temp = bytepad(encodeString(k), 136);
        byte[] newX = new byte[temp.length + x.length + 3];
        byte[] right = rightEncode(0, 1);
        System.arraycopy(temp, 0, newX, 0, temp.length);
        System.arraycopy(x, 0, newX, temp.length, x.length);
        newX[newX.length - 3] = right[0];
        newX[newX.length - 2] = right[1];
        newX[newX.length - 1] = 0x04;
        return cShake256(newX, L, kmac, s);
    }

    public static byte[] cShake256(byte[] x, int l, byte[] n, byte[] s) {
        n = encodeString(n);
        s = encodeString(s);
        byte[] ns = new byte[n.length + s.length];
        System.arraycopy(n, 0, ns, 0, n.length);
        System.arraycopy(s, 0, ns, n.length, s.length);
        byte[] padN = bytepad(ns, 136);
        byte[] input = new byte[padN.length + x.length];
        System.arraycopy(padN, 0, input, 0, padN.length);
        System.arraycopy(x, 0, input, padN.length, x.length);
        Sha3 keccak_512 = new Sha3(l / 8);
        keccak_512.sha3Update(input);
        byte[] md = new byte[l / 8];
        return keccak_512.sha3Final();
    }

    public static void printArray(byte[] input, String name) {
        System.out.print(name + ": ");
        for (int i = 0; i < input.length; i++) {
            System.out.print(String.format("%x, ", input[i]));
        }
        System.out.println();
    }
}
