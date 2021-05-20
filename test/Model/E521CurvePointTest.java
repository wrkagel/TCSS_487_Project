package Model;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class E521CurvePointTest {

    private final E521CurvePoint neutralPoint = new E521CurvePoint();

    @Test
    void zeroScalar() {
        assertTrue(neutralPoint.equals(E521CurvePoint.g.scalarMultiply(BigInteger.ZERO)));
    }

    @Test
    void oneScalar() {
        assertTrue(E521CurvePoint.g.equals(E521CurvePoint.g.scalarMultiply(BigInteger.ONE)));
    }

    @Test
    void addInverse() {
        E521CurvePoint negG = new E521CurvePoint(E521CurvePoint.g.getX().multiply(BigInteger.valueOf(-1L)),
                E521CurvePoint.g.getY());
        assertTrue(neutralPoint.equals(E521CurvePoint.g.add(negG)));
    }

    @Test
    void doublingVSadd() {
        assertTrue(E521CurvePoint.g.scalarMultiply(BigInteger.TWO).equals(
                E521CurvePoint.g.add(E521CurvePoint.g)));
    }

    @Test
    void quadruplingVSdoubledouble() {
        assertTrue(E521CurvePoint.g.scalarMultiply(BigInteger.valueOf(4L)).equals(
                E521CurvePoint.g.scalarMultiply(BigInteger.TWO).scalarMultiply(BigInteger.TWO)));
    }

    @Test
    void quadrupleNeutral() {
        assertTrue(neutralPoint.scalarMultiply(BigInteger.valueOf(4)).equals(neutralPoint));
    }

    @Test
    void quadrupleNonNeutral() {
        assertFalse(E521CurvePoint.g.scalarMultiply(BigInteger.valueOf(4)).equals(neutralPoint));
    }

    @Test
    void rScalar() {
        assertTrue(E521CurvePoint.g.scalarMultiply(E521CurvePoint.r).equals(neutralPoint));
    }

    @Test
    void modrScalar() {
        Random r = new Random();
        for(int i = 0; i < 100; i++) {
            long temp = r.nextLong();
            BigInteger k = new BigInteger(Long.toUnsignedString(temp));
            if (i % 2 == 0) k = k.add(E521CurvePoint.p);
            E521CurvePoint p1 = E521CurvePoint.g.scalarMultiply(k);
            E521CurvePoint p2 = E521CurvePoint.g.scalarMultiply(k.mod(E521CurvePoint.r));
            assertTrue(p1.equals(p2));
        }
    }

    @Test
    void plus1() {
        Random r = new Random();
        for(int i = 0; i < 100; i++) {
            long temp = r.nextLong();
            BigInteger k = new BigInteger(Long.toUnsignedString(temp));
            E521CurvePoint p1 = E521CurvePoint.g.scalarMultiply(k.add(BigInteger.ONE));
            E521CurvePoint p2 = E521CurvePoint.g.scalarMultiply(k).add(E521CurvePoint.g);
            assertTrue(p1.equals(p2));
        }
    }

    @Test
    void plust() {
        Random r = new Random();
        for(int i = 0; i < 100; i++) {
            long temp = r.nextLong();
            BigInteger k = new BigInteger(Long.toUnsignedString(temp));
            temp = r.nextLong();
            BigInteger t = new BigInteger(Long.toUnsignedString(temp));
            E521CurvePoint p1 = E521CurvePoint.g.scalarMultiply(k.add(t));
            E521CurvePoint p2 = E521CurvePoint.g.scalarMultiply(k).add(E521CurvePoint.g.scalarMultiply(t));
            assertTrue(p1.equals(p2));
        }
    }

    @Test
    void kTimesT() {
        Random r = new Random();
        byte[] a = new byte[64];
        byte[] b = new byte[65];
        for(int i = 0; i < 100; i++) {
            long temp = r.nextLong();
            BigInteger k = new BigInteger(Long.toUnsignedString(temp));
            temp = r.nextLong();
            BigInteger t = new BigInteger(Long.toUnsignedString(temp));
            if (i % 2 == 0) t = t.add(E521CurvePoint.p);
            E521CurvePoint p1 = E521CurvePoint.g.scalarMultiply(k).scalarMultiply(t);
            E521CurvePoint p2 = E521CurvePoint.g.scalarMultiply(t).scalarMultiply(k);
            E521CurvePoint p3 = E521CurvePoint.g.scalarMultiply(k.multiply(t).mod(E521CurvePoint.r));
            assertTrue(p1.equals(p2));
            assertTrue(p2.equals(p3));
        }
    }

}