package Control;

import Model.FileIO;
import Model.KMACXOF256;
import View.GUI;

import java.nio.charset.StandardCharsets;

public class Controller {

    private final GUI view;

    public Controller(GUI view) {
        this.view = view;
    }

    public void computeHash(byte[] inByte) {
        if(inByte == null) {
            inByte = FileIO.getFile(view, "Select file to hash.");
            if(inByte == null) return;
        }
        //Values for the hash function taken from the assignment specifications.
        String s = "D";
        byte[] outByte = KMACXOF256.compute(new byte[0], inByte, 512, s.getBytes(StandardCharsets.UTF_8));
        FileIO.writeHex(outByte, view, "Save resulting hash to a file");
    }

}
