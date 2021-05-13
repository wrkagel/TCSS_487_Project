import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileIO {


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
                System.out.println("No file was chosen or there was an error. Returning to main menu.\n");
                return null;
            }
        }
    }

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
                    for (int i = 0; i < outByte.length; i++) {
                        String hex = String.format("%02X", outByte[i]);
                        outputStream.write(hex.getBytes(StandardCharsets.UTF_8));
                    }
                } else {
                    System.out.println("There was an error writing to the file. Returning to main menu.\n");
                }
            } catch (FileNotFoundException e) {
                System.out.println("The file at " + outFile.getPath() + ". Could not be found.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No file was chosen or there was an error. Returning to main menu.\n");
        }

    }

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
                    System.out.println("There was an error writing to the file. Returning to main menu.\n");
                }
            } catch (FileNotFoundException e) {
                System.out.println("The file at " + outFile.getPath() + ". Could not be found.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No file was chosen or there was an error. Returning to main menu.\n");
        }

    }
}
