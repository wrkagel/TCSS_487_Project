import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

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

    private static void computeTag(byte[] inByte, Scanner sc) {
        String pw = getPassword(sc);
        String s = "T";
        byte[] outByte = KMACXOF256.compute(pw.getBytes(StandardCharsets.UTF_8), inByte,
                512, s.getBytes(StandardCharsets.UTF_8));
        FileIO.writeBytes(outByte);
    }

    private static String getPassword(Scanner sc) {
        System.out.println("Please enter a password to use for the authentication tag: ");
        return sc.nextLine();
    }

    private static void computeHash(byte[] inByte) {
        String s = "D";
        byte[] outByte = KMACXOF256.compute(new byte[0], inByte, 512, s.getBytes(StandardCharsets.UTF_8));
        FileIO.writeHex(outByte);
    }

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
