package View;

import Control.Controller;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class GUI extends JFrame{


    private final JTextArea input = new JTextArea("Text input can be enabled in the InputType menu above.",
            50, 50);

    private final JCheckBoxMenuItem textInputCheck = new JCheckBoxMenuItem("Enable Text Input");

    private Controller cont;

    public GUI() {
        this.setTitle("TCSS 487: Project by wrkagel");
        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getScreenSize();
        this.setLayout(new BorderLayout());
        createMenu();
        createWest();
        createCenter();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

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

    private void createCenter() {
        JPanel center = new JPanel();
        center.setLayout(new GridLayout(9, 1));

        //Hash button
        JButton hash = new JButton("Compute Hash");
        hash.addActionListener(e -> {
            if(textInputCheck.isSelected()) {
                cont.computeHash(input.getText().getBytes(StandardCharsets.UTF_8));
            } else {
                cont.computeHash(null);
            }
        });
        center.add(hash);

        //Symmetric Encrypt
        JButton symEncrypt = new JButton("Symmetric Encrypt");
        symEncrypt.addActionListener(e -> {
            if(textInputCheck.isSelected()) {
                cont.symmetricEncrypt(input.getText().getBytes(StandardCharsets.UTF_8));
            } else {
                cont.symmetricEncrypt(null);
            }
        });
        center.add(symEncrypt);

        //Symmetric Decrypt
        JButton symDecrypt = new JButton("Symmetric Decrypt");
        symDecrypt.addActionListener(e -> {
            if(textInputCheck.isSelected()) {
                cont.symmetricDecrypt(input.getText().getBytes(StandardCharsets.UTF_8));
            } else {
                cont.symmetricDecrypt(null);
            }
        });
        center.add(symDecrypt);

        //Authentication Tag
        JButton authTag = new JButton("Authentication Tag");
        authTag.addActionListener(e -> {
            if(textInputCheck.isSelected()) {
                cont.authentication(input.getText().getBytes(StandardCharsets.UTF_8));
            } else {
                cont.authentication(null);
            }
        });
        center.add(authTag);

        //Create key pair
        JButton keyPair = new JButton("Create Key Pair");
        keyPair.addActionListener(e -> cont.keyPair());
        center.add(keyPair);

        //Asymmetric Encryption
        JButton asEncrypt = new JButton("Asymmetric Encrypt");
        asEncrypt.addActionListener(e -> {
            if(textInputCheck.isSelected()) {
                cont.asEncrypt(input.getText().getBytes(StandardCharsets.UTF_8));
            } else {
                cont.asEncrypt(null);
            }
        });
        center.add(asEncrypt);

        //Asymmetric Decryption
        JButton asDecrypt = new JButton("Asymmetric Decrypt");
        asDecrypt.addActionListener(e -> {
            if(textInputCheck.isSelected()) {
                cont.asDecrypt(input.getText().getBytes(StandardCharsets.UTF_8));
            } else {
                cont.asDecrypt(null);
            }
        });
        center.add(asDecrypt);

        //Create Digital Signature
        JButton createSig = new JButton("Create Digital Signature");
        createSig.addActionListener(e -> cont.createSig());
        center.add(createSig);

        //Verify Digital Signature
        JButton verifySig = new JButton("Verify Digital Signature");
        verifySig.addActionListener(e -> cont.verifySig());
        center.add(verifySig);

        this.add(center, BorderLayout.CENTER);
    }

    private void createWest() {
        input.setEditable(false);
        input.setBackground(Color.LIGHT_GRAY);
        JScrollPane pane = new JScrollPane(input);
        this.add(pane, BorderLayout.WEST);
    }

    public void setCont(Controller cont) {
        this.cont = cont;
    }

}
