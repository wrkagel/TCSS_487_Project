package Model;

/*
    TCSS 487
    Project
    Walter Kagel
    4/27/2021
 */

import java.util.Arrays;

/**
 * Class that can perform Keccak-1600 Encryption and decryption on a long[25] data array.
 * Specifications taken from https://keccak.team/keccak_specs_summary.html and code developed
 * using the project at https://github.com/mjosaarinen/tiny_sha3 as a reference.
 */
public class Keccak1600 {

    /**
     * Round Constant values for Keccak-1600. Values copied from the Round Constants table at
     * https://keccak.team/keccak_specs_summary.html.
     */
    private static final long[] RC = new long[] {
            0x0000000000000001L,0x0000000000008082L,0x800000000000808AL,0x8000000080008000L,
            0x000000000000808BL,0x0000000080000001L,0x8000000080008081L,0x8000000000008009L,
            0x000000000000008AL,0x0000000000000088L,0x0000000080008009L,0x000000008000000AL,
            0x000000008000808BL,0x800000000000008BL,0x8000000000008089L,0x8000000000008003L,
            0x8000000000008002L,0x8000000000000080L,0x000000000000800AL,0x800000008000000AL,
            0x8000000080008081L,0x8000000000008080L,0x0000000080000001L,0x8000000080008008L};

    /**
     * Round Offset values for Keccak-1600. Values copied from the tiny_sha3 at
     * looking at the project https://github.com/mjosaarinen/tiny_sha3.
     */
    private static final int[] RO = new int[] {
            1,  3,  6,  10, 15, 21, 28, 36, 45, 55, 2,  14,
            27, 41, 56, 8,  25, 43, 62, 18, 39, 61, 20, 44};

    /**
     * Values for Keccak-1600. Values copied from the tiny_sha3 at
     * looking at the project https://github.com/mjosaarinen/tiny_sha3.
     */
    private static final int[] piln = new int[] {
            10, 7,  11, 17, 18, 3, 5,  16, 8,  21, 24, 4,
            15, 23, 19, 13, 12, 2, 20, 14, 22, 9,  6,  1};

    /**
     * Masks are used to make space in a long value for a byte to be added.
     */
    private static final long[] masks = new long[] {
            0x00FFFFFFFFFFFFFFL, 0xFF00FFFFFFFFFFFFL, 0xFFFF00FFFFFFFFFFL, 0xFFFFFF00FFFFFFFFL,
            0xFFFFFFFF00FFFFFFL, 0xFFFFFFFFFF00FFFFL, 0xFFFFFFFFFFFF00FFL, 0xFFFFFFFFFFFFFF00L
    };

    /**
     * Array that all permutations will be performed on.
     */
    private final long[] st = new long[25];

    /**
     * pt points to the current point in the st array.
     */
    private int pt;

    /**
     * rsize is used to denote the block size and mdlen denotes the length of the desired output.
     */
    private final int rsize, mdlen;

    /**
     * Constructs an instance of Sha3 that will output a given message digest length. rsize is constant for KMACXOF256
     * that this class is designed to work with and is therefore not a variable.
     * @param mdlen message digest length
     */
    Keccak1600(int mdlen) {
        Arrays.fill(st, 0);
        this.mdlen = mdlen;
        this.rsize = 136;
        this.pt = 0;
    }

    /**
     * Performs the keccak[1600] permutations on the internal st array.
     */
    private void sha3Keccak1600() {
        int i, j, r;
        long t;
        long[] bc = new long[5];

        //Reverse byte order to simulate little endian when performing permutations
        //This changes some rotation values during the permutations.
        for(i = 0; i < 25; i++) {
            st[i] = Long.reverseBytes(st[i]);
        }

        for (r = 0; r < 24; r++) {
            //Theta
            for (i = 0; i < 5; i++) {
                bc[i] = st[i] ^ st[i + 5] ^ st[i + 10] ^ st[i + 15] ^ st[i + 20];
            }
            for (i = 0; i < 5; i++) {
                t = bc[(i + 4) % 5] ^ Long.rotateLeft(bc[(i + 1) % 5], 1);
                for (j = 0; j < 25; j += 5) {
                    st[j + i] ^= t;
                }
            }

            //Rho Pi
            t = st[1];
            for (i = 0; i < 24; i++) {
                j = piln[i];
                bc[0] = st[j];
                st[j] = Long.rotateLeft(t, RO[i]);
                t = bc[0];
            }

            //Chi
            for (j = 0; j < 25; j += 5) {
                for (i = 0; i < 5; i++)
                    bc[i] = st[j + i];
                for (i = 0; i < 5; i++)
                    st[j + i] ^= (~bc[(i + 1) % 5]) & bc[(i + 2) % 5];
            }

            //Iota
            st[0] ^= RC[r];
        }

        //Flip the bytes back into their proper positions
        for (i = 0; i < 25; i++) {
            st[i] = Long.reverseBytes(st[i]);
        }

    }

    /**
     * Absorbs the data into the keccak[1600] sponge function one block at a time.
     * @param data input data
     */
    public void sha3Update( byte[] data) {
        int i, j = pt;
        for (i = 0; i < data.length; i++) {
            byte b = ((byte) (st[j / 8] >>> (8 * (7 - j % 8))));
            st[j/8] = st[j/8] & masks[i % 8];
            b ^= data[i];
            st[j / 8] |= Byte.toUnsignedLong(b) << (8 * (7 - j % 8));
            j++;
            if(j >= rsize) {
                sha3Keccak1600();
                j = 0;
            }
        }
        j = rsize - 1;
        byte b = ((byte) (st[j / 8] >>> (8 * (7 - j % 8))));
        st[j/8] = st[j/8] & masks[j % 8];
        b ^= 0x80;
        st[j / 8] |= Byte.toUnsignedLong(b) << (8 * (7 - j % 8));
        sha3Keccak1600();
        pt = j;
    }

    /**
     * Squeezes out a number of bits from the sponge function equal to the message digest length set at creation.
     * @return byte[] array of size mdlen
     */
    public byte[] sha3Final() {
        byte[] md = new byte[mdlen];
        int temp = mdlen;

        while(temp > rsize) {
            for(int i = 0; i < rsize; i++) {
                md[md.length - temp + i] = ((byte) (st[i / 8] >>> (8 * (7 - i % 8))));
            }
            sha3Keccak1600();
            temp -= rsize;
        }
        for (int i = 0; i < temp; i++) {
            md[md.length - temp + i] = ((byte) (st[i / 8] >>> (8 * (7 - i % 8))));
        }
        return md;

    }

    /**
     * Used for testing. Prints out the state at various points as long separated hex characters.
     * @param test long[] to be printed
     */
    private void printArray(long[] test) {
        System.out.print("permutation: ");
        for (long l : test) {
            System.out.printf("%016X, ", l);
        }
        System.out.println();
    }

}
