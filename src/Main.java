import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Scanner;

/**
 * Runs a command prompt that allows the user to perform various operations on a chosen file.
 */
public class Main {

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
            if(mode > 0 && mode < 5) {
                inByte = FileIO.getFile();
                if(inByte != null) {
                    operation(mode, inByte, sc);
                }
            }
            System.out.println();
        }
        sc.close();
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
            default -> System.out.println("Error with main.operation() switch statement.");
        }
    }

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
            out[i] = (byte) (out[i] ^ inByte[i + 64]);
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
            System.out.println("Failed to validate cryptogram. No output will be written.\n");
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
     * to an invalid number.
     * @param sc Scanner to get input from the user.
     * @return Entered integer or -1 if non-integer entered.
     */
    private static int getMode(Scanner sc) {
        int mode;
        String input;
        System.out.print("Please choose a mode by entering the corresponding number:\n1. Compute Hash" +
                "\n2. Symmetrically encrypt a file using KMACXOF256\n3. " +
                "Symmetrically decrypt a file using KMACXOF256\n4. Compute authentication tag\n" +
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
