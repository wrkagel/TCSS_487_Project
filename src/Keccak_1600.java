/*
    TCSS 487
    Project
    Walter Kagel
    4/25/2021
 */

/**
 * Class that can perform Keccak-1600 Encryption and decryption on a long[5][5] A.
 * Specifications taken from https://keccak.team/keccak_specs_summary.html and confirmed by
 * looking at the project https://github.com/mjosaarinen/tiny_sha3.
 */
public class Keccak_1600 {

    /**
     * Round Constant values for Keccak-1600. Values copied from the Round Constants table at
     * https://keccak.team/keccak_specs_summary.html.
     */
    private static final long[] RC = new long[] {
            0x0000000000000001l,0x0000000000008082l,0x800000000000808Al,0x8000000080008000l,
            0x000000000000808Bl,0x0000000080000001l,0x8000000080008081l,0x8000000000008009l,
            0x000000000000008Al,0x0000000000000088l,0x0000000080008009l,0x000000008000000Al,
            0x000000008000808Bl,0x800000000000008Bl,0x8000000000008089l,0x8000000000008003l,
            0x8000000000008002l,0x8000000000000080l,0x000000000000800Al,0x800000008000000Al,
            0x8000000080008081l,0x8000000000008080l,0x0000000080000001l,0x8000000080008008l};

    /**
     * Round Offset values for Keccak-1600. Values copied from the tiny_sha3 at
     * looking at the project https://github.com/mjosaarinen/tiny_sha3.
     */
    private static final int[] RO = new int[] {
            1,  3,  6,  10, 15, 21, 28, 36, 45, 55, 2,  14,
            27, 41, 56, 8,  25, 43, 62, 18, 39, 61, 20, 44};

    /**
     * Performs the steps for Keccak-1600 encryption.
     * @param A long[5][5] for the steps to be performed on.
     */
    public static void keccak1600Permutations(long[][] A) {
        for(int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                A[i][j] = Long.reverseBytes(A[i][j]);
            }
        }
        for (int i = 0; i < 24; i++) {
            round1600(A, RC[i], RO[i]);
        }
    }

    /**
     * Performs the 5 transformations within a single round of Keccak-1600. Adapted from pseudo-code at
     * https://keccak.team/keccak_specs_summary.html.
     * @param A A long[5][5] array that the transformations will be applied to.
     * @param RC The round constant.
     */
    private static void round1600(long[][] A, long RC, int RO) {
        long[] C = new long[5];
        long[] D = new long[5];
        long[][] B = new long[5][5];
        //Step 1 Theta
        for(int i = 0; i < 5; i++) {
            C[i] = A[i][4] ^ A[i][3] ^ A[i][2] ^ A[i][1] ^ A[i][0];
        }
        for(int i = 0; i < 5; i++) {
            D[i] = C[(i + 4) % 5] ^ Long.rotateLeft(C[(i+1)%5], 1);
        }
        for(int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                A[j][i] ^= D[i];
            }
        }
        //Step 2 & 3 Rho & Pi
        for(int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                B[(2*i + 3*j)%5][j] = Long.rotateLeft(A[j][i],RO);
            }
        }
        //Step 4 Chi
        for(int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                A[j][i] = B[j][i] ^ ((~(B[j][(i + 1) % 5])) & B[j][(i+2)%5]);
            }
        }
        //Step 5 Iota
        A[0][0] ^= RC;
    }

}
