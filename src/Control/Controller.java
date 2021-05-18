package Control;

import Model.E521CurvePoint;
import Model.IO;
import Model.KMACXOF256;
import View.GUI;

import java.math.BigInteger;
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

    public void keyPair() {
        byte[] pw = IO.getPassword(view, "Enter the password to use during key generation.");
        if (pw == null) {
            IO.showMessage(view, "No password entered. Canceling operation.");
            return;
        }
        byte[] temp = new byte[65];
        System.arraycopy(KMACXOF256.compute(pw, new byte[0],
                512, "K".getBytes(StandardCharsets.UTF_8)), 0, temp, 1, 64);
        BigInteger s = new BigInteger(temp);
        s = s.multiply(BigInteger.valueOf(4));
        E521CurvePoint v = E521CurvePoint.g.scalarMultiply(s);
        byte[] out = new byte[132];
        byte[] x = v.getX().toByteArray();
        System.arraycopy(x, 0, out, 131 - x.length, x.length);
        out[131] = (byte) (v.getY().testBit(0) ? 1 : 0);
        IO.writeBytes(out, view, "Save public key.");
    }

    public void asEncrypt(byte[] inByte) {
        if(inByte == null) {
            inByte = IO.getFile(view, "Select file to encrypt.");
            if(inByte == null) return;
        }
        byte[] pubByte = IO.getFile(view, "Select public key to use during encryption.");
        if(pubByte == null) return;
        byte[] x = new byte[131];
        System.arraycopy(pubByte, 0, x, 0, 131);
        E521CurvePoint v = new E521CurvePoint(new BigInteger(x), pubByte[131] == (byte) 1);
        SecureRandom r = new SecureRandom();
        byte[] k = new byte[65];
        r.nextBytes(k);
        System.arraycopy(k, 0, k, 1, k.length - 1);
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
        byte[] out = new byte[132 + inByte.length + 64];
        System.arraycopy(KMACXOF256.compute(ke, new byte[0], inByte.length * 8, "PKE".getBytes(StandardCharsets.UTF_8)),
                0, out, 132, inByte.length);
        for (int i = 0; i < inByte.length; i++) {
            out[i + 132] = (byte) (out[i + 132] ^ inByte[i]);
        }
        System.arraycopy(KMACXOF256.compute(ka, inByte, 512, "PKA".getBytes(StandardCharsets.UTF_8)), 0,
                out, out.length - 64, 64);
        byte[] temp = z.getX().toByteArray();
        System.arraycopy(temp, 0, out, 131 - temp.length, temp.length);
        out[131] = (byte) (z.getY().testBit(0) ? 1 : 0);
        IO.writeBytes(out, view, "Save encrypted file.");
    }

    public void asDecrypt(byte[] inByte) {
        boolean authTag = true;
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
        byte[] z = new byte[131];
        System.arraycopy(inByte, 0, z, 0, 131);
        E521CurvePoint w = new E521CurvePoint(new BigInteger(z), inByte[131] == 1);
        w = w.scalarMultiply(s);
        byte[] ke = new byte[64];
        byte[] ka = new byte[64];
        byte[] keka = KMACXOF256.compute(w.getX().toByteArray(), new byte[0], 1024, "P".getBytes(StandardCharsets.UTF_8));
        System.arraycopy(keka, 0, ke, 0, 64);
        System.arraycopy(keka, 64, ka, 0, 64);
        byte[] m = new byte[inByte.length - 132 - 64];
        m = KMACXOF256.compute(ke, new byte[0], m.length * 8, "PKE".getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < m.length; i++) {
            m[i] = (byte) (m[i] ^ inByte[i + 132]);
        }
        ka = KMACXOF256.compute(ka, m, 512, "PKA".getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < ka.length; i++) {
            if(ka[i] != inByte[inByte.length - 64 + i]) {
                authTag = false;
                break;
            }
        }
        if(authTag) {
            IO.writeBytes(m, view, "Save decrypted file.");
        } else {
            IO.showMessage(view, "Failed to validate. No output will be written.");
        }
    }

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
        byte[] out = new byte[132*2];
        System.arraycopy(h.toByteArray(), 0, out, 132 - h.toByteArray().length, h.toByteArray().length);
        System.arraycopy(z.toByteArray(), 0, out, out.length - z.toByteArray().length, z.toByteArray().length);
        IO.writeBytes(out, view, "Save signature to file.");
    }

    public void verifySig() {
        byte[] sig = IO.getFile(view, "Select signature file.");
        byte[] m = IO.getFile(view, "Select file associated with signature.");
        byte[] pubByte = IO.getFile(view, "Select public key.");
        if(pubByte == null) return;
        byte[] x = new byte[131];
        System.arraycopy(pubByte, 0, x, 0, 131);
        E521CurvePoint v = new E521CurvePoint(new BigInteger(x), pubByte[131] == (byte) 1);
        byte[] hByte = new byte[132];
        System.arraycopy(sig, 0, hByte, 0, 132);
        byte[] zByte = new byte[132];
        System.arraycopy(sig, 132, zByte, 0, 132);
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
