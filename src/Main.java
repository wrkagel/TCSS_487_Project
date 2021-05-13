import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {

        setupGUI();


        byte[] k = new byte[256 / 8];
        {
            for(int i = 0; i < k.length; i++) {
                k[i] = (byte) (0x40 + i);
            }
        }
        byte[] x = new byte[1600 / 8];
        for (int i = 0; i < x.length; i++) {
            x[i] = (byte) i;
        }
        byte[] s = "My Tagged Application".getBytes(StandardCharsets.UTF_8);
        int l = 512;
        byte[] md = KMACXOF256.KMACXOF256(k, x, l, s);
        KMACXOF256.printArray(md, "md");
    }

    private static void setupGUI() {
        var notFixed = new Object() {
            byte[] readIn;
        };
        JFrame gui = new JFrame();
        gui.setLayout(new BorderLayout());
        JMenuBar menu = new JMenuBar();
        gui.setJMenuBar(menu);
        JMenu file = new JMenu("File");
        menu.add(file);
        JMenu inputMode = new JMenu("Input Mode");
        menu.add(inputMode);
        JCheckBoxMenuItem textIn = new JCheckBoxMenuItem("Text Input Enabled");
        inputMode.add(textIn);
        JMenuItem menuItem = new JMenuItem("Open");
        file.add(menuItem);
        JTextArea input = new JTextArea(50, 50);
        input.setEditable(false);
        textIn.addActionListener(e -> {
            if (textIn.isSelected()) {
                input.setText("");
                input.setEditable(true);
            } else {
                input.setText("");
                input.setEditable(false);
            }
        });
        menuItem.addActionListener(e -> {
            if(textIn.isSelected()) textIn.doClick();
            JFileChooser choose = new JFileChooser();
            choose.showOpenDialog(gui);
            File chosen = choose.getSelectedFile();
            try {
                FileInputStream in = new FileInputStream(chosen);
                input.setText("Currently selected file: " + chosen.getPath());
                notFixed.readIn = in.readAllBytes();
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        JTextArea output = new JTextArea(50, 50);
        gui.add(input, BorderLayout.WEST);
        gui.add(output, BorderLayout.EAST);
        JPanel middle = new JPanel();
        middle.setLayout(new GridLayout());
        JButton hash = new JButton("Simple Hash ->");
        hash.addActionListener(e -> {
            byte[] md = KMACXOF256.KMACXOF256(new byte[0], notFixed.readIn,
                    512, "D".getBytes(StandardCharsets.UTF_8));
            for(int i = 0; i < md.length; i++) {
                output.append(Character.toString((char) md[i]));
            }
        });
        hash.setSize(200, 200);
        middle.add(hash);
        gui.add(middle, BorderLayout.CENTER);
        gui.pack();
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gui.setVisible(true);
    }

}
