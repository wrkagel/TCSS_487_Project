package Control;

import Model.E521CurvePoint;
import Model.IO;
import Model.KMACXOF256;
import View.GUI;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/*
    TCSS 487
    Project
    Walter Kagel
    5/19/2021
 */

/**
 * Contains all functions that will run after the user presses a button on the application.
 */
public class Controller {

    /**
     * Used so that the controller can pass the GUI on to the IO system.
     */
    private final GUI view;

    /**
     * Instantiates a controller.
     * @param view The GUI for the application.
     */
    public Controller(GUI view) {
        this.view = view;
    }

    /**
     * Computes a hash from a byte[] using KMACXOF256.
     * The hash is written out to a file of the user's choice as a string of hexadecimal characters that are
     * byte separated.
     * @param inByte contains a byte[] of user text input from the GUI or null if using file input.
     */
    public void computeHash(byte[] inByte) {
        //If null then use IO system to read in a file of the user's choice.
        if(inByte == null) {
            inByte = IO.getFile(view, "Select file to hash.");
            if(inByte == null) return;
        }
        //Values for the hash function taken from the assignment specifications.
        String s = "D";
        byte[] outByte = KMACXOF256.compute(new byte[0], inByte, 512, s.getBytes(StandardCharsets.UTF_8));
        IO.writeHex(outByte, view, "Save resulting hash to a file.");
    }

    /**
     * Symmetrically encrypt a byte[] using KMACXOF256.
     * The encrypted byte[] is written out to a file of the user's choice where the first 64 bytes are the random
     * bits z, the last 64 bytes are the tag t, and the remaining middle bytes are the encrypted byte[].
     * @param inByte contains a byte[] of user text input from the GUI or null if using file input.
     */
    public void symmetricEncrypt(byte[] inByte) {
        //If null then use IO system to read in a file of the user's choice.
        if(inByte == null) {
            inByte = IO.getFile(view, "Select file to encrypt.");
            if(inByte == null) return;
        }
        //Get the password from the user.
        byte[] pw = IO.getPassword(view, "Enter the password to use during encryption.");
        if (pw == null) {
            IO.showMessage(view, "No password entered. Canceling operation.");
            return;
        }
        SecureRandom r = new SecureRandom();
        byte[] z = new byte[64];
        byte[] ke = new byte[64];
        byte[] ka = new byte[64];
        byte[] zpw = new byte[64 + pw.length];
        byte[] out = new byte[128 + inByte.length];
        //Encryption algorithm
        r.nextBytes(z);
        System.arraycopy(z, 0, zpw, 0, z.length);
        System.arraycopy(z, 0, out, 0, z.length);
        System.arraycopy(pw, 0, zpw, z.length, pw.length);
        byte[] keka = KMACXOF256.compute(zpw, new byte[0], 1024, "S".getBytes(StandardCharsets.UTF_8));
        System.arraycopy(keka, 0, ke, 0, 64);
        System.arraycopy(keka, 64, ka, 0, 64);
        System.arraycopy(KMACXOF256.compute(ke, new byte[0], inByte.length * 8, "SKE".getBytes(StandardCharsets.UTF_8)),
                0, out, 64, inByte.length);
        for (int i = 0; i < inByte.length; i++) {
            out[i + 64] = (byte) (out[i + 64] ^ inByte[i]);
        }
        System.arraycopy(KMACXOF256.compute(ka, inByte, 512, "SKA".getBytes(StandardCharsets.UTF_8)), 0,
                out, inByte.length + 64, 64);
        //Write out result
        IO.writeBytes(out, view, "Save encrypted file.");
    }

    /**
     * Symmetrically decrypt a byte[] using KMACXOF256.
     * Decrypted byte[] will be saved to a file of the user's choice.
     * The byte[] to be decrypted is assumed to have the first 64 bytes be the random bits z, the last 64 bytes be
     * the tag t, and the middle bytes be the encrypted bytes.
     * @param inByte contains a byte[] of user text input from the GUI or null if using file input.
     */
    public void symmetricDecrypt(byte[] inByte) {
        //Will be used during authentication
        boolean authTag = true;
        //If null then use IO system to read in a file of the user's choice.
        if(inByte == null) {
            inByte = IO.getFile(view, "Select file to decrypt.");
            if(inByte == null) return;
        }
        //Get password from user.
        byte[] pw = IO.getPassword(view, "Enter the password to use during decryption.");
        if (pw == null) {
            IO.showMessage(view, "No password entered. Canceling operation.");
            return;
        }
        byte[] ke = new byte[64];
        byte[] ka = new byte[64];
        byte[] zpw = new byte[64 + pw.length];
        byte[] data = new byte[inByte.length - 128];
        //Decryption algorithm.
        System.arraycopy(inByte, 64, data, 0, data.length);
        System.arraycopy(inByte, 0, zpw, 0, 64);
        System.arraycopy(pw, 0, zpw, 64, pw.length);
        byte[] keka = KMACXOF256.compute(zpw, new byte[0], 1024, "S".getBytes(StandardCharsets.UTF_8));
        System.arraycopy(keka, 0, ke, 0, 64);
        System.arraycopy(keka, 64, ka, 0, 64);
        byte[] out = KMACXOF256.compute(ke, new byte[0], data.length * 8, "SKE".getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) (out[i] ^ data[i]);
        }
        ka = KMACXOF256.compute(ka, out, 512, "SKA".getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < ka.length; i++) {
            if(ka[i] != inByte[inByte.length - 64 + i]) {
                authTag = false;
                break;
            }
        }
        //If tags matched, then write out result, otherwise print an error message and do not write.
        if(authTag) {
            IO.writeBytes(out, view, "Save decrypted file.");
        } else {
            IO.showMessage(view, "Failed to validate. No output will be written.");
        }
    }

    /**
     * Creates an authentication tag based on a byte[] and user password using KMACXOF256.
     * @param inByte contains a byte[] of user text input from the GUI or null if using file input.
     */
    public void authentication(byte[] inByte) {
        //If null then use IO system to read in file of the user's choosing.
        if(inByte == null) {
            inByte = IO.getFile(view, "Select file to create tag from.");
            if(inByte == null) return;
        }
        byte[] pw = IO.getPassword(view, "Enter the password to use during decryption.");
        if (pw == null) {
            IO.showMessage(view, "No password entered. Canceling operation.");
            return;
        }
        //Values for the function taken from the assignment specifications.
        byte[] outByte = KMACXOF256.compute(pw, inByte,512, "T".getBytes(StandardCharsets.UTF_8));
        IO.writeBytes(outByte, view, "Save authentication tag.");
    }

    /**
     * Generate a public key from a user input password. Uses the E-521 curve and KMACXOF256.
     * Public key is saved to a file of the user's choosing with the first 66 bytes being the x coordinate and
     * the 67th byte specifying if the least significant bit of the y coordinate is 0 or 1.
     */
    public void keyPair() {
        //Get password from user.
        byte[] pw = IO.getPassword(view, "Enter the password to use during key generation.");
        if (pw == null) {
            IO.showMessage(view, "No password entered. Canceling operation.");
            return;
        }
        //Size 65 byte[] used to ensure that the 64 bit result is positive when converted to BigInteger.
        byte[] temp = new byte[65];
        System.arraycopy(KMACXOF256.compute(pw, new byte[0],
                512, "K".getBytes(StandardCharsets.UTF_8)), 0, temp, 1, 64);
        BigInteger s = new BigInteger(temp);
        s = s.multiply(BigInteger.valueOf(4));
        E521CurvePoint v = E521CurvePoint.g.scalarMultiply(s);
        byte[] out = new byte[67];
        byte[] x = v.getX().toByteArray();
        System.arraycopy(x, 0, out, 66 - x.length, x.length);
        out[66] = (byte) (v.getY().testBit(0) ? 1 : 0);
        IO.writeBytes(out, view, "Save public key.");
    }

    /**
     * Encrypts a file using E-521 curve and KMACXOF256. Saves the encrypted byte[] to a file of the user's choosing.
     * The output file contains the information for the random curve point generated from the password in the first
     * 67 bytes, the tag in the last 64 bytes, and the encrypted byte[] in the middle bytes.
     * @param inByte contains a byte[] of user text input from the GUI or null if using file input.
     */
    public void asEncrypt(byte[] inByte) {
        //If null then use IO system to read in file of the user's choosing.
        if(inByte == null) {
            inByte = IO.getFile(view, "Select file to encrypt.");
            if(inByte == null) return;
        }
        //Get public key file of user's choosing.
        byte[] pubByte = IO.getFile(view, "Select public key to use during encryption.");
        if(pubByte == null) return;
        byte[] x = new byte[66];
        System.arraycopy(pubByte, 0, x, 0, 66);
        E521CurvePoint v = new E521CurvePoint(new BigInteger(x), pubByte[66] == (byte) 1);
        SecureRandom r = new SecureRandom();
        byte[] k = new byte[65];
        r.nextBytes(k);
        k[0] = 0;
        BigInteger k4 = new BigInteger(k);
        k4 = k4.multiply(BigInteger.valueOf(4));
        E521CurvePoint w = v.scalarMultiply(k4);
        E521CurvePoint z = E521CurvePoint.g.scalarMultiply(k4);
        byte[] ke = new byte[64];
        byte[] ka = new byte[64];
        byte[] keka = KMACXOF256.compute(w.getX().toByteArray(), new byte[0], 1024, "P".getBytes(StandardCharsets.UTF_8));
        System.arraycopy(keka, 0, ke, 0, 64);
        System.arraycopy(keka, 64, ka, 0, 64);
        byte[] out = new byte[67 + inByte.length + 64];
        System.arraycopy(KMACXOF256.compute(ke, new byte[0], inByte.length * 8, "PKE".getBytes(StandardCharsets.UTF_8)),
                0, out, 67, inByte.length);
        for (int i = 0; i < inByte.length; i++) {
            out[i + 67] = (byte) (out[i + 67] ^ inByte[i]);
        }
        System.arraycopy(KMACXOF256.compute(ka, inByte, 512, "PKA".getBytes(StandardCharsets.UTF_8)), 0,
                out, out.length - 64, 64);
        byte[] temp = z.getX().toByteArray();
        System.arraycopy(temp, 0, out, 66 - temp.length, temp.length);
        out[66] = (byte) (z.getY().testBit(0) ? 1 : 0);
        IO.writeBytes(out, view, "Save encrypted file.");
    }

    /**
     * Decrypts a byte[] using the E-521 curve and KMACXOF256. Saves the decrypted byte[] to a file of the user's
     * choosing.
     * Encrypted byte[] assumed to be of the form where the first 67 bytes describe a random curve point generated
     * using the user's password, the last 64 bytes are the tag, and the middle bytes are the bytes to decrypt.
     * @param inByte contains a byte[] of user text input from the GUI or null if using file input.
     */
    public void asDecrypt(byte[] inByte) {
        boolean authTag = true;
        //If null then use IO system to read in file of the user's choosing.
        if(inByte == null) {
            inByte = IO.getFile(view, "Select file to encrypt.");
            if(inByte == null) return;
        }
        byte[] pw = IO.getPassword(view, "Enter password for decryption.");
        byte[] temp = new byte[65];
        System.arraycopy(KMACXOF256.compute(pw, new byte[0],
                512, "K".getBytes(StandardCharsets.UTF_8)), 0, temp, 1, 64);
        BigInteger s = new BigInteger(temp);
        s = s.multiply(BigInteger.valueOf(4));
        byte[] z = new byte[66];
        System.arraycopy(inByte, 0, z, 0, 66);
        E521CurvePoint w = new E521CurvePoint(new BigInteger(z), inByte[66] == 1);
        w = w.scalarMultiply(s);
        byte[] ke = new byte[64];
        byte[] ka = new byte[64];
        byte[] keka = KMACXOF256.compute(w.getX().toByteArray(), new byte[0], 1024, "P".getBytes(StandardCharsets.UTF_8));
        System.arraycopy(keka, 0, ke, 0, 64);
        System.arraycopy(keka, 64, ka, 0, 64);
        byte[] m = new byte[inByte.length - 67 - 64];
        m = KMACXOF256.compute(ke, new byte[0], m.length * 8, "PKE".getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < m.length; i++) {
            m[i] = (byte) (m[i] ^ inByte[i + 67]);
        }
        ka = KMACXOF256.compute(ka, m, 512, "PKA".getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < ka.length; i++) {
            if(ka[i] != inByte[inByte.length - 64 + i]) {
                authTag = false;
                break;
            }
        }
        //If the above authentication worked, then save output to file. Otherwise, show an error message.
        if(authTag) {
            IO.writeBytes(m, view, "Save decrypted file.");
        } else {
            IO.showMessage(view, "Failed to validate. No output will be written.");
        }
    }

    /**
     * Create a signature based on a file and password of the user's choosing. Saves signature to a file of the
     * user's choosing. Uses the E-521 curve and KMACXOF256.
     */
    public void createSig() {
        byte[] inByte = IO.getFile(view, "Select file to sign.");
        if(inByte == null) return;
        byte[] pw = IO.getPassword(view, "Enter the password to be used during signature creation.");
        byte[] temp = new byte[65];
        temp[0] = 0;
        System.arraycopy(KMACXOF256.compute(pw, new byte[0],
                512, "K".getBytes(StandardCharsets.UTF_8)), 0, temp, 1, 64);
        BigInteger s = new BigInteger(temp);
        s = s.multiply(BigInteger.valueOf(4));
        System.arraycopy(KMACXOF256.compute(s.toByteArray(), inByte,
                512, "N".getBytes(StandardCharsets.UTF_8)), 0, temp, 1, 64);
        BigInteger k = new BigInteger(temp);
        k = k.multiply(BigInteger.valueOf(4));
        E521CurvePoint u = E521CurvePoint.g.scalarMultiply(k);
        byte [] test1 = KMACXOF256.compute(u.getX().toByteArray(), inByte,
                512, "T".getBytes(StandardCharsets.UTF_8));
        System.arraycopy(test1, 0, temp, 1, 64);
        BigInteger h = new BigInteger(temp);
        BigInteger z = k.subtract(h.multiply(s)).mod(E521CurvePoint.r);
        byte[] out = new byte[67*2];
        System.arraycopy(h.toByteArray(), 0, out, 67 - h.toByteArray().length, h.toByteArray().length);
        System.arraycopy(z.toByteArray(), 0, out, out.length - z.toByteArray().length, z.toByteArray().length);
        IO.writeBytes(out, view, "Save signature to file.");
    }

    /**
     * Verify a signature based on a user chosen signature file, the file associated with that signature, and the
     * public key associated with the person who signed the file. If any of the preceding associations is incorrect
     * or the bytes in the files have been changed then the validation will fail.
     */
    public void verifySig() {
        byte[] sig = IO.getFile(view, "Select signature file.");
        if(sig == null) return;
        byte[] m = IO.getFile(view, "Select file associated with signature.");
        if (m == null) return;
        byte[] pubByte = IO.getFile(view, "Select public key.");
        if(pubByte == null) return;
        byte[] x = new byte[66];
        System.arraycopy(pubByte, 0, x, 0, 66);
        E521CurvePoint v = new E521CurvePoint(new BigInteger(x), pubByte[66] == (byte) 1);
        byte[] hByte = new byte[67];
        System.arraycopy(sig, 0, hByte, 0, 67);
        byte[] zByte = new byte[67];
        System.arraycopy(sig, 67, zByte, 0, 67);
        BigInteger h = new BigInteger(hByte);
        BigInteger z = new BigInteger(zByte);
        E521CurvePoint u = E521CurvePoint.g.scalarMultiply(z).add(v.scalarMultiply(h));
        byte[] temp = KMACXOF256.compute(u.getX().toByteArray(), m, 512, "T".getBytes(StandardCharsets.UTF_8));
        byte[] hPrime = new byte[65];
        hPrime[0] = 0;
        System.arraycopy(temp, 0, hPrime, 1, 64);
        if ((new BigInteger(hPrime)).equals(h)) {
            IO.showMessage(view, "Signature validated.");
        } else {
            IO.showMessage(view, "Signature not validated.");
        }
    }
}
