package View;

import Control.Controller;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;

/*
    TCSS 487
    Project
    Walter Kagel
    5/19/2021
 */

/**
 * The GUI of the application. Contains the area for text input and the buttons to run the functions.
 */
public class GUI extends JFrame{

    /**
     * Text input area for when the user wants to use text input.
     */
    private final JTextArea input = new JTextArea("Text input can be enabled in the InputType menu above.",
            50, 50);

    /**
     * The menu checkbox that tells the system whether the user wants to use text input or not.
     */
    private final JCheckBoxMenuItem textInputCheck = new JCheckBoxMenuItem("Enable Text Input");

    /**
     * The controller that all user actions requiring encryption functions will be passed to.
     */
    private Controller cont;

    /**
     * Holds the screen size.
     */
    private Dimension screenSize;

    /**
     * Constructs the GUI. Note that the controller must be set after construction or errors will occur when attempting
     * to use any of the buttons.
     */
    public GUI() {
        this.setTitle("TCSS 487: Project by wrkagel");
        Toolkit t = Toolkit.getDefaultToolkit();
        screenSize = t.getScreenSize();
        this.setBounds(0, 0, (int) (screenSize.getWidth() / 1.5), (int) (screenSize.getHeight() / 1.5));
        this.setLayout(new BorderLayout());
        createMenu();
        createEast();
        createCenter();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    /**
     * Sets the controller associated with the GUI. Must be set before user presses any of the action buttons
     * or errors will be created.
     * @param cont controller that runs all encryption functions.
     */
    public void setCont(Controller cont) {
        this.cont = cont;
    }

    /**
     * Creates and sets the menu bar of the application.
     */
    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu inputType = new JMenu("InputType");
        menuBar.add(inputType);
        inputType.add(textInputCheck);
        this.setJMenuBar(menuBar);
        textInputCheck.addActionListener(e -> {
            if (textInputCheck.isSelected()) {
                input.setText("");
                input.setEditable(true);
                input.setBackground(Color.WHITE);
            } else {
                input.setText("Text input can be enabled in the InputType menu above.");
                input.setEditable(false);
                input.setBackground(Color.LIGHT_GRAY);
            }
        });
    }

    /**
     * Creates and sets the buttons of the application. Contains all action calls to the controller.
     */
    private void createEast() {
        JPanel east = new JPanel();
        east.setLayout(new GridLayout(9, 1));

        //Hash button
        JButton hash = new JButton("Compute Hash");
        hash.addActionListener(e -> {
            if(textInputCheck.isSelected()) {
                cont.computeHash(input.getText().getBytes(StandardCharsets.UTF_8));
            } else {
                cont.computeHash(null);
            }
        });
        hash.setToolTipText("Create a hash of the input using KMACXOF256 and save it to a file.");
        east.add(hash);

        //Symmetric Encrypt
        JButton symEncrypt = new JButton("Symmetric Encrypt");
        symEncrypt.addActionListener(e -> {
            if(textInputCheck.isSelected()) {
                cont.symmetricEncrypt(input.getText().getBytes(StandardCharsets.UTF_8));
            } else {
                cont.symmetricEncrypt(null);
            }
        });
        symEncrypt.setToolTipText("Encrypt the input using KMACXOF256 and save it to a file.");
        east.add(symEncrypt);

        //Symmetric Decrypt
        JButton symDecrypt = new JButton("Symmetric Decrypt");
        symDecrypt.addActionListener(e -> {
            if(textInputCheck.isSelected()) {
                cont.symmetricDecrypt(input.getText().getBytes(StandardCharsets.UTF_8));
            } else {
                cont.symmetricDecrypt(null);
            }
        });
        symDecrypt.setToolTipText("Decrypt the input using KMACXOF256 and save it to a file.");
        east.add(symDecrypt);

        //Authentication Tag
        JButton authTag = new JButton("Authentication Tag");
        authTag.addActionListener(e -> {
            if(textInputCheck.isSelected()) {
                cont.authentication(input.getText().getBytes(StandardCharsets.UTF_8));
            } else {
                cont.authentication(null);
            }
        });
        authTag.setToolTipText("Make an authentication tag \"MAC\" for the input and save it to a file.");
        east.add(authTag);

        //Create key pair
        JButton keyPair = new JButton("Create Key Pair");
        keyPair.addActionListener(e -> cont.keyPair());
        keyPair.setToolTipText("Create a public key based on the entered password using the E521 curve (and KMACXOF256) " +
                "and save it to a file.");
        east.add(keyPair);

        //Asymmetric Encryption
        JButton asEncrypt = new JButton("Asymmetric Encrypt");
        asEncrypt.addActionListener(e -> {
            if(textInputCheck.isSelected()) {
                cont.asEncrypt(input.getText().getBytes(StandardCharsets.UTF_8));
            } else {
                cont.asEncrypt(null);
            }
        });
        asEncrypt.setToolTipText("Encrypt the input based on a public key file using the E521 curve (and KMACXOF256) " +
                "and save it to a file.");
        east.add(asEncrypt);

        //Asymmetric Decryption
        JButton asDecrypt = new JButton("Asymmetric Decrypt");
        asDecrypt.addActionListener(e -> {
            if(textInputCheck.isSelected()) {
                cont.asDecrypt(input.getText().getBytes(StandardCharsets.UTF_8));
            } else {
                cont.asDecrypt(null);
            }
        });
        asDecrypt.setToolTipText("Decrypt the input based on the entered password using the E521 curve (and KMACXOF256) " +
                "and save it to a file.");
        east.add(asDecrypt);

        //Create Digital Signature
        JButton createSig = new JButton("Create Digital Signature");
        createSig.addActionListener(e -> cont.createSig());
        createSig.setToolTipText("Create a digital signature based on a chosen file and entered password, then save " +
                "the signature to a file.");
        east.add(createSig);

        //Verify Digital Signature
        JButton verifySig = new JButton("Verify Digital Signature");
        verifySig.addActionListener(e -> cont.verifySig());
        verifySig.setToolTipText("Verify a digital signature by choosing a signature file, the associated file, " +
                "and the associated public key. Verification is shown via a popup message.");
        east.add(verifySig);

        east.setPreferredSize(new Dimension((int) (screenSize.getWidth() * 0.1), 0));

        this.add(east, BorderLayout.EAST);
    }

    /**
     * Creates the text input area of the application.
     */
    private void createCenter() {
        input.setEditable(false);
        input.setBackground(Color.LIGHT_GRAY);
        JScrollPane pane = new JScrollPane(input);
        this.add(pane, BorderLayout.CENTER);
    }

}
