import javax.swing.JFileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Controls the input and output from/to files for the program.
 */
public class FileIO {

    /**
     * Lets the user choose a file using JFileChooser and then reads it in as a byte[].
     * @return The read in byte[]
     */
    public static byte[] getFile() {
        while(true) {
            System.out.println("Please choose a file to operate on (*May need to tab to file chooser).");
            JFileChooser chooser = new JFileChooser();
            File inFile;
            if(JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(null)) {
                inFile = chooser.getSelectedFile();
                try (FileInputStream inputStream = new FileInputStream(inFile)) {
                    return inputStream.readAllBytes();
                } catch (FileNotFoundException e) {
                    System.out.println("The file at " + inFile.getPath() + ". Could not be found.");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            } else {
                System.out.println("No file was chosen or there was an error. Returning to main menu.");
                return null;
            }
        }
    }

    /**
     * Writes a byte array to a file chosen using JFileChooser. The bytes array is written out as a series of hex
     * characters with capital letters and no spaces or other punctuation.
     * @param outByte Data to write as a byte[]
     */
    public static void writeHex(byte[] outByte) {
        System.out.println("Please choose a file to save to (*May need to tab to file chooser).");
        JFileChooser chooser = new JFileChooser();
        File outFile;
        if(JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(null)) {
            outFile = chooser.getSelectedFile();
            try {
                outFile.delete();
                if(outFile.createNewFile()) {
                    FileOutputStream outputStream = new FileOutputStream(outFile);
                    for (byte b : outByte) {
                        String hex = String.format("%02X", b);
                        outputStream.write(hex.getBytes(StandardCharsets.UTF_8));
                    }
                } else {
                    System.out.println("There was an error writing to the file. Returning to main menu.");
                }
            } catch (FileNotFoundException e) {
                System.out.println("The file at " + outFile.getPath() + ". Could not be found.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No file was chosen or there was an error. Returning to main menu.");
        }

    }

    /**
     * Writes a byte[] to a file chosen using JFileChooser. The byte[] is written directly to the file.
     * @param outByte data to write as a byte[].
     */
    public static void writeBytes(byte[] outByte) {
        System.out.println("Please choose a file to save to (*May need to tab to file chooser).");
        JFileChooser chooser = new JFileChooser();
        File outFile;
        if(JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(null)) {
            outFile = chooser.getSelectedFile();
            try {
                outFile.delete();
                if(outFile.createNewFile()) {
                    FileOutputStream outputStream = new FileOutputStream(outFile);
                    outputStream.write(outByte);
                } else {
                    System.out.println("There was an error writing to the file. Returning to main menu.");
                }
            } catch (FileNotFoundException e) {
                System.out.println("The file at " + outFile.getPath() + ". Could not be found.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No file was chosen or there was an error. Returning to main menu.");
        }

    }
}
