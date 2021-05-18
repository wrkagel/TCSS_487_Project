import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Scanner;

/**
 * Runs a command prompt that allows the user to perform various hashing and encrypting operations on a chosen file.
 */
public class Main2 {

    /**
     * Runs the program.
     * @param args Not used.
     */
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        byte[] inByte;
        int mode = -1;

        while(mode != 0) {
            mode = getMode(sc);
            if(mode > 0 && mode < 10) {
                inByte = getInput(sc);
                if(inByte != null) {
                    operation(mode, inByte, sc);
                }
            }
            System.out.println();
        }
        sc.close();
    }

    /**
     * Lets the user either choose a file or enter text input that will then be returned as a byte[]
     * @param sc Scanner used to accept users choice of input method
     * @return byte[] of either read in file or user input String
     */
    private static byte[] getInput(Scanner sc) {
        int choice;
        System.out.println("Please choose either file input or text input:\n1. File\n2. Text\nInput Method: ");
        String s = sc.nextLine();
        try {
            choice = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            choice = -1;
        }
        if (choice == 1) {
            return FileIO.getFile();
        } else if (choice == 2) {
            return FileIO.getText();
        } else {
            System.out.println(s + " is not a valid input choice. Returning to main menu.");
            return null;
        }
    }

    /**
     * Calls the appropriate function based on the mode chosen.
     * @param mode User chosen mode.
     * @param inByte byte[] of read in file.
     * @param sc Scanner used to get input from the player.
     */
    private static void operation(int mode, byte[] inByte, Scanner sc) {
        switch (mode) {
            case 1 -> computeHash(inByte);
            case 2 -> symmetricEncrypt(inByte, sc);
            case 3 -> symmetricDecrypt(inByte, sc);
            case 4 -> computeTag(inByte, sc);
            case 5 -> createKeyPair(sc);
            case 6 -> ellipticalEncrypt(sc, inByte);
            case 7 -> ellipticalDecrypt(inByte, sc);
            case 8 -> generateSignature(inByte, sc);
            case 9 -> validateSignature(inByte, sc);
            default -> System.out.println("Error with main.operation() switch statement.");
        }
    }

    private static void generateSignature(byte[] inByte, Scanner sc) {
        byte[] pw = getPassword(sc).getBytes(StandardCharsets.UTF_8);
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
        E521Curve u = E521Curve.g.scalarMultiply(k);
        byte [] test1 = KMACXOF256.compute(u.getX().toByteArray(), inByte,
                512, "T".getBytes(StandardCharsets.UTF_8));
        System.arraycopy(test1, 0, temp, 1, 64);
        BigInteger h = new BigInteger(temp);
        BigInteger z = k.subtract(h.multiply(s)).mod(E521Curve.r);
        byte[] out = new byte[132*2];
        System.arraycopy(h.toByteArray(), 0, out, 132 - h.toByteArray().length, h.toByteArray().length);
        System.arraycopy(z.toByteArray(), 0, out, out.length - z.toByteArray().length, z.toByteArray().length);
        FileIO.writeBytes(out);
    }

    private static void validateSignature(byte[] inByte, Scanner sc) {
        byte[] m = FileIO.getFile();
        E521Curve v = new E521Curve(new BigInteger("3528648883931951250008336645058857502635029786422946993282955126229324713094170609568248071925130573728678195522456187848315505452052918617037029777469449455"), new BigInteger("2207840549414269915595267378931222222247731507075726412957805245355061846868081925709084412409617354842198311851013884828550843736519843463251423300220746319"));
        byte[] hByte = new byte[132];
        System.arraycopy(inByte, 0, hByte, 0, 132);
        byte[] zByte = new byte[132];
        System.arraycopy(inByte, 132, zByte, 0, 132);
        BigInteger h = new BigInteger(hByte);
        BigInteger z = new BigInteger(zByte);
        E521Curve u = E521Curve.g.scalarMultiply(z).add(v.scalarMultiply(h));
        byte[] temp = KMACXOF256.compute(u.getX().toByteArray(), m, 512, "T".getBytes(StandardCharsets.UTF_8));
        byte[] hPrime = new byte[65];
        hPrime[0] = 0;
        System.arraycopy(temp, 0, hPrime, 1, 64);
        if ((new BigInteger(hPrime)).equals(h)) {
            System.out.println("validated");
        } else {
            System.out.println("Not validated");
        }

    }

    private static void ellipticalEncrypt(Scanner sc, byte[] inByte) {
        E521Curve v = new E521Curve(new BigInteger("3528648883931951250008336645058857502635029786422946993282955126229324713094170609568248071925130573728678195522456187848315505452052918617037029777469449455"), new BigInteger("2207840549414269915595267378931222222247731507075726412957805245355061846868081925709084412409617354842198311851013884828550843736519843463251423300220746319"));
        SecureRandom r = new SecureRandom();
        byte[] k = new byte[65];
        r.nextBytes(k);
        System.arraycopy(k, 0, k, 1, k.length - 1);
        k[0] = 0;
        BigInteger k4 = new BigInteger(k);
        k4 = k4.multiply(BigInteger.valueOf(4));
        E521Curve w = v.scalarMultiply(k4);
        E521Curve z = E521Curve.g.scalarMultiply(k4);
        System.out.println(w);
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
                out, inByte.length + 132, 64);
        byte[] temp = z.getX().toByteArray();
        System.arraycopy(temp, 0, out, 131 - temp.length, temp.length);
        out[131] = z.getY().mod(BigInteger.TWO).byteValueExact();

        FileIO.writeBytes(out);
    }

    private static void ellipticalDecrypt(byte[] inByte, Scanner sc) {
        boolean authTag = true;
        byte[] pw = getPassword(sc).getBytes(StandardCharsets.UTF_8);
        byte[] temp = new byte[65];
        System.arraycopy(KMACXOF256.compute(pw, new byte[0],
                512, "K".getBytes(StandardCharsets.UTF_8)), 0, temp, 1, 64);
        BigInteger s = new BigInteger(temp);
        s = s.multiply(BigInteger.valueOf(4));
        byte[] z = new byte[131];
        System.arraycopy(inByte, 0, z, 0, 131);
        E521Curve w = new E521Curve(new BigInteger(z), inByte[131] != 0);
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
        ka = KMACXOF256.compute(ka, m, 512, "SKA".getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < ka.length; i++) {
            if(ka[i] != inByte[inByte.length - 64 + i]) {
                authTag = false;
                break;
            }
        }
        if(authTag) {
            FileIO.writeBytes(m);
        } else {
            System.out.println("Failed to validate. No output will be written.");
        }
    }

    private static void createKeyPair(Scanner sc) {
        String pw = getPassword(sc);
        byte[] temp = new byte[65];
        System.arraycopy(KMACXOF256.compute(pw.getBytes(StandardCharsets.UTF_8), new byte[0],
                512, "K".getBytes(StandardCharsets.UTF_8)), 0, temp, 1, 64);
        BigInteger s = new BigInteger(temp);
        s = s.multiply(BigInteger.valueOf(4));
        E521Curve v = E521Curve.g.scalarMultiply(s);
        FileIO.writeString(v.toString());
    }

    /**
     * Encrypts a byte[] using a password chosen by the user. Writes the encrypted byte[], along with the
     * random bits and authentication tag, to a file of the user's choosing.
     * @param inByte byte[] to encrypt
     * @param sc scanner to get password from user
     */
    private static void symmetricEncrypt(byte[] inByte, Scanner sc) {
        String pw = getPassword(sc);
        byte[] pwByte = pw.getBytes(StandardCharsets.UTF_8);
        SecureRandom r = new SecureRandom();
        byte[] z = new byte[64];
        byte[] ke = new byte[64];
        byte[] ka = new byte[64];
        byte[] zpw = new byte[64 + pwByte.length];
        byte[] out = new byte[128 + inByte.length];
        r.nextBytes(z);
        System.arraycopy(z, 0, zpw, 0, z.length);
        System.arraycopy(z, 0, out, 0, z.length);
        System.arraycopy(pwByte, 0, zpw, z.length, pwByte.length);
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
        FileIO.writeBytes(out);
    }

    /**
     * Decrypts a byte[] using a password provided by the user. If the authentication tag matches after decryption
     * the result is written to a file of the user's choice, otherwise an error is printed and nothing is written out.
     * @param inByte byte[] to be decrypted, includes random bits and authentication tag
     * @param sc Scanner to get user password input
     */
    private static void symmetricDecrypt(byte[] inByte, Scanner sc) {
        boolean authTag = true;
        String pw = getPassword(sc);
        byte[] pwByte = pw.getBytes(StandardCharsets.UTF_8);
        byte[] ke = new byte[64];
        byte[] ka = new byte[64];
        byte[] zpw = new byte[64 + pwByte.length];
        byte[] data = new byte[inByte.length - 128];
        System.arraycopy(inByte, 64, data, 0, data.length);
        System.arraycopy(inByte, 0, zpw, 0, 64);
        System.arraycopy(pwByte, 0, zpw, 64, pwByte.length);
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
            FileIO.writeBytes(out);
        } else {
            System.out.println("Failed to validate. No output will be written.");
        }
    }

    /**
     * Computes an authentication tag based on the byte[] input and a password entered by the user.
     * @param inByte byte[] of data
     * @param sc Scanner used to get user input
     */
    private static void computeTag(byte[] inByte, Scanner sc) {
        String pw = getPassword(sc);
        //Values for the function taken from the assignment specifications.
        String s = "T";
        byte[] outByte = KMACXOF256.compute(pw.getBytes(StandardCharsets.UTF_8), inByte,
                512, s.getBytes(StandardCharsets.UTF_8));
        FileIO.writeBytes(outByte);
    }

    /**
     * Gets a password from the player.
     * @param sc Scanner to get input
     * @return String entered by player.
     */
    private static String getPassword(Scanner sc) {
        System.out.println("Please enter a password: ");
        return sc.nextLine();
    }

    /**
     * Computes a hash value based on the byte[] inByte
     * @param inByte input data to the hash function.
     */
    private static void computeHash(byte[] inByte) {
        //Values for the hash function taken from the assignment specifications.
        String s = "D";
        byte[] outByte = KMACXOF256.compute(new byte[0], inByte, 512, s.getBytes(StandardCharsets.UTF_8));
        FileIO.writeHex(outByte);
    }

    /**
     * Gets a selection of mode from the user. If the user fails to enter an integer it sets the mode
     * to an invalid number. Potential modes are taken from the TCSS_487 Project Description.
     * @param sc Scanner to get input from the user.
     * @return Entered integer or -1 if non-integer entered.
     */
    private static int getMode(Scanner sc) {
        int mode;
        String input;
        System.out.print("Please choose a mode by entering the corresponding number:\n1. Compute Hash of File\n" +
                "2. Symmetrically encrypt a file using KMACXOF256\n3. " +
                "Symmetrically decrypt a file using KMACXOF256\n4. Compute authentication tag\n" +
                "5. Create a key pair\n6. Asymmetrically encrypt a file\n" +
                "7. Asymmetrically decrypt a file\n8. Generate signature.\n9. Validate signature." +
                "0. Exit\nEnter mode: ");
        input = sc.nextLine();
        try {
            mode = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println(input + " is not a valid number. Please enter a valid number.");
            mode = -1;
        }
        return mode;
    }

}
