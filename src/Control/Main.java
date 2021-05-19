package Control;

import View.GUI;

/**
 * Contains main for an application that allows a user to perform various cryptographic functions on files
 * and input text. These functions are hashing, symmetric encryption, symmetric decryption, and creating a tag using
 * KMACXOF256 and generating a public key, asymmetric encryption, asymmetric decryption, digitally signing a file,
 * and verifying a digital signature using Schnorr/ECDHIES, the E-521 curve, and KMACXOF256.
 */
public class Main {

    /**
     * Starts the program.
     * @param args N/A
     */
    public static void main(String[] args) {
        GUI view = new GUI();
        Controller cont = new Controller(view);
        view.setCont(cont);
    }
}
