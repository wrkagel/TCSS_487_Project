import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        byte[] test = KMACXOF.encodeString(new byte[] {0, 4, 3});
        System.out.println(Arrays.toString(test));
    }

}
