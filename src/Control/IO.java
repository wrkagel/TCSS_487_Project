package Control;

import View.GUI;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/*
    TCSS 487
    Project
    Walter Kagel
    5/19/2021
 */

/**
 * Controls the input and output from/to files for the program.
 */
public class IO {

    /**
     * Lets the user choose a file using JFileChooser and then reads it in as a byte[].
     * @param view GUI that the JOptionPane will be associated with.
     * @param message Message to display on JOptionPane
     * @return The read in byte[]
     */
    public static byte[] getFile(GUI view, String message) {
        String result = "Result string of getFile not set.";
        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogTitle(message);
        File inFile;
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(view)) {
            inFile = chooser.getSelectedFile();
            try (FileInputStream inputStream = new FileInputStream(inFile)) {
                return inputStream.readAllBytes();
            } catch (FileNotFoundException e) {
                result = "The file at " + inFile.getPath() + ". Could not be found.";
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else{
            result = "No file was chosen or there was an error.";
        }
        showMessage(view, result);
        return null;
    }

    /**
     * Writes a byte array to a file chosen using JFileChooser. The bytes array is written out as a series of hex
     * characters with capital letters and no spaces or other punctuation.
     * @param outByte Data to write as a byte[]
     * @param view GUI that the JOptionPane will be associated with.
     * @param message Message to display on JOptionPane
     */
    public static void writeHex(byte[] outByte, GUI view, String message) {
        String result = "Successfully wrote to file.";
        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogTitle(message);
        File outFile;
        if(JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(view)) {
            outFile = chooser.getSelectedFile();
            try {
                outFile.delete();
                if (outFile.createNewFile()) {
                        FileOutputStream outputStream = new FileOutputStream(outFile);
                        for (byte b : outByte) {
                            String hex = String.format("%02X", b);
                            outputStream.write(hex.getBytes(StandardCharsets.UTF_8));
                        }
                    } else {
                        result = "There was an error while writing to the file.";
                }
        } catch (FileNotFoundException e) {
                result = "The file at " + outFile.getPath() + ". Could not be found.";
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            result = "No file was chosen or there was an error.";
        }
        showMessage(view, result);

    }

    /**
     * Writes a byte[] to a file chosen using JFileChooser. The byte[] is written directly to the file.
     * @param outByte data to write as a byte[].
     * @param view GUI that the JOptionPane will be associated with.
     * @param message Message to display on JOptionPane
     */
    public static void writeBytes(byte[] outByte, GUI view, String message) {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogTitle(message);
        File outFile;
        String result = "Successfully wrote to file.";
        if(JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(view)) {
            outFile = chooser.getSelectedFile();
            try {
                outFile.delete();
                if(outFile.createNewFile()) {
                    FileOutputStream outputStream = new FileOutputStream(outFile);
                    outputStream.write(outByte);
                } else {
                    result = "There was an error writing to the file.";
                }
            } catch (FileNotFoundException e) {
                result = "The file at " + outFile.getPath() + ". Could not be found.";
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            result = "No file was chosen or there was an error.";
        }
        showMessage(view, result);
    }

    /**
     * Gets a password from the user.
     * @param view GUI that the JOptionPane will be associated with.
     * @param message Message to display on JOptionPane
     * @return user password as byte[]
     */
    public static byte[] getPassword(GUI view, String message) {
        String input = JOptionPane.showInputDialog(view, message);
        return input == null ? null : input.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Shows a message to the user using a standard dialog box.
     * @param view GUI that the JOptionPane will be associated with
     * @param message Message to display.
     */
    public static void showMessage(GUI view, String message) {
        JOptionPane.showMessageDialog(view, message);
    }

}
