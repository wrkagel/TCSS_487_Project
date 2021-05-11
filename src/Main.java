import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        byte[] k = new byte[] {0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E,
                0x4F, 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F};
        for(int i = 0; i < k.length; i++) {
            k[i] = KMACXOF.enc8(k[i]);
        }
        byte[] x = new byte[] {0x00, 0x01, 0x02, 0x03};
        for(int i = 0; i < x.length; i++) {
            x[i] = KMACXOF.enc8(x[i]);
        }
        byte[] s = "My Tagged Application".getBytes(StandardCharsets.UTF_8);
        int l = 512;
        KMACXOF.KMACXOF256(k, x, l, s);
    }

}
