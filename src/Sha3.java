/*
    TCSS 487
    Project
    Walter Kagel
    4/25/2021
 */

/**
 * Class that can perform Keccak-1600 Encryption and decryption on a long[25] data.
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
     * Round Offset values for Keccak-1600. Values copied from the tiny_sha3 at
     * looking at the project https://github.com/mjosaarinen/tiny_sha3.
     */
    private static final int[] piln = new int[] {
            10, 7,  11, 17, 18, 3, 5,  16, 8,  21, 24, 4,
            15, 23, 19, 13, 12, 2, 20, 14, 22, 9,  6,  1};

    /**
     * Array that all permutations will be performed on.
     */
    private long[] st = new long[25];

    private int pt, rsize, mdlen;

    Sha3(int mdlen) {
        for (int i = 0; i < 25; i++) {
            st[i] = 0l;
        }
        this.mdlen = mdlen;
        this.rsize = 200 - 2 * mdlen;
        this.pt = 0;
    }

    public void sha3Keccak1600() {
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

    public void sha3Update( byte[] data, int len) {
        int i, j = pt;
        for (i = 0; i < len; i++) {
            byte b = ((byte) (st[j / 8] >>> (8 * (7 - j % 8))));
            b ^= data[i];
            st[j / 8] |= ((long) b) << (8 * (7 - j % 8));
            j++;
            if(j >= rsize) {
                sha3Keccak1600();
                j = 0;
            }
        }
        pt = j;
    }

    public void sha3Final(byte[] md) {
        int i;

        byte b = ((byte) (st[pt / 8] >>> (8 * (7 - pt % 8))));
        b ^= 0x06;
        st[pt / 8] |= ((long) b) << (8 * (7 - pt % 8));

        b = ((byte) (st[(rsize - 1) / 8] >>> (8 * (7 - (rsize - 1) % 8))));
        b ^= 0x06;
        st[(rsize - 1) / 8] |= ((long) b) << (8 * (7 - (rsize - 1) % 8));

        for (i = 0; i < mdlen; i++) {
            md[i] = ((byte) (st[i / 8] >>> (8 * (7 - i % 8))));
        }

    }

}
