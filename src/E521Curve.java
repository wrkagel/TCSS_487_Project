import java.math.BigInteger;

public class E521Curve {

    /**
     *
     */
    private static final BigInteger d = BigInteger.valueOf(-376014);

    public static final BigInteger p = BigInteger.TWO.pow(521).subtract(BigInteger.ONE);

    public static final BigInteger r = BigInteger.TWO.pow(519).subtract(new BigInteger("337554763258501705789107630" +
            "418782636071904961214051226618635150085779108655765"));

    public static final E521Curve g = new E521Curve(new BigInteger("529374276594561432744596104918707564862413794" +
            "97403995879568382780332381890164675224899684445938504542046223834763665908437320829964399700635619771427" +
            "59015827"), false);

    private final BigInteger x;

    private final BigInteger y;

    E521Curve() {
        x = BigInteger.ZERO;
        y = BigInteger.ONE;
    }

    E521Curve(BigInteger x, boolean yLsb) {
        this(x, calculateY(x, yLsb).mod(p));
    }

    E521Curve(BigInteger x, BigInteger y) {
        BigInteger left = (x.pow(2)).add(y.pow(2)).mod(p);
        BigInteger right = BigInteger.ONE.add(d.multiply(x.pow(2)
                .multiply(y.pow(2)))).mod(p);
        if (left.compareTo(right) == 0) {
            this.x = x;
            this.y = y;
        } else {
            throw new IllegalArgumentException("x and y must satisfy the equation x^2 + y^2 = 1 + d(x^2)(y^2).");
        }
    }

    public E521Curve add(E521Curve oth) {
        BigInteger num1 = x.multiply(oth.y).add(y.multiply(oth.x)).mod(p);
        BigInteger num2 = y.multiply(oth.y).subtract(x.multiply(oth.x)).mod(p);
        BigInteger den1 = BigInteger.ONE.add(d.multiply(x).multiply(oth.x).multiply(y).multiply(oth.y)).mod(p);
        BigInteger den2 = BigInteger.ONE.subtract(d.multiply(x).multiply(oth.x).multiply(y).multiply(oth.y)).mod(p);
        den1 = den1.modInverse(p);
        den2 = den2.modInverse(p);
        return new E521Curve(num1.multiply(den1).mod(p), num2.multiply(den2).mod(p));
    }

    public E521Curve scalarMultiply(BigInteger s) {
        if(s.equals(BigInteger.ZERO)) {
            return new E521Curve();
        }
        E521Curve v = new E521Curve(x, y);
        for (int i = s.bitLength() - 2; i >= 0; i--) {
            v = v.add(v);
            if(s.testBit(i)) {
                v = v.add(this);
            }
        }
        return v;
    }

    public boolean equals(E521Curve oth) {
        return (x.equals(oth.x) && y.equals(oth.y));
    }

    private static BigInteger calculateY(BigInteger x, boolean yLsb) {
        BigInteger radicand = BigInteger.ONE.subtract(x.modPow(BigInteger.TWO, p));
        radicand = radicand.multiply(BigInteger.ONE.subtract(d.multiply(x.modPow(BigInteger.TWO, p))).modInverse(p));
        return sqrt(radicand, p, yLsb);
    }

    /**
     * Compute a square root of v mod p with a specified least significant bit, if such a root exists.
     * Taken from the project assignment specifications.
     * @param   v   the radicand.
     * @param   p   the modulus (must satisfy p mod 4 = 3).*
     * @param   lsb desired least significant bit (true: 1, false: 0).
     * @return  a square root r of v mod p with r mod 2 = 1 iff lsb = true if such a root exists, otherwise null.
     */
    private static BigInteger sqrt(BigInteger v, BigInteger p, boolean lsb) {
        assert (p.testBit(0) && p.testBit(1)); // p = 3 (mod 4)
        if (v.signum() == 0) {
            return BigInteger.ZERO;
        }
        BigInteger r = v.modPow(p.shiftRight(2).add(BigInteger.ONE), p);
        if (r.testBit(0) != lsb) {
            r = p.subtract(r); // correct the lsb

        }
        return (r.multiply(r).subtract(v).mod(p).signum() == 0) ? r : null;
    }

    public BigInteger getX() {
        return x.add(BigInteger.ZERO);
    }

    public BigInteger getY() {
        return y.add(BigInteger.ZERO);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        sb.append(x.toString());
        sb.append(", ");
        sb.append(y.toString());
        sb.append(")");
        return sb.toString();
    }

}
