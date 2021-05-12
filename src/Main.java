import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        byte[] k = new byte[0];
        byte[] x = new byte[1600 / 8];
        for (int i = 0; i < x.length; i++) {
            x[i] = (byte) i;
        }
        byte[] s = "".getBytes(StandardCharsets.UTF_8);
        int l = 512;
        byte[] md = KMACXOF.KMACXOF256(k, x, l, s);
        KMACXOF.printArray(md, "md");
    }

}
