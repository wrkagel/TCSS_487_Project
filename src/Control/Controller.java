package Control;

import Model.IO;
import Model.KMACXOF256;
import View.GUI;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class Controller {

    private final GUI view;

    public Controller(GUI view) {
        this.view = view;
    }

    public void computeHash(byte[] inByte) {
        if(inByte == null) {
            inByte = IO.getFile(view, "Select file to hash.");
            if(inByte == null) return;
        }
        //Values for the hash function taken from the assignment specifications.
        String s = "D";
        byte[] outByte = KMACXOF256.compute(new byte[0], inByte, 512, s.getBytes(StandardCharsets.UTF_8));
        IO.writeHex(outByte, view, "Save resulting hash to a file.");
    }

    public void symmetricEncrypt(byte[] inByte) {
        if(inByte == null) {
            inByte = IO.getFile(view, "Select file to encrypt.");
            if(inByte == null) return;
        }
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
        IO.writeBytes(out, view, "Save encrypted file.");
    }

    public void symmetricDecrypt(byte[] inByte) {
        boolean authTag = true;
        if(inByte == null) {
            inByte = IO.getFile(view, "Select file to decrypt.");
            if(inByte == null) return;
        }
        byte[] pw = IO.getPassword(view, "Enter the password to use during decryption.");
        if (pw == null) {
            IO.showMessage(view, "No password entered. Canceling operation.");
            return;
        }
        byte[] ke = new byte[64];
        byte[] ka = new byte[64];
        byte[] zpw = new byte[64 + pw.length];
        byte[] data = new byte[inByte.length - 128];
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
        if(authTag) {
            IO.writeBytes(out, view, "Save decrypted file.");
        } else {
            IO.showMessage(view, "Failed to validate. No output will be written.");
        }
    }

    public void authentication(byte[] inByte) {
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
}
