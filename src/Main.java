import java.nio.charset.StandardCharsets;
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
            case 1:
                computeHash(inByte);
                break;
            case 2:
            case 3:
            case 4:
                computeTag(inByte, sc);
                break;
            case 5:
            default:
                System.out.println("Error with main.operation() switch statement.");
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
        System.out.println("Please enter a password to use for the authentication tag: ");
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
