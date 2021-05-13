/*
    TCSS 487
    Project
    Walter Kagel
    4/25/2021
 */

/**
 * Class that can perform Keccak-1600 Encryption and decryption on a long[25] data array.
 * Specifications taken from https://keccak.team/keccak_specs_summary.html and code developed
 * using the project at https://github.com/mjosaarinen/tiny_sha3 as a reference.
 */
public class Sha3 {

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
            0x00FFFFFFFFFFFFFFl, 0xFF00FFFFFFFFFFFFl, 0xFFFF00FFFFFFFFFFl, 0xFFFFFF00FFFFFFFFl,
            0xFFFFFFFF00FFFFFFl, 0xFFFFFFFFFF00FFFFl, 0xFFFFFFFFFFFF00FFl, 0xFFFFFFFFFFFFFF00l
    };

    /**
     * Array that all permutations will be performed on.
     */
    private long[] st = new long[25];

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
    Sha3(int mdlen) {
        for (int i = 0; i < 25; i++) {
            st[i] = 0l;
        }
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
        printArray(st);
        j = rsize - 1;
        byte b = ((byte) (st[j / 8] >>> (8 * (7 - j % 8))));
        st[j/8] = st[j/8] & masks[j % 8];
        b ^= 0x80;
        st[j / 8] |= Byte.toUnsignedLong(b) << (8 * (7 - j % 8));
        printArray(st);
        sha3Keccak1600();
        printArray(st);
        pt = j;
    }

    /**
     * Squeezes out a number of bits from the sponge function equal to the message digest length set at creation.
     * @return byte[] array of size mdlen
     */
    public byte[] sha3Final() {
        int i = 0;

        byte[] md = new byte[mdlen];
        int temp = mdlen;

        while(temp > rsize) {
            for(i = 0; i < rsize; i++) {
                md[md.length - temp + i] = ((byte) (st[i / 8] >>> (8 * (7 - i % 8))));
            }
            sha3Keccak1600();
            temp -= rsize;
        }
        for (i = 0; i < temp; i++) {
            md[md.length - temp + i] = ((byte) (st[i / 8] >>> (8 * (7 - i % 8))));
        }


        return md;

    }

    public void printArray(long[] test) {
        System.out.print("permutation: ");
        for(int i = 0; i < test.length; i++) {
                System.out.print(String.format("%016x, ", test[i]));
        }
        System.out.println();
    }

}
