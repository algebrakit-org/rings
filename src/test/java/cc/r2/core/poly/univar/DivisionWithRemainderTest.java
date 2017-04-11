package cc.r2.core.poly.univar;

import cc.r2.core.number.BigInteger;
import cc.r2.core.number.primes.BigPrimes;
import cc.r2.core.poly.AbstractPolynomialTest;
import cc.r2.core.test.Benchmark;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static cc.r2.core.poly.univar.DivisionWithRemainder.*;
import static cc.r2.core.poly.univar.RandomPolynomials.randomPoly;
import static org.junit.Assert.*;


/**
 * Created by poslavsky on 15/02/2017.
 */
public class DivisionWithRemainderTest extends AbstractPolynomialTest {
    @Test
    @SuppressWarnings("ConstantConditions")
    public void test1() throws Exception {
        long modulus = 11;
        lMutablePolynomialZp a = lMutablePolynomialZ.create(3480, 8088, 8742, 13810, 12402, 10418, 8966, 4450, 950).modulus(modulus);
        lMutablePolynomialZp b = lMutablePolynomialZ.create(2204, 2698, 3694, 3518, 5034, 5214, 5462, 4290, 1216).modulus(modulus);

        PolynomialGCD.PolynomialRemainders<lMutablePolynomialZp> prs = PolynomialGCD.Euclid(a, b);
        lMutablePolynomialZp gcd = prs.gcd();
        assertEquals(3, gcd.degree);
        assertTrue(DivisionWithRemainder.divideAndRemainder(a, gcd, true)[1].isZero());
        assertTrue(DivisionWithRemainder.divideAndRemainder(b, gcd, true)[1].isZero());
    }

    @Test(expected = ArithmeticException.class)
    public void test2() throws Exception {
        //test long overflow
        lMutablePolynomialZ dividend = lMutablePolynomialZ.create(28130, 95683, 93697, 176985, 135507, 101513, 75181, 17575, 0);
        lMutablePolynomialZ divider = lMutablePolynomialZ.create(24487310, 38204421, 12930314, 41553770, -1216266, 7382581, 15631547, 0, 0);
        pseudoDivideAndRemainder(dividend, divider, true);
    }

    @Test
    public void test3() throws Exception {
        lMutablePolynomialZ dividend = lMutablePolynomialZ.create(28130, 95683, 93697, 176985, 135507, 101513, 75181, 17575);
        lMutablePolynomialZ divider = lMutablePolynomialZ.one();
        lMutablePolynomialZ[] qr = divideAndRemainder(dividend, divider, true);
        assertEquals(dividend, qr[0]);
        assertTrue(qr[1].isZero());
    }

    @Test
    public void test4_ModularSmallPolynomialsRandom() throws Exception {
        int thr = 81;
        // polynomials
        RandomGenerator rnd = getRandom();
        lMutablePolynomialZp[] qd;
        for (int i = 0; i < its(1000, 10_000); i++) {
            lMutablePolynomialZ dividend = randomPoly(rnd.nextInt(thr), rnd);
            lMutablePolynomialZ divider = randomPoly(rnd.nextInt(thr), rnd);

            for (long prime : getModulusArray(9, 1, 40)) {
                if (dividend.lc() % prime == 0 || divider.lc() % prime == 0)
                    continue;

                lMutablePolynomialZp a = dividend.modulus(prime, true);
                lMutablePolynomialZp b = divider.modulus(prime, true);
                try {
                    qd = DivisionWithRemainder.divideAndRemainder(a, b, true);
                    assertQuotientRemainder(a, b, qd);
                    qd = DivisionWithRemainder.divideAndRemainder(a.clone(), b, false);

                    assertQuotientRemainder(a, b, qd);
                } catch (Exception err) {
                    System.out.println(dividend.toStringForCopy());
                    System.out.println(divider.toStringForCopy());
                    System.out.println(prime);
                    throw err;
                }
            }
        }
    }

    @Test
    public void test4a() throws Exception {
        long prime = 7;
        lMutablePolynomialZp dividend = lMutablePolynomialZ.create(95, 45, 67, 5, -2, 65, 24, 24, 60).modulus(prime);
        lMutablePolynomialZp divider = lMutablePolynomialZ.create(94, 86).modulus(prime);

        lMutablePolynomialZp[] qd = DivisionWithRemainder.divideAndRemainder(dividend.clone(), divider, false);
        assertQuotientRemainder(dividend, divider, qd);
    }

    @Test
    public void test5_SmallPolynomialsRandom() throws Exception {
        RandomGenerator rnd = getRandom();
        int passed = 0;
        int wins = 0;
        for (int i = 0; i < its(1000, 10_000); i++) {
            lMutablePolynomialZ dividend = randomPoly(15, 1000, rnd);
            lMutablePolynomialZ divider = randomPoly(10, 1000, rnd);
            double norm = -1;
            try {
                try {
                    lMutablePolynomialZ[] qr = pseudoDivideAndRemainder(dividend, divider, true);
                    assertPseudoQuotientRemainder(dividend, divider, qr);
                    qr = DivisionWithRemainder.pseudoDivideAndRemainder(dividend.clone(), divider, false);
                    assertPseudoQuotientRemainder(dividend, divider, qr);
                    norm = qr[0].norm2();
                    ++passed;
                } catch (ArithmeticException e) {}

                double normAdaptive = -1;
                try {
                    lMutablePolynomialZ[] qr = DivisionWithRemainder.pseudoDivideAndRemainderAdaptive(dividend, divider, true);
                    assertPseudoQuotientRemainder(dividend, divider, qr);
                    qr = DivisionWithRemainder.pseudoDivideAndRemainderAdaptive(dividend.clone(), divider, false);
                    assertPseudoQuotientRemainder(dividend, divider, qr);
                    normAdaptive = qr[0].norm2();
                    ++passed;
                } catch (ArithmeticException e) {}


                if (norm != -1) {
                    assertTrue(normAdaptive != -1);
                    assertTrue(normAdaptive <= norm);
                    if (normAdaptive < norm)
                        ++wins;
                }
            } catch (Exception|AssertionError e) {
                System.out.println(dividend.toStringForCopy());
                System.out.println(divider.toStringForCopy());
                throw e;
            }
        }
        System.out.println(passed);
        System.out.println(wins);
    }

    @Test
    public void test5_SmallPolynomialsRandom_a() throws Exception {
        lMutablePolynomialZ dividend = lMutablePolynomialZ.create(1, 4, -5, -2, 9, 4, -5, 7, 5, -5, 6, 3, 9, 8, 9, -8);
        lMutablePolynomialZ divider = lMutablePolynomialZ.create(7, 6, -1, 5, 0, 1, 0, 0, 8, 3, 7);
        lMutablePolynomialZ[] qr = pseudoDivideAndRemainder(dividend, divider, true);
        assertPseudoQuotientRemainder(dividend, divider, qr);
    }

    @Test
    public void test5_SmallPolynomialsRandom_b() throws Exception {
        lMutablePolynomialZ dividend = lMutablePolynomialZ.create(6, 9, 9, 3, 2, 6, -6, 7, -2, 8, 4, -8, 7, 3, 3, -6);
        lMutablePolynomialZ divider = lMutablePolynomialZ.create(0, 0, 0, 3, 1, 8, 8, -5, 6, -7, 9);
        lMutablePolynomialZ[] qr = pseudoDivideAndRemainder(dividend, divider, true);
        assertPseudoQuotientRemainder(dividend, divider, qr);
    }

    @Test
    public void test5_SmallPolynomialsRandom_с() throws Exception {
        lMutablePolynomialZ dividend = lMutablePolynomialZ.create(5, -6, 0, -9, 3, -1, -5, 8, 0, 1, -1, -8, 8, 2, 0, 2);
        lMutablePolynomialZ divider = lMutablePolynomialZ.create(1, 7, 6, -7, 9, 3, 3, 6, 3, -7, -3);
        lMutablePolynomialZ[] qr = pseudoDivideAndRemainder(dividend, divider, true);
        lMutablePolynomialZ d = divider.clone().multiply(qr[0]).add(qr[1]);
        assertPseudoQuotientRemainder(dividend, divider, qr);
    }

    @Test
    public void test6_ModularSmallPolynomialsRemainderRandom() throws Exception {
        RandomGenerator rnd = getRandom();
        for (int i = 0; i < its(1000, 10_000); i++) {
            lMutablePolynomialZ dividend = randomPoly(5 + rnd.nextInt(20), 100, rnd);
            lMutablePolynomialZ divider = randomPoly(rnd.nextInt(20), 100, rnd);
            if (dividend.degree < divider.degree) {
                lMutablePolynomialZ tmp = dividend;
                dividend = divider;
                divider = tmp;
            }
            for (long prime : getModulusArray(9, 1, 40)) {
                if (dividend.lc() % prime == 0 || divider.lc() % prime == 0)
                    continue;
                lMutablePolynomialZp a = dividend.clone().modulus(prime);
                lMutablePolynomialZp b = divider.clone().modulus(prime);
                lMutablePolynomialZp expected = DivisionWithRemainder.divideAndRemainder(a, b, true)[1];
                Assert.assertEquals(expected, DivisionWithRemainder.remainder(a, b, true));
                Assert.assertEquals(expected, DivisionWithRemainder.remainder(a.clone(), b, false));
            }
        }
    }

    @Test
    public void test7_LinearDividerRandom() throws Exception {
        RandomGenerator rnd = getRandom();
        RandomDataGenerator rndd = new RandomDataGenerator(rnd);
        DescriptiveStatistics
                fast = new DescriptiveStatistics(), fastPseudo = new DescriptiveStatistics(),
                gen = new DescriptiveStatistics(), genPseudo = new DescriptiveStatistics();

        long nIterations = its(1000, 15_000);
        out:
        for (int i = 0; i < nIterations; i++) {
            lMutablePolynomialZ dividend = randomPoly(rndd.nextInt(1, 10), 10, rnd);
            lMutablePolynomialZ divider;
            do {
                divider = lMutablePolynomialZ.create(rndd.nextInt(-10, 10), 1);
            } while (divider.degree == 0);

            if (i == nIterations / 10)
                Arrays.asList(fast, fastPseudo, gen, genPseudo).forEach(DescriptiveStatistics::clear);

            long start = System.nanoTime();
            lMutablePolynomialZ[] actual = DivisionWithRemainder.divideAndRemainderLinearDivider(dividend, divider, true);
            fast.addValue(System.nanoTime() - start);
            start = System.nanoTime();
            lMutablePolynomialZ[] expected = DivisionWithRemainder.divideAndRemainderGeneral0(dividend, divider, 1, true);
            gen.addValue(System.nanoTime() - start);
            assertArrayEquals(expected, actual);
            lMutablePolynomialZ[] expectedNoCopy = divideAndRemainderGeneral0(dividend.clone(), divider, 1, false);
            lMutablePolynomialZ[] actualNoCopy = divideAndRemainderLinearDivider(dividend.clone(), divider, false);
            assertArrayEquals(expected, actualNoCopy);
            assertArrayEquals(expected, expectedNoCopy);


            for (long modulus : getSmallModulusArray(10)) {
                do {
                    divider = lMutablePolynomialZ.create(rndd.nextLong(-10, 10), rndd.nextLong(-10, 10));
                } while (divider.degree == 0 || LongArithmetics.gcd(divider.lc(), modulus) != 1);

                lMutablePolynomialZp dividendMod = dividend.modulus(modulus), dividerMod = divider.modulus(modulus);
                if (dividendMod.degree == 0) {
                    --i;
                    continue out;
                }
                start = System.nanoTime();
                lMutablePolynomialZp[] actualMod = DivisionWithRemainder.divideAndRemainderLinearDividerModulus(dividendMod, dividerMod, true);
                fast.addValue(System.nanoTime() - start);
                start = System.nanoTime();
                lMutablePolynomialZp[] expectedMod = DivisionWithRemainder.divideAndRemainderClassic0(dividendMod, dividerMod, true);
                gen.addValue(System.nanoTime() - start);
                assertArrayEquals(expectedMod, actualMod);

                lMutablePolynomialZp[] actualNoCopyMod = DivisionWithRemainder.divideAndRemainderLinearDividerModulus(dividendMod.clone(), dividerMod, false);
                lMutablePolynomialZp[] expectedNoCopyMod = DivisionWithRemainder.divideAndRemainderClassic0(dividendMod.clone(), dividerMod, false);
                assertArrayEquals(expectedMod, actualNoCopyMod);
                assertArrayEquals(expectedMod, expectedNoCopyMod);
            }

            do {
                divider = lMutablePolynomialZ.create(rndd.nextLong(-10, 10), rndd.nextLong(-10, 10));
            } while (divider.degree == 0);
            start = System.nanoTime();
            actual = DivisionWithRemainder.pseudoDivideAndRemainderLinearDivider(dividend, divider, true);
            fastPseudo.addValue(System.nanoTime() - start);
            start = System.nanoTime();
            long factor = LongArithmetics.safePow(divider.lc(), dividend.degree - divider.degree + 1);
            expected = DivisionWithRemainder.divideAndRemainderGeneral0(dividend, divider, factor, true);
            genPseudo.addValue(System.nanoTime() - start);
            assertArrayEquals(expected, actual);

            actualNoCopy = pseudoDivideAndRemainderLinearDivider(dividend.clone(), divider, false);
            expectedNoCopy = divideAndRemainderGeneral0(dividend.clone(), divider, factor, false);
            assertArrayEquals(expected, actualNoCopy);
            assertArrayEquals(expected, expectedNoCopy);

            do {
                divider = lMutablePolynomialZ.create(rndd.nextLong(-10, 10), rndd.nextLong(-10, 10));
            } while (divider.degree == 0);
            start = System.nanoTime();
            actual = DivisionWithRemainder.pseudoDivideAndRemainderLinearDividerAdaptive(dividend, divider, true);
            fastPseudo.addValue(System.nanoTime() - start);
            start = System.nanoTime();
            expected = DivisionWithRemainder.pseudoDivideAndRemainderAdaptive0(dividend, divider, true);
            genPseudo.addValue(System.nanoTime() - start);
            assertArrayEquals(expected, actual);

            actualNoCopy = DivisionWithRemainder.pseudoDivideAndRemainderLinearDividerAdaptive(dividend.clone(), divider, false);
            expectedNoCopy = DivisionWithRemainder.pseudoDivideAndRemainderAdaptive0(dividend.clone(), divider, false);
            assertArrayEquals(expected, actualNoCopy);
            assertArrayEquals(expected, expectedNoCopy);
        }
        System.out.println("Fast:    " + fast.getMean());
        System.out.println("General: " + gen.getMean());

        System.out.println("       pseudo ");
        System.out.println("Fast:    " + fastPseudo.getMean());
        System.out.println("General: " + genPseudo.getMean());
    }


    @Test(expected = ArithmeticException.class)
    public void test8() throws Exception {
        lMutablePolynomialZ a = lMutablePolynomialZ.create(8, -2 * 8, 8, 8 * 2);
        lMutablePolynomialZ b = lMutablePolynomialZ.create(0);
        divideAndRemainder(a, b, false);
    }


    @Test
    public void test9() throws Exception {
        lMutablePolynomialZ a = lMutablePolynomialZ.create(8, -2 * 8, 8, 8 * 2);
        lMutablePolynomialZ b = lMutablePolynomialZ.create(0);
        lMutablePolynomialZ[] zeros = {lMutablePolynomialZ.zero(), lMutablePolynomialZ.zero()};
        assertArrayEquals(zeros, divideAndRemainder(b, a, true));
        assertArrayEquals(zeros, pseudoDivideAndRemainder(b, a, true));
        assertArrayEquals(zeros, DivisionWithRemainder.pseudoDivideAndRemainderAdaptive(b, a, true));
        assertArrayEquals(Arrays.stream(zeros).map(x -> x.modulus(13)).toArray(size -> new lMutablePolynomialZp[size]),
                DivisionWithRemainder.divideAndRemainder(b.modulus(13), a.modulus(13), true));
    }

    private static <T extends IMutablePolynomial<T>> void assertQuotientRemainder(T dividend, T divider, T[] qr) {
        if (qr == null) return;
        assertEquals(dividend, divider.clone().multiply(qr[0]).add(qr[1]));
    }

    private static void assertPseudoQuotientRemainder(lMutablePolynomialZ dividend, lMutablePolynomialZ divider, lMutablePolynomialZ[] qr) {
        if (qr == null) return;
        lMutablePolynomialZ d = divider.clone().multiply(qr[0]).add(qr[1]);
        lMutablePolynomialZ[] factor = divideAndRemainder(d, dividend, true);
        assertNotNull(factor);
        assertTrue(factor[1].isZero());
        assertTrue(factor[0].isConstant());
    }

    private static lMutablePolynomialZp inverseModMonomial0(lMutablePolynomialZp poly, int xDegree) {
        if (xDegree < 1)
            return null;
        if (poly.cc() != 1)
            throw new IllegalArgumentException();
        int r = DivisionWithRemainder.log2(xDegree);
        lMutablePolynomialZp gPrev = poly.createOne();
        for (int i = 0; i < r; ++i) {
            lMutablePolynomialZp tmp = gPrev.clone().multiply(2).subtract(gPrev.square().multiply(poly));
            gPrev = DivisionWithRemainder.remainderMonomial(tmp, 1 << i, false);
        }
        return gPrev;
    }

    @Test
    public void test10_InverseModRandom() throws Exception {
        RandomGenerator rnd = getRandom();
        long modulus = getModulusRandom(10);
        for (int i = 0; i < its(100, 1000); i++) {
            lMutablePolynomialZp f = RandomPolynomials.randomMonicPoly(1 + rnd.nextInt(100), modulus, rnd);
            f.data[0] = 1;
            int modDegree = 1 + rnd.nextInt(2 * f.degree);
            lMutablePolynomialZp invMod = inverseModMonomial0(f, modDegree);
            assertInverseModMonomial(f, invMod, modDegree);
        }
    }

    static void assertInverseModMonomial(lMutablePolynomialZp poly, lMutablePolynomialZp invMod, int monomialDegree) {
        assertTrue(PolynomialArithmetics.polyMultiplyMod(poly, invMod, lMutablePolynomialZp.createMonomial(poly.modulus, 1, monomialDegree), true).isOne());
    }

    @Test
    public void test11_InverseModStructureRandom() throws Exception {
        RandomGenerator rnd = getRandom();
        for (int i = 0; i < its(100, 1000); i++) {
            long modulus = getModulusRandom(20);
            lMutablePolynomialZp p = RandomPolynomials.randomMonicPoly(2 + rnd.nextInt(100), modulus, rnd);
            p.data[0] = 1;

            DivisionWithRemainder.InverseModMonomial invMod = DivisionWithRemainder.fastDivisionPreConditioning(p);
            for (int j = 0; j < 30; j++) {
                int xDegree = 1 + rnd.nextInt(1025);
                assertEquals(invMod.getInverse(xDegree), inverseModMonomial0(p.clone().reverse(), xDegree));
            }
        }
    }

    @Test
    public void test12_FastDivisionRandom() throws Exception {
        RandomGenerator rnd = getRandom();
        for (int i = 0; i < its(100, 500); i++) {
            long modulus = getModulusRandom(getRandomData().nextInt(30, 33));
            lMutablePolynomialZp b = RandomPolynomials.randomMonicPoly(30, modulus, rnd);
            lMutablePolynomialZp a = RandomPolynomials.randomMonicPoly(rnd.nextInt(30), modulus, rnd);

            DivisionWithRemainder.InverseModMonomial invMod = DivisionWithRemainder.fastDivisionPreConditioning(b);
            lMutablePolynomialZp[] fast = DivisionWithRemainder.divideAndRemainderFast(a, b, invMod, true);
            lMutablePolynomialZp[] plain = DivisionWithRemainder.divideAndRemainderClassic(a, b, true);
            assertArrayEquals(fast, plain);
        }
    }

    @Test
    public void test13() throws Exception {
        long modulus = 7;
        lMutablePolynomialZp a = lMutablePolynomialZ.create(5, 1, 4, 6, 4, 3, 5, 5, 3, 4, 2, 2, 5, 2, 5, 6, 1, 1, 2, 5, 1, 0, 0, 6, 6, 5, 5, 1, 0, 1, 4, 1, 1).modulus(modulus);
        lMutablePolynomialZp b = lMutablePolynomialZ.create(2, 5, 3, 1, 1, 5, 6, 3, 4, 0, 0, 5, 4, 0, 2, 1).modulus(modulus);
        DivisionWithRemainder.InverseModMonomial invMod = DivisionWithRemainder.fastDivisionPreConditioning(b);
        lMutablePolynomialZp[] fast = DivisionWithRemainder.divideAndRemainderFast(a, b, invMod, true);
        lMutablePolynomialZp[] plain = DivisionWithRemainder.divideAndRemainderClassic(a, b, true);
        assertArrayEquals(fast, plain);
    }

    @Test
    public void test14() throws Exception {
        long modulus = 7;
        lMutablePolynomialZp a = lMutablePolynomialZ.create(5, 3, 3, 3, 5, 3, 1, 4, -3, 1, 4, 5, 0, 2, 2, -5, 1).modulus(modulus);
        lMutablePolynomialZp b = lMutablePolynomialZ.create(0, 4, 6, 1, 2, 4, 0, 0, 6, 5, 2, 3, 1, 4, 0, 1).modulus(modulus);
        DivisionWithRemainder.InverseModMonomial invMod = DivisionWithRemainder.fastDivisionPreConditioning(b);
        lMutablePolynomialZp[] fast = DivisionWithRemainder.divideAndRemainderFast(a, b, invMod, true);
        lMutablePolynomialZp[] plain = DivisionWithRemainder.divideAndRemainderClassic(a, b, true);
        assertArrayEquals(fast, plain);
    }

    @Test
    public void test15() throws Exception {
        long modulus = 17;
        lMutablePolynomialZp a = lMutablePolynomialZ.create(0, 6, 2, 1, 10, 15, 16, 15, 2, 11, 13, 0, 1, 15, 5, 13, 8, 14, 13, 14, 15, 1, 1).modulus(modulus);
        lMutablePolynomialZp b = lMutablePolynomialZ.create(7, 12, 12, 12, 13, 2, 7, 10, 7, 15, 13, 1, 10, 16, 6, 1).modulus(modulus);
        DivisionWithRemainder.InverseModMonomial invMod = DivisionWithRemainder.fastDivisionPreConditioning(b);
        lMutablePolynomialZp[] fast = DivisionWithRemainder.divideAndRemainderFast(a, b, invMod, true);
        lMutablePolynomialZp[] plain = DivisionWithRemainder.divideAndRemainderClassic(a, b, true);
        assertArrayEquals(fast, plain);
    }

    @Test
    public void test16() throws Exception {
        long modulus = 17;
        lMutablePolynomialZp a = lMutablePolynomialZ.create(5, 9, 4, 9, 8, 12, 11, 9, 1, 6, 15, 7, 11, 2, 11, 13, 11, 10, 5, 1).modulus(modulus);
        lMutablePolynomialZp b = lMutablePolynomialZ.create(11, 15, 9, 5, 11, 5, 14, 9, 1, 0, 16, 12, 11, 5, 15, 10, 15, 2, 14, 3, 1, 16, 16, 12, 13, 1, 12, 11, 1, 15, 1).modulus(modulus);
        DivisionWithRemainder.InverseModMonomial invMod = DivisionWithRemainder.fastDivisionPreConditioning(b);
        lMutablePolynomialZp[] fast = DivisionWithRemainder.divideAndRemainderFast(a, b, invMod, true);
        lMutablePolynomialZp[] plain = DivisionWithRemainder.divideAndRemainderClassic(a, b, true);
        assertArrayEquals(fast, plain);
    }

    @Test
    public void test17() throws Exception {
        long modulus = 7;
        lMutablePolynomialZp f = lMutablePolynomialZ.create(0, 2, 3, 4, -5, 1).modulus(modulus).reverse();
        int modDegree = f.degree;
        lMutablePolynomialZp invmod = inverseModMonomial0(f, modDegree);
        lMutablePolynomialZp r = PolynomialArithmetics.polyMultiplyMod(f, invmod, lMutablePolynomialZp.createMonomial(modulus, 1, modDegree), true);
        assertTrue(r.isOne());
    }

    @Test
    public void test18() throws Exception {
        long modulus = 17;
        lMutablePolynomialZp f = lMutablePolynomialZ.create(7, 12, 12, 12, 13, 2, 7, 10, 7, 15, 13, 1, 10, 16, 6, 1).modulus(modulus).reverse();
        int modDegree = 9;
        lMutablePolynomialZp invmod = inverseModMonomial0(f, modDegree);
        lMutablePolynomialZp r = PolynomialArithmetics.polyMultiplyMod(f, invmod, lMutablePolynomialZp.createMonomial(modulus, 1, modDegree), true);
        assertTrue(r.isOne());
    }

    @Test
    @Benchmark(runAnyway = true)
    public void test19_FastDivisionPerformance() throws Exception {
        long modulus = 5659;
        RandomGenerator rnd = getRandom();
        lMutablePolynomialZp divider = RandomPolynomials.randomMonicPoly(118, modulus, rnd);

        DescriptiveStatistics classic = new DescriptiveStatistics(), fast = new DescriptiveStatistics();
        DivisionWithRemainder.InverseModMonomial invRev = DivisionWithRemainder.fastDivisionPreConditioning(divider);
        long nIterations = its(1000, 15000);
        for (int i = 0; i < nIterations; i++) {
            if (i * 10 == nIterations) {
                classic.clear();
                fast.clear();
            }
            lMutablePolynomialZ dividendZ = RandomPolynomials.randomPoly(3 * divider.degree / 2, (int) modulus, rnd);
            lMutablePolynomialZp dividend = dividendZ.modulus(modulus);

            long start = System.nanoTime();
            lMutablePolynomialZp[] qdPlain = DivisionWithRemainder.divideAndRemainderClassic(dividend, divider, true);
            long plain = System.nanoTime() - start;
            classic.addValue(plain);

            start = System.nanoTime();
            lMutablePolynomialZp[] qdNewton = DivisionWithRemainder.divideAndRemainderFast(dividend, divider, invRev, true);
            long newton = System.nanoTime() - start;
            fast.addValue(newton);

            assertArrayEquals(qdPlain, qdNewton);
        }

        System.out.println("==== Plain ====");
        System.out.println(classic.getPercentile(50));

        System.out.println("==== Fast ====");
        System.out.println(fast.getPercentile(50));
    }

    @Test
    @Benchmark(runAnyway = true)
    public void test20_FastDivisionPerformance() throws Exception {
        long modulus = BigPrimes.nextPrime(124987324L);
        RandomGenerator rnd = getRandom();

        DescriptiveStatistics classic = new DescriptiveStatistics(), fast = new DescriptiveStatistics();
        long nIterations = its(1000, 15000);
        int dividerDegree = 156;
        int dividendDegree = 256;
        for (int i = 0; i < nIterations; i++) {
            if (i * 10 == nIterations) {
                classic.clear();
                fast.clear();
            }

            lMutablePolynomialZp divider = RandomPolynomials.randomMonicPoly(dividerDegree, modulus, rnd);
            lMutablePolynomialZ dividendZ = RandomPolynomials.randomPoly(dividendDegree, (int) modulus, rnd);
            lMutablePolynomialZp dividend = dividendZ.modulus(modulus);
            divider.multiply(3);

            long start = System.nanoTime();
            lMutablePolynomialZp[] qdPlain = DivisionWithRemainder.divideAndRemainderClassic(dividend, divider, true);
            long plain = System.nanoTime() - start;
            classic.addValue(plain);

            start = System.nanoTime();
            lMutablePolynomialZp[] qdNewton = DivisionWithRemainder.divideAndRemainderFast(dividend, divider, true);
            long newton = System.nanoTime() - start;
            fast.addValue(newton);

            assertArrayEquals(qdPlain, qdNewton);
        }


        System.out.println("==== Plain ====");
        System.out.println(classic.getMean());

        System.out.println("==== Fast ====");
        System.out.println(fast.getMean());
    }

    @Test
    public void test21() throws Exception {
        long modulus = 17;
        lMutablePolynomialZp a = lMutablePolynomialZ.create(5, 9, 4, 9, 8, 12, 11, 9, 1, 6, 15, 7, 11, 2, 11, 13, 11, 10, 5, 1).modulus(modulus);
        lMutablePolynomialZp b = lMutablePolynomialZ.create(11, 15, 9, 5, 11, 5, 14, 9, 1, 0, 16, 12, 11, 5, 15, 10, 15, 2, 14, 3, 1, 16, 16, 12, 13, 1, 12, 11, 1, 15, 13).modulus(modulus);
        lMutablePolynomialZp[] fast = DivisionWithRemainder.divideAndRemainderFast(a, b, true);
        lMutablePolynomialZp[] plain = DivisionWithRemainder.divideAndRemainderClassic(a, b, true);
        assertArrayEquals(fast, plain);
    }

    @Test
    public void test22() throws Exception {
        bMutablePolynomialZp bDividend = bMutablePolynomialZ.create(1, 2, 3, 4, 5, 6, 5, 4, 3, 2, 1).modulus(BigInteger.valueOf(7));
        lMutablePolynomialZp lDividend = bDividend.toLong();


        bMutablePolynomialZp bDivider = bMutablePolynomialZ.create(1, 2, 3, 3, 2, 1).modulus(BigInteger.valueOf(7));
        lMutablePolynomialZp lDivider = bDivider.toLong();

        bMutablePolynomialZp[] bqd = DivisionWithRemainder.divideAndRemainderFast(bDividend, bDivider, true);
        lMutablePolynomialZp[] lqd = DivisionWithRemainder.divideAndRemainderFast(lDividend, lDivider, true);
        Assert.assertArrayEquals(new lMutablePolynomialZp[]{bqd[0].toLong(), bqd[1].toLong()}, lqd);
    }


    @Test
    @Benchmark(runAnyway = true)
    public void test19_BigInteger_FastDivisionPerformance() throws Exception {
        BigInteger modulus = new BigInteger("1247842098624308285367648396697");//BigPrimes.nextPrime(new BigInteger(100, rnd));
        RandomGenerator rnd = getRandom();
        bMutablePolynomialZp divider = RandomPolynomials.randomMonicPoly(128, modulus, rnd);

        DescriptiveStatistics classic = new DescriptiveStatistics(), fast = new DescriptiveStatistics();
        DivisionWithRemainder.InverseModMonomial<bMutablePolynomialZp> invRev = DivisionWithRemainder.fastDivisionPreConditioning(divider);
        long nIterations = its(1000, 5000);
        for (int i = 0; i < nIterations; i++) {
            if (i == nIterations / 10) {
                classic.clear();
                fast.clear();
            }
            bMutablePolynomialZ dividendZ = RandomPolynomials.randomPoly(3 * divider.degree / 2, modulus, rnd);
            bMutablePolynomialZp dividend = dividendZ.modulus(modulus);

            long start = System.nanoTime();
            bMutablePolynomialZp[] qdPlain = DivisionWithRemainder.divideAndRemainderClassic(dividend, divider, true);
            long plain = System.nanoTime() - start;
            classic.addValue(plain);

            start = System.nanoTime();
            bMutablePolynomialZp[] qdNewton = DivisionWithRemainder.divideAndRemainderFast(dividend, divider, invRev, true);
            long newton = System.nanoTime() - start;
            fast.addValue(newton);

            assertArrayEquals(qdPlain, qdNewton);
        }

        System.out.println("==== Plain ====");
        System.out.println(classic.getPercentile(50));

        System.out.println("==== Fast ====");
        System.out.println(fast.getPercentile(50));
    }


    @Test
    @Benchmark(runAnyway = true)
    public void test20_BigInteger_FastDivisionPerformance() throws Exception {
        RandomGenerator rnd = getRandom();
        BigInteger modulus = new BigInteger("1247842098624308285367648396697");//BigPrimes.nextPrime(new BigInteger(100, rnd));

        DescriptiveStatistics classic = new DescriptiveStatistics(), fast = new DescriptiveStatistics();
        long nIterations = its(1000, 5000);
        int dividerDegree = 126;
        int dividendDegree = 256;
        for (int i = 0; i < nIterations; i++) {
            if (nIterations / 10 == i) {
                classic.clear();
                fast.clear();
            }

            bMutablePolynomialZp divider = RandomPolynomials.randomMonicPoly(dividerDegree, modulus, rnd);
            bMutablePolynomialZ dividendZ = RandomPolynomials.randomPoly(dividendDegree, modulus, rnd);
            bMutablePolynomialZp dividend = dividendZ.modulus(modulus);
            divider.multiply(BigInteger.THREE);

            long start = System.nanoTime();
            bMutablePolynomialZp[] qdPlain = DivisionWithRemainder.divideAndRemainderClassic(dividend, divider, true);
            long plain = System.nanoTime() - start;
            classic.addValue(plain);

            start = System.nanoTime();
            bMutablePolynomialZp[] qdNewton = DivisionWithRemainder.divideAndRemainderFast(dividend, divider, true);
            long newton = System.nanoTime() - start;
            fast.addValue(newton);

            assertArrayEquals("dividend = " + dividend + ";\ndivider = " + divider + ";\n", qdPlain, qdNewton);
        }


        System.out.println("==== Plain ====");
        System.out.println(classic.getMean());

        System.out.println("==== Fast ====");
        System.out.println(fast.getMean());
    }

    @Test
    public void test23() throws Exception {
        BigInteger modulus = new BigInteger("998427238390739620139");
        bMutablePolynomialZp dividend = bMutablePolynomialZ.parse("989441076315244786644+174683251098354358x^1+2939699558711223765x^2+993164729241539182424x^3+8652504087827847685x^4+2978039521215483585x^5+5687372540827878771x^6+3684693598277313443x^7+3034113231916032517x^8+1842720927561159970x^9+1401489172494884190x^10").modulus(modulus);
        bMutablePolynomialZp divider = bMutablePolynomialZ.parse("718119058879299323824+59748620370951943044x^1+27715597040703811206x^2+3x^3").modulus(modulus);

        bMutablePolynomialZp[] classic = DivisionWithRemainder.divideAndRemainderClassic(dividend, divider, true);
        bMutablePolynomialZp[] fast = DivisionWithRemainder.divideAndRemainderFast(dividend, divider, true);

        System.out.println(Arrays.toString(classic));
        System.out.println(Arrays.toString(fast));
        assertQuotientRemainder(dividend, divider, classic);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideByZero() throws Exception {
        lMutablePolynomialZp poly = lMutablePolynomialZ.create(1, 2, 3, 4).modulus(3);
        System.out.println(Arrays.toString(divideAndRemainder(poly, poly.createZero(), false)));
    }
}