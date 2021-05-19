package Model;

import java.math.BigInteger;

/*
    TCSS 487
    Project
    Walter Kagel
    5/18/2021
 */

/**
 * Stores a point on the E-521 curve defined in
 * "A note on high-security general-purpose elliptic curves" by Diego F. Aranha,
 * Paulo S. L. M. Barreto, Geovandro C. C. F. Pereira, and Jefferson E. Ricardini.
 * Has methods for addition, scalar multiplication, checking equality, returning the x or y coordinate, and
 * returning a string representation of the point.
 * Curve equation: x^2 + y^2 = 1 + d(x^2)(y^2) where d = -376014
 * modulus p = 2^521 - 1
 */
public class E521CurvePoint {

    /**
     *  Value for d in curve equation. Taken from definition of the curve.
     */
    private static final BigInteger d = BigInteger.valueOf(-376014L);

    /**
     * Value of the modulus p for the E-521 curve. Taken from the definition of the curve.
     */
    public static final BigInteger p = BigInteger.TWO.pow(521).subtract(BigInteger.ONE);

    /**
     * Value of r for the curve. Copied from the programming assignment page and verified to match the curve definition.
     */
    public static final BigInteger r = BigInteger.TWO.pow(519).subtract(new BigInteger("337554763258501705789107630" +
            "418782636071904961214051226618635150085779108655765"));

    /**
     * Common basepoint for the curve. Taken from SafeCurves at https://safecurves.cr.yp.to/base.html.
     */
    public static final E521CurvePoint g = new E521CurvePoint(new BigInteger("15710548941849953875359397498943175" +
            "6864529735040290582143762518115230499438118852963259119606760410077267392791511426719338990500327" +
            "6673749012051148356041324"), BigInteger.valueOf(12L));

    /**
     * Stores the x coordinate of the curve point.
     */
    private final BigInteger x;

    /**
     * Stores the y coordinate of the curve point.
     */
    private final BigInteger y;

    /**
     * Create a curve point at the neutral element of addition. Values taken from the programming assignment.
     */
    E521CurvePoint() {
        x = BigInteger.ZERO;
        y = BigInteger.ONE;
    }

    /**
     * Create a curve point with a given x coordinate and the least significant bit of the y coordinate.
     * @param x x coordinate
     * @param yLsb denotes even or odd y value via least significant bit of y
     * @throws IllegalArgumentException if no such curve point exists.
     */
    public E521CurvePoint(BigInteger x, boolean yLsb) {
        this(x, calculateY(x, yLsb));
    }

    /**
     * Create a curve point with the given coordinates. Values are modded with p before the curve check is
     * performed.
     * @param x x coordinate
     * @param y y coordinate
     * @throws IllegalArgumentException if no such curve point exists.
     */
    public E521CurvePoint(BigInteger x, BigInteger y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("Not a valid curve point. x: " + x + ", y: " + y + ".");
        }
        x = x.mod(p);
        y = y.mod(p);
        BigInteger left = (x.pow(2)).add(y.pow(2)).mod(p);
        BigInteger right = BigInteger.ONE.add(d.multiply(x.pow(2)
                .multiply(y.pow(2)))).mod(p);
        if (left.compareTo(right) == 0) {
            this.x = x;
            this.y = y;
        } else {
            throw new IllegalArgumentException("Not a valid curve point. x: " + x + ", y: " + y + ".");
        }
    }

    /**
     * Add this curve point to another curve point using the addition formula described in the programming
     * assignment. Returns a new curve point based on that addition.
     * @param oth curve point to be added
     * @return resulting curve point after addition.
     */
    public E521CurvePoint add(E521CurvePoint oth) {
        BigInteger num1 = x.multiply(oth.y).add(y.multiply(oth.x)).mod(p);
        BigInteger num2 = y.multiply(oth.y).subtract(x.multiply(oth.x)).mod(p);
        BigInteger mult = d.multiply(x).multiply(oth.x).multiply(y).multiply(oth.y);
        BigInteger den1 = BigInteger.ONE.add(mult).mod(p);
        BigInteger den2 = BigInteger.ONE.subtract(mult).mod(p);
        den1 = den1.modInverse(p);
        den2 = den2.modInverse(p);
        return new E521CurvePoint(num1.multiply(den1).mod(p), num2.multiply(den2).mod(p));
    }

    /**
     * Multiplies this curve point by a scalar multiple using the formula described in the programming assignment.
     * Returns a new curve point.
     * @param s scalar value to multiply by
     * @return curve point after scalar multiplication
     */
    public E521CurvePoint scalarMultiply(BigInteger s) {
        if(s.equals(BigInteger.ZERO)) {
            return new E521CurvePoint();
        }
        E521CurvePoint v = new E521CurvePoint(x, y);
        for (int i = s.bitLength() - 2; i >= 0; i--) {
            v = v.add(v);
            if(s.testBit(i)) {
                v = v.add(this);
            }
        }
        return v;
    }

    /**
     * Returns if this curve point is equal to another curve point.
     * Two points are considered equal if both their x and y coordinates are the same.
     * @param oth curve point to check equality with
     * @return true if equal, false otherwise
     */
    public boolean equals(E521CurvePoint oth) {
        return (x.equals(oth.x) && y.equals(oth.y));
    }

    /**
     * Returns the x coordinate of this curve point.
     * @return x value
     */
    public BigInteger getX() {
        return x.add(BigInteger.ZERO);
    }

    /**
     * Returns the y coordinate of this curve point.
     * @return y value
     */
    public BigInteger getY() {
        return y.add(BigInteger.ZERO);
    }

    /**
     * Used to calculate an appropriate y value for the given x value where the least significant bit of the y value
     * matches yLsb.
     * @param x x coordinate of curve point
     * @param yLsb least significant bit of y coordinate of curve point
     * @return y value if it exists, null otherwise.
     */
    private static BigInteger calculateY(BigInteger x, boolean yLsb) {
        BigInteger radicand = BigInteger.ONE.subtract(x.modPow(BigInteger.TWO, p));
        radicand = radicand.multiply(BigInteger.ONE.subtract(d.multiply(x.modPow(BigInteger.TWO, p))).modInverse(p));
        return sqrt(radicand, yLsb);
    }

    /**
     * Compute a square root of v mod p with a specified least significant bit, if such a root exists.
     * Taken from the project assignment specifications.
     * @param   v   the radicand.
     * @param   lsb desired least significant bit (true: 1, false: 0).
     * @return  a square root r of v mod p with r mod 2 = 1 iff lsb = true if such a root exists, otherwise null.
     */
    private static BigInteger sqrt(BigInteger v, boolean lsb) {
        assert (E521CurvePoint.p.testBit(0) && E521CurvePoint.p.testBit(1)); // p = 3 (mod 4)
        if (v.signum() == 0) {
            return BigInteger.ZERO;
        }
        BigInteger r = v.modPow(E521CurvePoint.p.shiftRight(2).add(BigInteger.ONE), E521CurvePoint.p);
        if (r.testBit(0) != lsb) {
            r = E521CurvePoint.p.subtract(r); // correct the lsb

        }
        return (r.multiply(r).subtract(v).mod(E521CurvePoint.p).signum() == 0) ? r : null;
    }

    /**
     * Returns a string representation of the curve point in the form (x coordinate value, y coordinate value).
     * @return (x, y)
     */
    public String toString() {
        return "(" + x.toString() + ", " + y.toString() + ")";
    }

}
