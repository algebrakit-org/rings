package cc.r2.core.poly.multivar;

import cc.r2.core.number.BigInteger;
import cc.r2.core.number.primes.BigPrimes;
import cc.r2.core.number.primes.SmallPrimes;
import cc.r2.core.poly.AbstractPolynomialTest;
import cc.r2.core.poly.Domain;
import cc.r2.core.poly.IntegersModulo;
import cc.r2.core.util.RandomDataGenerator;
import cc.r2.core.util.TimeUnits;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static cc.r2.core.poly.Integers.Integers;
import static cc.r2.core.poly.multivar.DegreeVector.LEX;
import static cc.r2.core.poly.multivar.MultivariateGCD.*;
import static cc.r2.core.poly.multivar.MultivariatePolynomial.*;
import static cc.r2.core.poly.multivar.MultivariateReduction.dividesQ;
import static cc.r2.core.poly.multivar.RandomMultivariatePolynomial.randomPolynomial;
import static org.junit.Assert.*;

/**
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class MultivariateGCDTest extends AbstractPolynomialTest {


    private static void assertBrownGCD(MultivariatePolynomial<BigInteger> gcd,
                                       MultivariatePolynomial<BigInteger> a,
                                       MultivariatePolynomial<BigInteger> b) {
        MultivariatePolynomial<BigInteger> actualGCD = BrownGCD(a, b);
        lMultivariatePolynomial lActualGCD = BrownGCD(asLongPolyZp(a), asLongPolyZp(b));
        Assert.assertTrue(dividesQ(actualGCD, gcd));
        Assert.assertEquals(asLongPolyZp(actualGCD).monic(), lActualGCD.monic());
    }

    @Test
    public void testBrown1() throws Exception {
        IntegersModulo domain = new IntegersModulo(BigPrimes.nextPrime(1321323));
        MultivariatePolynomial<BigInteger>
                a = parse("c*b*a^2+b^2 + c", domain, LEX),
                b = parse("a^2+2*b^2 + 2*c", domain, LEX),
                gcd = parse("c*a+b+a+ c*a^3", domain, LEX);
        a = a.multiply(gcd);
        b = b.multiply(gcd);
        assertBrownGCD(gcd, a, b);
    }

    @Test
    public void testBrown2() throws Exception {
        IntegersModulo domain = new IntegersModulo(BigPrimes.nextPrime(1321323));
        MultivariatePolynomial<BigInteger>
                a = parse("c*b*a^2+b^2 + c", domain, LEX),
                b = parse("a^2+2*b^2 + 2*c", domain, LEX),
                gcd = parse("c*a*b+b*b+a*b+ c*a^3*b", domain, LEX);
        a = a.multiply(gcd);
        b = b.multiply(gcd);
        assertBrownGCD(gcd, a, b);
    }

    @Test
    public void testBrown3() throws Exception {
        IntegersModulo domain = new IntegersModulo(BigPrimes.nextPrime(659));
        MultivariatePolynomial<BigInteger>
                a = parse("656*c^2+7*b^3*c+656*a*b^2+2*a^3*c+5*a^3*b^3", domain, LEX),
                b = parse("654+654*a*b^2*c^2+a*b^3*c^2+652*a^2*b*c^2+656*a^2*b^2*c", domain, LEX),
                gcd = parse("7*b+655*a*b^3*c^2+6*a^2*b^3*c^2", domain, LEX);
        a = a.multiply(gcd);
        b = b.multiply(gcd);
        assertBrownGCD(gcd, a, b);
    }

    @Test
    public void testBrown3a() throws Exception {
        IntegersModulo domain = new IntegersModulo(17);
        MultivariatePolynomial<BigInteger>
                a = parse("656*c^2+7*b^3*c+656*a*b^2+2*a^3*c+5*a^3*b^3", domain, LEX),
                b = parse("654+654*a*b^2*c^2+a*b^3*c^2+652*a^2*b*c^2+656*a^2*b^2*c", domain, LEX),
                gcd = parse("7*b^6+655*a*b^3*c^6+6*a^2*b^3*c^4", domain, LEX);
        a = a.multiply(gcd);
        b = b.multiply(gcd);
        assertBrownGCD(gcd, a, b);
    }

    @Test
    public void testBrown4() throws Exception {
        IntegersModulo domain = new IntegersModulo(653);
        String[] vars = {"a", "b", "c"};
        MultivariatePolynomial<BigInteger>
                a = parse("6*b^5*c+a*b^3+a^2*b^2+a^2*b^2*c+a^3*b*c^3", domain, LEX, vars),
                b = parse("9*a*b^2*c^6+a*b^4*c^6+a^2*b^2*c^3+a^5*b^2+a^5*b^6*c^4+a^6*c+a^6*b^2*c", domain, LEX, vars),
                gcd = parse("653*b^3*c^4+b^4+b^5*c^3+a^2*b*c^2+a^4*b^2*c^4", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);
        assertBrownGCD(gcd, a, b);
    }

    @Test
    public void testBrown5() throws Exception {
        RandomGenerator rnd = PrivateRandom.getRandom();
        rnd.setSeed(28);
        IntegersModulo domain = new IntegersModulo(653);
        String[] vars = {"a", "b", "c"};
        MultivariatePolynomial<BigInteger>
                a = parse("561*a^2*c^2+a^2*b^2*c+a^3*b+a^4*b^2+a^4*b^5*c^3+a^5*b", domain, LEX, vars),
                b = parse("561*a*c^3+a*b^4*c^5+a^2*c^2+a^2*b^6*c^3+a^3*b^6*c^5+a^5*b^5*c^3+a^5*b^5*c^6", domain, LEX, vars),
                gcd = parse("4*c^2+b^4+a^2*b^4*c+a^3*b^2*c+a^3*b^6+a^5*b^2*c^6+a^6*b^5", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);
        assertBrownGCD(gcd, a, b);
    }

    @Test
    public void testBrown6() throws Exception {
        PrivateRandom.getRandom().setSeed(1564);
        IntegersModulo domain = new IntegersModulo(937);
        String[] vars = {"a", "b", "c"};
        MultivariatePolynomial<BigInteger>
                a = parse("931*a^3*b^4*c+a^4+a^4*b^6*c^2+a^5*b*c^3+a^6*b*c^2", domain, LEX, vars),
                b = parse("932*b*c+a*b^6*c^2+a^3*b*c^2+a^3*b^3*c^5+a^3*b^5*c+a^5*b^5*c^3+a^6*b^2*c^6+a^6*b^4*c^5+a^6*b^6", domain, LEX, vars),
                gcd = parse("935*c^2+c^4+a^3*b*c^5+a^3*b^2*c^3+a^4*b^3", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);
        assertBrownGCD(gcd, a, b);
    }

    @Test
    public void testBrown7() throws Exception {
        PrivateRandom.getRandom().setSeed(2369);
        IntegersModulo domain = new IntegersModulo(569);
        String[] vars = {"a", "b", "c"};
        MultivariatePolynomial<BigInteger>
                a = parse("563*b^2*c+6*b^4*c^3+4*a*b^4*c^3+563*a*b^4*c^4+560*a^2*b^5*c^2+9*a^3*b^4*c+5*a^4*c^2+7*a^4*b^3*c^5+4*a^5*b^4*c^5+6*a^5*b^5", domain, LEX, vars),
                b = parse("4*b^2*c+5*b^2*c^3+5*b^3*c+3*a^2*b+3*a^2*b*c^2+565*a^3*b*c^2", domain, LEX, vars),
                gcd = parse("4+8*b^2*c^3+4*b^3+8*b^3*c+7*a*c+a*b*c+7*a^2*b^2+2*a^2*b^2*c^2+5*a^3*c^2+5*a^3*c^3", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);
        assertBrownGCD(gcd, a, b);
    }

    @Test
    public void testBrown_random1() throws Exception {
        RandomGenerator rnd = getRandom();
        rnd.setSeed(123);

        int nVarsMin = 3, nVarsMax = 3;
        int minDegree = 3, maxDegree = 5;
        int minSize = 5, maxSize = 10;

        int nIterations = its(1000, 10000);
        TripletPort sampleData = new TripletPort(nVarsMin, nVarsMax, minDegree, maxDegree, minSize, maxSize, rnd);
        for (int n = 0; n < nIterations; n++) {
            PrivateRandom.getRandom().setSeed(n);
            MultivariatePolynomial<BigInteger> gcdActual = null;
            GCDTriplet data = sampleData.nextSample(false, false);
            checkConsistency(data.a, data.b, data.gcd, data.aGCD, data.bGCD);
            try {
                PrivateRandom.getRandom().setSeed(n);
                gcdActual = BrownGCD(data.aGCD, data.bGCD);
                checkConsistency(gcdActual);
                assertTrue(dividesQ(gcdActual, data.gcd));

                PrivateRandom.getRandom().setSeed(n);
                assertTrue(dividesQ(BrownGCD(asLongPolyZp(data.aGCD), asLongPolyZp(data.bGCD)), asLongPolyZp(data.gcd)));
            } catch (Throwable err) {
                System.out.println("seed: " + n);
                System.out.println("modulus: " + data.domain);
                System.out.println("a: " + data.a);
                System.out.println("b: " + data.b);
                System.out.println("aGCD: " + data.aGCD);
                System.out.println("bGCD: " + data.bGCD);
                System.out.println("expected gcd: " + data.gcd);
                System.out.println("actual gcd  : " + gcdActual);
                throw err;
            }
        }
    }

    @Test
    public void testBrown_random2() throws Exception {
        RandomGenerator rnd = getRandom();
        rnd.setSeed(123);

        int nVarsMin = 3, nVarsMax = 3;
        int minDegree = 3, maxDegree = 5;
        int minSize = 5, maxSize = 10;

        int nIterations = its(1000, 10000);
        TripletPort sampleData = new TripletPort(nVarsMin, nVarsMax, minDegree, maxDegree, minSize, maxSize, rnd);
        for (int n = 0; n < nIterations; n++) {
            PrivateRandom.getRandom().setSeed(n);
            MultivariatePolynomial<BigInteger> gcdActual = null;
            GCDTriplet data = sampleData.nextSample(false, false);

            checkConsistency(data.a, data.b);
            try {
                PrivateRandom.getRandom().setSeed(n);
                gcdActual = BrownGCD(data.a, data.b);
                checkConsistency(gcdActual);
                assertTrue(dividesQ(data.a, gcdActual));
                assertTrue(dividesQ(data.b, gcdActual));

                PrivateRandom.getRandom().setSeed(n);
                lMultivariatePolynomial lGcdActual = BrownGCD(asLongPolyZp(data.a), asLongPolyZp(data.b));
                assertTrue(dividesQ(asLongPolyZp(data.a), lGcdActual));
                assertTrue(dividesQ(asLongPolyZp(data.b), lGcdActual));
            } catch (Throwable err) {
                System.out.println("seed: " + n);
                System.out.println("modulus: " + data.domain);
                System.out.println("a: " + data.a);
                System.out.println("b: " + data.b);
                System.out.println("actual gcd  : " + gcdActual);
                throw err;
            }
        }
    }

    @Test
    public void testBrown_random3() throws Exception {
        RandomGenerator rnd = getRandom();
        rnd.setSeed(123);

        int nVarsMin = 100, nVarsMax = 100, nVars = 3;
        int minDegree = 3, maxDegree = 5;
        int minSize = 5, maxSize = 10;

        int nIterations = its(1000, 10000);
        TripletPort sampleData = new TripletPort(nVarsMin, nVarsMax, minDegree, maxDegree, minSize, maxSize, rnd);
        for (int n = 0; n < nIterations; n++) {
            PrivateRandom.getRandom().setSeed(n);
            MultivariatePolynomial<BigInteger> gcdActual = null;
            GCDTriplet data = sampleData.nextSample(false, false);

            MultivariatePolynomial<BigInteger> aGCD = data.a;
            MultivariatePolynomial<BigInteger> bGCD = data.b;
            MultivariatePolynomial<BigInteger> gcd = data.gcd;

            do {
                gcd = gcd.evaluate(rnd.nextInt(gcd.nVariables), data.domain.randomElement(rnd));
            } while (gcd.nUsedVariables() > nVars);

            int[] gcdDegrees = gcd.degrees();
            for (int i = 0; i < gcdDegrees.length; i++) {
                if (gcdDegrees[i] == 0) {
                    if (rnd.nextBoolean())
                        aGCD = aGCD.evaluate(i, data.domain.randomElement(rnd));
                    else
                        bGCD = bGCD.evaluate(i, data.domain.randomElement(rnd));
                }
            }

            aGCD = aGCD.multiply(gcd);
            bGCD = bGCD.multiply(gcd);

            checkConsistency(data.a, data.b, gcd, aGCD, bGCD);
            try {
                PrivateRandom.getRandom().setSeed(n);
                gcdActual = BrownGCD(aGCD, bGCD);
                checkConsistency(gcdActual);
                assertTrue(dividesQ(gcdActual, gcd));

                PrivateRandom.getRandom().setSeed(n);
                lMultivariatePolynomial lGcdActual = BrownGCD(asLongPolyZp(aGCD), asLongPolyZp(bGCD));
                assertTrue(dividesQ(lGcdActual, asLongPolyZp(gcd)));
            } catch (Throwable err) {
                System.out.println("seed: " + n);
                System.out.println("modulus: " + data.domain);
                System.out.println("a: " + data.a);
                System.out.println("b: " + data.b);
                System.out.println("aGCD: " + aGCD);
                System.out.println("bGCD: " + bGCD);
                System.out.println("expected gcd: " + gcd);
                System.out.println("actual gcd  : " + gcdActual);
                throw err;
            }
        }
    }

    @Test
    public void testBrown8() throws Exception {
        PrivateRandom.getRandom().setSeed(2369);
        IntegersModulo domain = new IntegersModulo(569);
        String[] vars = {"a", "b", "c"};
        MultivariatePolynomial<BigInteger>
                a = parse("a^4 + c^4", domain, LEX, vars),
                b = parse("4*b^2*c+5*b^2*c^3+5*b^3*c+3*a^2*b+3*a^2*b*c^2+565*a^3*b*c^2", domain, LEX, vars),
                gcd = parse("a^2 + c^2", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);
        assertBrownGCD(gcd, a, b);
    }

    @Test
    public void testBrown9() throws Exception {
        PrivateRandom.getRandom().setSeed(2369);
        IntegersModulo domain = new IntegersModulo(569);
        String[] vars = {"a", "b", "c", "d"};
        MultivariatePolynomial<BigInteger>
                a = parse("a^4 + c^4", domain, LEX, vars),
                b = parse("b^2 + d^2", domain, LEX, vars),
                gcd = parse("d^2", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);
        assertBrownGCD(gcd, a, b);
    }

    @Test
    public void testBrown10() throws Exception {
        IntegersModulo domain = new IntegersModulo(5642359);
        String[] vars = {"a", "b"};
        MultivariatePolynomial<BigInteger>
                a = parse("1199884 + 4783764*b + b^2 + 3215597*a*b + 2196297*a*b^2 + 4781733*a^4 + a^4*b + 2196297*a^5*b", domain, LEX, vars),
                b = parse("4645946 + 3921107*b + b^2 + 5605437*a*b + 2196297*a*b^2 + 4781733*a^3 + a^3*b + 2196297*a^4*b", domain, LEX, vars);

        MultivariatePolynomial<BigInteger> gcdActual = BrownGCD(a, b);
        gcdActual = gcdActual.monic().multiply(domain.valueOf(4781733));
        MultivariatePolynomial<BigInteger> expected = parse("1574588 + 4559668*b + 4781733*a*b", domain, LEX, vars);
        assertEquals(expected, gcdActual);
    }


    private static void assertZippelGCD(MultivariatePolynomial<BigInteger> gcd,
                                        MultivariatePolynomial<BigInteger> a,
                                        MultivariatePolynomial<BigInteger> b) {
        MultivariatePolynomial<BigInteger> actualGCD = ZippelGCD(a, b);
        lMultivariatePolynomial lActualGCD = ZippelGCD(asLongPolyZp(a), asLongPolyZp(b));
        Assert.assertTrue(dividesQ(actualGCD, gcd));
        Assert.assertEquals(asLongPolyZp(actualGCD).monic(), lActualGCD.monic());
    }

    @Test
    public void testZippel1() throws Exception {
        String[] vars = {"a", "b", "c"};
        IntegersModulo domain = new IntegersModulo(BigPrimes.nextPrime(5642342L));
        MultivariatePolynomial<BigInteger>
                a = parse("a^2 + b^2 + a*c^2", domain, LEX, vars),
                b = parse("a^2 + 2*b^2 + b*c^2", domain, LEX, vars),
                gcd = parse("a^2 + c*a + b + a*c*b + a*c^2", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);

        RandomGenerator rnd = getRandom();

        int variable = a.nVariables - 1;
        BigInteger seed = BigInteger.valueOf(101);
        MultivariatePolynomial<BigInteger> skeleton = gcd.evaluate(variable, seed);

        for (int i = 0; i < 100; i++) {
            SparseInterpolation<BigInteger> sparseInterpolation
                    = createInterpolation(variable, a, b, skeleton, rnd);
            BigInteger point = domain.randomElement(rnd);
            assertEquals(gcd.evaluate(variable, point), sparseInterpolation.evaluate(point));
        }

        lMultivariatePolynomial la = asLongPolyZp(a), lb = asLongPolyZp(b),
                lskeleton = asLongPolyZp(skeleton), lgcd = asLongPolyZp(gcd);
        for (int i = 0; i < 100; i++) {
            lSparseInterpolation sparseInterpolation
                    = createInterpolation(variable, la, lb, lskeleton, rnd);
            long point = domain.asLong().randomElement(rnd);
            assertEquals(lgcd.evaluate(variable, point), sparseInterpolation.evaluate(point));
        }
    }

    @Test
    public void testZippel2() throws Exception {
        String[] vars = {"a", "b", "c"};
        IntegersModulo domain = new IntegersModulo(BigPrimes.nextPrime(5642342L));
        MultivariatePolynomial<BigInteger>
                a = parse("a^2 + b^2 + a*c^2", domain, LEX, vars),
                b = parse("a^2 + 2*b^2 + b*c^2", domain, LEX, vars),
                gcd = parse("a^2 + c*a + b + a*c*b + a*c^2", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);
        assertZippelGCD(gcd, a, b);
    }

    @Test
    public void testZippel5() throws Exception {
        String[] vars = {"a", "b", "c"};
        IntegersModulo domain = new IntegersModulo(BigPrimes.nextPrime(31579447));
        System.out.println(domain);
        MultivariatePolynomial<BigInteger>
                a = parse("b^2 + a*c + a*b*c^2 + a*b^2 + a^5", domain, LEX, vars),
                b = parse("1 + a*b", domain, LEX, vars),
                gcd = parse("1 + a*b*c^2 + a^2*c^2 + a^2*b*c^2 + a^2*b^2*c^2 + a^7", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);


        RandomGenerator rnd = getRandom();
        rnd.setSeed(123);

        int variable = a.nVariables - 1;
        BigInteger seed = BigInteger.valueOf(7893482);
        MultivariatePolynomial<BigInteger> skeleton = gcd.evaluate(variable, seed);

        SparseInterpolation<BigInteger> sparseInterpolation = createInterpolation(variable, a, b, skeleton, rnd);
        BigInteger point = domain.valueOf(1324);
        assertEquals(gcd.evaluate(variable, point), sparseInterpolation.evaluate(point));
    }

    @Test
    public void testZippel6() throws Exception {
        MultivariateGCD.ALWAYS_LINZIP = true;
        String[] vars = {"a", "b", "c"};
        IntegersModulo domain = new IntegersModulo(BigPrimes.nextPrime(31579447));
        MultivariatePolynomial<BigInteger>
                a = parse("1+3*b*c^2+7*b^2*c^2+4*a+7*a^3*c^2+a^6", domain, LEX, vars),
                b = parse("b^3*c^2+a*b^2*c+9*a^2*b*c+9*a^3*b*c^2+a^7", domain, LEX, vars),
                gcd = parse("b^3*c^2+2*a*c+5*a*b+2*a*b^2*c+4*a*b^3*c^2+5*a^3*b+a^7", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);


        RandomGenerator rnd = getRandom();
        rnd.setSeed(123);

        int variable = a.nVariables - 1;
        BigInteger seed = BigInteger.valueOf(7893482);
        MultivariatePolynomial<BigInteger> skeleton = gcd.evaluate(variable, seed);

        SparseInterpolation<BigInteger> sparseInterpolation = createInterpolation(variable, a, b, skeleton, rnd);
        BigInteger point = domain.valueOf(1324);
        assertEquals(gcd.evaluate(variable, point), sparseInterpolation.evaluate(point));
    }

    @Test
    public void testZippel_monic_random1() throws Exception {
        RandomGenerator rnd = getRandom();
        int nVarsMin = 3, nVarsMax = 10, minDegree = 3, maxDegree = 5, minSize = 5, maxSize = 10;

        int nIterations = its(100, 1500);
        TripletPort sampleData = new TripletPort(nVarsMin, nVarsMax, minDegree, maxDegree, minSize, maxSize, rnd);
        for (int n = 0; n < nIterations; n++) {
            if (n % 100 == 0) System.out.println(n);

            GCDTriplet data = sampleData.nextSample(true, true);

            int variable = data.a.nVariables - 1;
            BigInteger seed;
            do {seed = data.a.domain.randomElement(rnd);} while (seed.isZero());
            MultivariatePolynomial<BigInteger> skeleton = data.gcd.evaluate(variable, seed);

            for (int i = 0; i < 10; i++) {
                int rndSeed = i^n;
                rnd.setSeed(rndSeed);
                SparseInterpolation<BigInteger> sparseInterpolation
                        = createInterpolation(variable, data.aGCD, data.bGCD, skeleton, rnd);

                lSparseInterpolation lSparseInterpolation
                        = createInterpolation(variable, asLongPolyZp(data.aGCD), asLongPolyZp(data.bGCD), asLongPolyZp(skeleton), rnd);
                BigInteger point = data.a.domain.randomElement(rnd);
                try {
                    MultivariatePolynomial<BigInteger> expected = data.gcd.evaluate(variable, point).monic();
                    MultivariatePolynomial<BigInteger> actual = sparseInterpolation.evaluate(point).monic();
                    assertEquals(expected, actual);

                    lMultivariatePolynomial lActual = lSparseInterpolation.evaluate(point.longValueExact()).monic();
                    assertEquals(asLongPolyZp(expected), lActual);
                } catch (Throwable e) {
                    System.out.println("rnd seed : " + rndSeed);
                    System.out.println("seed point : " + seed);
                    System.out.println("point : " + point);
                    System.out.println("a: " + data.a);
                    System.out.println("b: " + data.b);
                    System.out.println("gcd : " + data.gcd);
                    throw e;
                }
            }
        }
    }

    @Test
    public void testZippel7() throws Exception {
        MultivariateGCD.ALWAYS_LINZIP = true;
        String[] vars = {"a", "b", "c"};
        IntegersModulo domain = new IntegersModulo(BigPrimes.nextPrime(31579447));
        MultivariatePolynomial<BigInteger>
                a = parse("29322275*b+5*b*c+6*a^2*b^3*c^2+29322274*a^2*b^3*c^2*d^3+5*a^3*b*c^2*d^2+a^11", domain, LEX, vars),
                b = parse("7*a^3*b^3*c^3*d^4+9*a^3*b^4*c+29322274*a^3*b^4*c^5*d^2+29322277*a^5*b*c*d+a^5*b^4+a^15", domain, LEX, vars),
                gcd = parse("4*d^2+8*c^2*d+4*b*c+6*b^3*c^2*d^2+2*a^3*b^2*c^3*d+a^10", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);

        RandomGenerator rnd = getRandom();
        rnd.setSeed(123);

        int variable = a.nVariables - 1;
        BigInteger seed = BigInteger.valueOf(7893482);
        MultivariatePolynomial<BigInteger> skeleton = gcd.evaluate(variable, seed);

        SparseInterpolation<BigInteger> sparseInterpolation = createInterpolation(variable, a, b, skeleton, rnd);
        BigInteger point = domain.valueOf(1324);
        assertEquals(gcd.evaluate(variable, point), sparseInterpolation.evaluate(point));
    }

    @Test
    public void testZippel_monic_random2() throws Exception {
        RandomGenerator rnd = getRandom();
        int nVarsMin = 3, nVarsMax = 5, minDegree = 3, maxDegree = 5, minSize = 5, maxSize = 10;
        int nIterations = its(100, 1500);

        DescriptiveStatistics zippel = new DescriptiveStatistics(), brown = new DescriptiveStatistics();

        TripletPort sampleData = new TripletPort(nVarsMin, nVarsMax, minDegree, maxDegree, minSize, maxSize, rnd);
        for (int n = 0; n < nIterations; n++) {
            if (n == nIterations / 10) {
                zippel.clear(); brown.clear();
            }
            if (n % 100 == 0) System.out.println(n);

            GCDTriplet data = sampleData.nextSample(false, true);
            MultivariatePolynomial<BigInteger> gcdZippel = null, gcdBrown = null;
            try {
                PrivateRandom.getRandom().setSeed(n);
                long start = System.nanoTime();
                gcdZippel = ZippelGCD(data.aGCD, data.bGCD);
                zippel.addValue(System.nanoTime() - start);

                start = System.nanoTime();
                gcdBrown = BrownGCD(data.aGCD, data.bGCD);
                brown.addValue(System.nanoTime() - start);

                assertTrue(dividesQ(gcdZippel, data.gcd));
                assertTrue(dividesQ(gcdBrown, data.gcd));

                PrivateRandom.getRandom().setSeed(n);
                lMultivariatePolynomial lGcdZippel = ZippelGCD(asLongPolyZp(data.aGCD), asLongPolyZp(data.bGCD));
                assertTrue(dividesQ(lGcdZippel, asLongPolyZp(data.gcd)));
            } catch (Throwable e) {
                System.out.println("rnd seed : " + n);
                System.out.println("domain: " + data.domain);
                System.out.println("a: " + data.a);
                System.out.println("b: " + data.b);
                System.out.println("gcd : " + data.gcd);
                System.out.println("gcdActual : " + gcdZippel);
                throw e;
            }
        }
        System.out.println("Zippel: " + TimeUnits.statisticsNanotime(zippel));
        System.out.println("Brown: " + TimeUnits.statisticsNanotime(brown));
    }

    @Test(timeout = 10000)
    public void testZippel9() throws Exception {
        String[] vars = {"a", "b", "c"};
        IntegersModulo domain = new IntegersModulo(26478253);
        PrivateRandom.getRandom().setSeed(0);
        MultivariatePolynomial<BigInteger>
                a = parse("26478246*a*c^2+7*a*b+26478250*a*b*c^2+26478249*a*b^3*c^2+26478248*a^2*c^2+8*a^3*b*c^2+a^7", domain, LEX, vars),
                b = parse("4*b^3*c^2+7*a+5*a*b+8*a*b^2+6*a^3*b^2*c+a^7", domain, LEX, vars),
                gcd = parse("26478248*a*b^2*c^2+3*a*b^3*c^2+2*a^2*b^3*c^2+5*a^3*c+a^8", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);
        assertNotNull(ZippelGCD(a, b));
        assertNotNull(ZippelGCD(asLongPolyZp(a), asLongPolyZp(b)));
    }

    @Test
    public void testZippel8() throws Exception {
        MultivariateGCD.ALWAYS_LINZIP = true;
        String[] vars = {"a", "b", "c"};
        IntegersModulo domain = new IntegersModulo(31579447);
        MultivariatePolynomial<BigInteger>
                a = parse("5*a+29923129*a*b*c^2+3*a*b^2+29923132*a^2*b*c^2+7*a^3*c", domain, LEX, vars),
                b = parse("4*c^2+29923126*a*c^2+5*a*b+6*a^2*b^2*c^3+29923128*a^3*c^3", domain, LEX, vars),
                gcd = parse("29923132+8*b*c^3+29923132*b^2*c^3+8*a*b*c+7*a^3*b^3*c", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);


        RandomGenerator rnd = getRandom();
        rnd.setSeed(123);

        int variable = a.nVariables - 1;
        BigInteger seed = BigInteger.valueOf(7893482);
        MultivariatePolynomial<BigInteger> skeleton = gcd.evaluate(variable, seed);

        SparseInterpolation<BigInteger> sparseInterpolation = createInterpolation(variable, a, b, skeleton, rnd);
        BigInteger point = domain.valueOf(1324);
        assertEquals(gcd.evaluate(variable, point).monic(), sparseInterpolation.evaluate(point).monic());
    }

    @Test
    public void testZippel_nonmonic_random2() throws Exception {
        MultivariateGCD.ALWAYS_LINZIP = true;
        RandomGenerator rnd = getRandom();
        int nVarsMin = 3, nVarsMax = 5, minDegree = 3, maxDegree = 5, minSize = 5, maxSize = 10;
        int nIterations = its(100, 1500);

        DescriptiveStatistics zippel = new DescriptiveStatistics(), brown = new DescriptiveStatistics();

        TripletPort sampleData = new TripletPort(nVarsMin, nVarsMax, minDegree, maxDegree, minSize, maxSize, rnd);
        for (int n = 0; n < nIterations; n++) {
            if (n == nIterations / 10) {
                zippel.clear(); brown.clear();
            }
            if (n % 10 == 0)
                System.out.println(n);

            GCDTriplet data = sampleData.nextSample(false, false);
            MultivariatePolynomial<BigInteger> gcdZippel = null, gcdBrown = null;
            try {
                PrivateRandom.getRandom().setSeed(n);
                long start = System.nanoTime();
                gcdZippel = ZippelGCD(data.aGCD, data.bGCD);
                zippel.addValue(System.nanoTime() - start);

                start = System.nanoTime();
                gcdBrown = BrownGCD(data.aGCD, data.bGCD);
                brown.addValue(System.nanoTime() - start);

                assertTrue(dividesQ(gcdZippel, data.gcd));
                assertTrue(dividesQ(gcdBrown, data.gcd));

                PrivateRandom.getRandom().setSeed(n);
                lMultivariatePolynomial lGcdZippel = ZippelGCD(asLongPolyZp(data.aGCD), asLongPolyZp(data.bGCD));
                assertTrue(dividesQ(lGcdZippel, asLongPolyZp(data.gcd)));
            } catch (Throwable e) {
                System.out.println("rnd seed : " + n);
                System.out.println("domain: " + data.domain);
                System.out.println("a: " + data.a);
                System.out.println("b: " + data.b);
                System.out.println("gcd : " + data.gcd);
                System.out.println("gcdActual : " + gcdZippel);
                throw e;
            }
        }
        System.out.println("Zippel: " + TimeUnits.statisticsNanotime(zippel));
        System.out.println("Brown: " + TimeUnits.statisticsNanotime(brown));
    }

    @Test
    public void assadasd() throws Exception {
        IntegersModulo domain = new IntegersModulo(21535757L);
        MultivariatePolynomial<BigInteger> a = parse("13659400*b^3*c*d+6829700*a*b^3*c^3*d+3855362*a^2*b^3*c^3*d^3+2974338*a^3*b^3*c^2*d^3", domain, LEX);
        MultivariatePolynomial<BigInteger> b = parse("6107385*b^3*c*d+13821571*a*b^3*c^3*d+8143180*a^2*b^3*c^3*d^3+5678391*a^3*b^3*c^2*d^3", domain, LEX);
        System.out.println(a.monic());
        System.out.println(b.monic());
    }

    @Test
    public void testZippel_nonmonic_random3() throws Exception {
        MultivariateGCD.ALWAYS_LINZIP = true;
        RandomGenerator rnd = getRandom();
        int nVarsMin = 7, nVarsMax = 10, minDegree = 7, maxDegree = 10, minSize = 7, maxSize = 10;
        int nIterations = its(100, 1500);

        DescriptiveStatistics zippel = new DescriptiveStatistics();

        TripletPort sampleData = new TripletPort(nVarsMin, nVarsMax, minDegree, maxDegree, minSize, maxSize, rnd);
        for (int n = 0; n < nIterations; n++) {
            if (n == nIterations / 10)
                zippel.clear();
            if (n % 10 == 0)
                System.out.println(n);

            GCDTriplet data = sampleData.nextSample(false, false);
            MultivariatePolynomial<BigInteger> gcdZippel = null, gcdBrown = null;
            try {
                PrivateRandom.getRandom().setSeed(n);
                long start = System.nanoTime();
                gcdZippel = ZippelGCD(data.aGCD, data.bGCD);
                zippel.addValue(System.nanoTime() - start);
                assertTrue(dividesQ(gcdZippel, data.gcd));

                PrivateRandom.getRandom().setSeed(n);
                lMultivariatePolynomial lGcdZippel = ZippelGCD(asLongPolyZp(data.aGCD), asLongPolyZp(data.bGCD));
                assertTrue(dividesQ(lGcdZippel, asLongPolyZp(data.gcd)));
            } catch (Throwable e) {
                System.out.println("rnd seed : " + n);
                System.out.println("domain: " + data.domain);
                System.out.println("a: " + data.a);
                System.out.println("b: " + data.b);
                System.out.println("gcd : " + data.gcd);
                System.out.println("gcdActual : " + gcdZippel);
                throw e;
            }
        }
        System.out.println("Zippel: " + TimeUnits.statisticsNanotime(zippel));
    }

    @Test
    public void testZippel3() throws Exception {
        String[] vars = {"a", "b", "c"};
        IntegersModulo domain = new IntegersModulo(BigPrimes.nextPrime(5642342L));
        MultivariatePolynomial<BigInteger>
                a = parse("5*a^2*c^2+5*a^2*b^2*c^2+5*a^2*b^4*c^3+9*a^2*b^5*c^5+25709547*a^3*b^6*c^6+8*a^4*b*c^3+a^4*b^3*c+5*a^4*b^3*c^6", domain, LEX, vars),
                b = parse("3*a*b^2*c^2+2*a^2*b^4+25709540*a^4*b*c^6+7*a^5*c^2+8*a^6*b*c^3", domain, LEX, vars),
                gcd = parse("a + 5*b^2*c^6+2*a^4*b^4*c^5+25709543*a^5*b^2*c^5+9*a^6*c+25709540*a^6*c^3", domain, LEX, vars);
        RandomGenerator rnd = getRandom();

        int variable = a.nVariables - 1;
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);


        System.out.println(ZippelGCD(a, b));
    }

    @Test
    public void testZippel10() throws Exception {
        String[] vars = {"a", "b", "c"};
        IntegersModulo domain = new IntegersModulo(BigPrimes.nextPrime(5642342L));
        MultivariatePolynomial<BigInteger>
                a = parse("a + b + c", domain, LEX, vars),
                b = parse("a - b + c", domain, LEX, vars),
                gcd = parse("a^3*b+2*a^3+c*a^3+12*b^2+24*b+12*b*c", domain, LEX, vars);
        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);

        assertZippelGCD(gcd, a, b);
    }

    @Ignore
    @Test
    public void testZippel4_performance() throws Exception {
        PrivateRandom.getRandom().setSeed(1232);
        String[] vars = {"a", "b", "c", "d", "e"};
        IntegersModulo domain = new IntegersModulo(SmallPrimes.nextPrime(100000));
        MultivariatePolynomial<BigInteger>
                a = parse("2147483167*a^4*b^60*c^57*d^26*e+44*a^8*b^39*c^67*d^22*e^17+38*a^32*b^6*c^13*d^10*e^3+357*a^36*b^34*c^60*d^2*e^59+563*a^42*b^41*c^45*d^52*e^14+257*a^44*b^68*c^43*d^2*e^73+613*a^48*b^55*c^22*d^32*e^19+2147483093*a^52*b^26*c^4*d^72*e^32+19*a^52*b^40*c^26*d^45*e^55+639*a^55*b^72*c^55*d^65", domain, LEX, vars),
                b = parse("2147483150*b^25*c^18*d^62*e^59+2147482723*a^4*b^5*c^65*d^26*e^7+261*a^15*b^60*c^59*d^63*e^53+394*a^27*b^22*c^34*d^54*e^13+952*a^39*b^48*c^17*d^54*e^16+243*a^60*b^15*c^3*d^51*e^46+40*a^61*b^56*c^39*d^40*e^21+555*a^62*b^20*c^20*d^60*e^47+627*a^67*b^8*c^22*d^67*e^61+447*a^70*b^59*c^71*d^24*e^5", domain, LEX, vars),
                gcd = parse("35*a*b^36*c^74*d^62*e^51+376*a^2*b^28*c^64*e^53+893*a^6*b^13*c^60*d^44*e^42+23*a^8*b^71*c^40*d^36*e^11+783*a^20*b^28*c^12*d^31*e^68+2147482938*a^31*b^30*c^40*d^65*e^72+2147482960*a^31*b^49*c^38*d^71*e^55+737*a^47*b^15*c^71*d^13*e^72+868*a^53*b^30*c^40*d^29*e^46+898*a^61*b^71*c^13*d^50*e^66", domain, LEX, vars);

        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);
        System.out.println(a);
        System.out.println(b);

        lMultivariatePolynomial
                aL = asLongPolyZp(a),
                bL = asLongPolyZp(b);

        for (int i = 0; i < 1000; i++) {
            long start = System.nanoTime();
            assertEquals(10, ZippelGCD(aL, bL).size());
            System.out.println(System.nanoTime() - start);
        }
    }

    @Test
    public void testPairedIterator() throws Exception {
        RandomGenerator rnd = getRandom();
        int nIterations = its(1000, 1000);
        for (int n = 0; n < nIterations; n++) {
            MultivariatePolynomial<BigInteger>
                    a = randomPolynomial(5, 50, 20, rnd),
                    b = randomPolynomial(5, 50, 20, rnd);

            PairIterator<MonomialTerm<BigInteger>, MultivariatePolynomial<BigInteger>>
                    it = new PairIterator<>(a, b);

            MultivariatePolynomial<BigInteger> acc = a.createZero();
            while (it.hasNext()) {
                it.advance();
                acc.add(it.aTerm);
                acc.add(it.bTerm);
                assertTrue(it.aTerm.coefficient.isZero() || it.bTerm.coefficient.isZero() || 0 == a.ordering.compare(it.aTerm, it.bTerm));
            }
            assertEquals(acc, a.clone().add(b));
        }
    }

    @Test
    public void testSparseInterpolation1() throws Exception {
        IntegersModulo domain = new IntegersModulo(31574773);
        MultivariatePolynomial<BigInteger>
                a = parse("31574768*a*b^2*c^4+4*a^4*b^3+3*a^5*b+31574764*a^5*b^5*c^5+6*a^6*b^3*c^2", domain, LEX),
                b = parse("7*a^2*b^6*c^3+a^5*b^4*c^4+31574764*a^6*c^3+5*a^6*b^2*c^2", domain, LEX),
                gcd = parse("9*c^4+31574766*a*b^2+2*a^2*b*c^2+31574768*a^2*b^3*c^6+9*a^3*b^2*c^3", domain, LEX);

        a = a.multiply(gcd);
        b = b.multiply(gcd);
        assertEquals(ZippelGCD(a, b).monic(), interpolateGCD(asLongPolyZp(a), asLongPolyZp(b), asLongPolyZp(ZippelGCD(a, b)), getRandom()).monic().toBigPoly());
    }

    @Test
    public void testSparseInterpolation2() throws Exception {
        IntegersModulo domain = new IntegersModulo(24001871);
        MultivariatePolynomial<BigInteger>
                a = parse("3*b^4*c^2+7*b^4*c^3+4*a*b^5*c^3+6*a^4*b^6+24001865*a^5*b", domain, LEX, "a", "b", "c"),
                b = parse("5*a*c^4+9*a^4*b^4*c^2+9*a^6*b*c^6", domain, LEX, "a", "b", "c"),
                gcd = parse("5*a*b^2*c^2+a*b^2*c^4+24001866*a*b^4*c^3", domain, LEX, "a", "b", "c");

        a = a.multiply(gcd);
        b = b.multiply(gcd);
        assertEquals(asLongPolyZp(gcd).monic(), interpolateGCD(asLongPolyZp(a), asLongPolyZp(b), asLongPolyZp(gcd), getRandom()).monic());
    }

    @Test
    public void testSparseInterpolation3() throws Exception {
        IntegersModulo domain = new IntegersModulo(17312587);
        MultivariatePolynomial<BigInteger>
                a = parse("5*a^3*c^6+9*a^5*b^2*c^3+7*a^5*b^6*c^5+8*a^5*b^6*c^6+6*a^6*b^6*c", domain, LEX, "a", "b", "c"),
                b = parse("17312581*c^6+5*a^2*b^2*c^6+3*a^4*b^6*c^4+2*a^5+4*a^5*b^3*c^6", domain, LEX, "a", "b", "c"),
                gcd = parse("5*a^5*b*c^2+6*a^5*b^3*c^6+2*a^5*b^4*c^4", domain, LEX, "a", "b", "c");

        a = a.multiply(gcd);
        b = b.multiply(gcd);
        lMultivariatePolynomial intrp = interpolateGCD(asLongPolyZp(a), asLongPolyZp(b), asLongPolyZp(gcd), getRandom());
        assertEquals(asLongPolyZp(gcd).monic(), intrp.monic());
    }

    @Test
    public void testSparseInterpolation4() throws Exception {
        IntegersModulo domain = new IntegersModulo(27445993);
        MultivariatePolynomial<BigInteger>
                a = parse("7*a*b*c^3+8*a^3*c+8*a^4*b^2*c^4+8*a^4*b^6*c^6", domain, LEX, "a", "b", "c"),
                b = parse("a*b^6*c^2+6*a^2*b^3*c^3+27445990*a^3*b^6*c^2", domain, LEX, "a", "b", "c"),
                gcd = parse("5*b*c^3+8*b^5*c+4*b^6+5*a*b^3+4*a^6*b^3*c^3", domain, LEX, "a", "b", "c");

        a = a.multiply(gcd);
        b = b.multiply(gcd);

        lMultivariatePolynomial la = asLongPolyZp(a), lb = asLongPolyZp(b);
        lMultivariatePolynomial lgcd = ZippelGCD(la, lb);
        lMultivariatePolynomial intrp = interpolateGCD(la, lb, lgcd, getRandom());
        assertEquals(lgcd.monic(), intrp.monic());
    }

    @Test
    public void testSparseInterpolation_random1() throws Exception {
        int nIterations = its(1000, 5000);
        RandomGenerator rnd = getRandom();
        TripletPort sampleData = new TripletPort(3, 5, 5, 15, 5, 15, rnd);
        for (int n = 0; n < nIterations; n++) {
            GCDTriplet gcdTriplet = sampleData.nextSample(false, false);
            lMultivariatePolynomial gcd = null, actual = null;
            try {

                lMultivariatePolynomial la = asLongPolyZp(gcdTriplet.aGCD);
                lMultivariatePolynomial lb = asLongPolyZp(gcdTriplet.bGCD);
                gcd = ZippelGCD(la, lb);
                if (la.isConstant() || lb.isConstant() || gcd.degree(0) == 0) {
                    --n; continue;
                }
                actual = interpolateGCD(la, lb, gcd, rnd);
                assertEquals(gcd.monic(), actual.monic());
            } catch (Throwable thr) {
                System.out.println(gcdTriplet.domain);
                System.out.println(gcdTriplet.a);
                System.out.println(gcdTriplet.b);
                System.out.println(gcd);
                System.out.println(actual);
                throw thr;
            }
        }
    }

    @Test
    public void testSparseInterpolation5() throws Exception {
        MultivariatePolynomial<BigInteger>
                a = parse("7*a*b*c^3+8*a^3*c+8*a^4*b^2*c^4+8*a^4*b^6*c^6", Integers, LEX, "a", "b", "c"),
                b = parse("a*b^6*c^2+6*a^2*b^3*c^3+27445990*a^3*b^6*c^2", Integers, LEX, "a", "b", "c"),
                gcd = parse("5*b*c^3+8*b^5*c+4*b^6+5*a*b^3+4*a^6*b^3*c^3", Integers, LEX, "a", "b", "c");

        a = a.multiply(gcd);
        b = b.multiply(gcd);

        IntegersModulo domain = new IntegersModulo(27445993);

        lMultivariatePolynomial
                la = asLongPolyZp(a.setDomain(domain)),
                lb = asLongPolyZp(b.setDomain(domain));
        lMultivariatePolynomial skeleton = ZippelGCD(la, lb);

        IntegersModulo domain1 = new IntegersModulo(BigPrimes.nextPrime(37445993132451L));
        lMultivariatePolynomial
                la1 = asLongPolyZp(a.setDomain(domain1)),
                lb1 = asLongPolyZp(b.setDomain(domain1));

        lMultivariatePolynomial gcd1 = ZippelGCD(la1, lb1);

        skeleton = skeleton.setDomain(la1.domain);
        lMultivariatePolynomial intrp = interpolateGCD(la1, lb1, skeleton, getRandom());
        assertEquals(gcd1.monic(), intrp.monic());
    }

    @Test
    public void testSparseInterpolation_random2() throws Exception {
        int nIterations = its(500, 1000);
        RandomGenerator rnd = getRandom();
        TripletPort sampleData = new TripletPort(3, 5, 5, 15, 5, 15, rnd);
        for (int n = 0; n < nIterations; n++) {
            GCDTriplet gcdTriplet = sampleData.nextSample(false, false).asZ();
            lMultivariatePolynomial skeleton = null, gcd = null, actual = null;
            try {

                IntegersModulo domain = new IntegersModulo(getModulusRandom(20));
                lMultivariatePolynomial
                        la = asLongPolyZp(gcdTriplet.aGCD.setDomain(domain)),
                        lb = asLongPolyZp(gcdTriplet.bGCD.setDomain(domain));

                skeleton = ZippelGCD(la, lb);
                if (la.isConstant() || lb.isConstant() || skeleton.degree(0) == 0) {
                    --n; continue;
                }

                IntegersModulo domain1 = new IntegersModulo(getModulusRandom(20));
                lMultivariatePolynomial
                        la1 = asLongPolyZp(gcdTriplet.aGCD.setDomain(domain1)),
                        lb1 = asLongPolyZp(gcdTriplet.bGCD.setDomain(domain1));

                gcd = ZippelGCD(la1, lb1);
                if (!gcd.sameSkeleton(skeleton)) {
                    --n; continue;
                }

                actual = interpolateGCD(la1, lb1, skeleton.setDomain(la1.domain), rnd);
                assertEquals(gcd.monic(), actual.monic());
            } catch (Throwable thr) {
                System.out.println(gcdTriplet.domain);
                System.out.println(gcdTriplet.a);
                System.out.println(gcdTriplet.b);
                System.out.println(skeleton);
                System.out.println(actual);
                throw thr;
            }
        }
    }

    @Ignore
    @Test
    public void testSparseInterpolation6a() throws Exception {
        for (int i = 0; i < 10000; i++) {
            RandomGenerator rnd = PrivateRandom.getRandom();
            rnd.setSeed(i);
            IntegersModulo domain = new IntegersModulo(1049);
            MultivariatePolynomial<BigInteger>
                    a = parse("15*a*b^5*c^5*d^3+27*a^2*b^10*c^4*d+35*a^3*b^7*c^5*d^4+20*a^3*b^7*c^5*d^5+40*a^4*b^11*c^11*d^10+63*a^4*b^12*c^4*d^2+36*a^4*b^12*c^4*d^3+72*a^5*b^16*c^10*d^8+243*a^6*b^12*c^9*d^4+15*a^6*b^15*c*d^3+15*a^6*b^15*c^11*d^10+45*a^7*b^9*c^11*d^7+5*a^7*b^14*c^7*d^4+12*a^8*b*c*d^11+35*a^8*b^14*c^4*d^8+567*a^8*b^14*c^9*d^5+324*a^8*b^14*c^9*d^6+81*a^8*b^14*c^10*d^5+231*a^8*b^15*c^3*d^10+35*a^8*b^17*c*d^4+20*a^8*b^17*c*d^5+35*a^8*b^17*c^11*d^11+20*a^8*b^17*c^11*d^12+9*a^8*b^19*c^6*d^2+15*a^9*c^10*d^11+415*a^9*b^6*c^4*d^11+390*a^9*b^8*c^8*d^5+648*a^9*b^18*c^15*d^11+63*a^9*b^19*c^3*d^6+40*a^9*b^21*c^7*d^10+40*a^9*b^21*c^17*d^17+24*a^10*c^3*d^11+28*a^10*b^3*c*d^12+16*a^10*b^3*c*d^13+747*a^10*b^11*c^3*d^9+20*a^10*b^12*c^4*d^5+702*a^10*b^13*c^7*d^3+405*a^10*b^14*c^5*d^5+539*a^10*b^17*c^3*d^11+308*a^10*b^17*c^3*d^12+35*a^11*b^2*c^10*d^12+20*a^11*b^2*c^10*d^13+32*a^11*b^7*c^7*d^18+36*a^11*b^17*c^3*d^3+729*a^11*b^19*c^4*d^3+616*a^11*b^21*c^9*d^17+56*a^12*b^2*c^3*d^12+32*a^12*b^2*c^3*d^13+40*a^12*b^6*c^16*d^18+729*a^12*b^16*c^15*d^8+45*a^12*b^19*c^7*d^7+45*a^12*b^19*c^17*d^14+81*a^12*b^21*c^11*d^5+5*a^12*b^24*c^3*d^4+5*a^12*b^24*c^13*d^11+18*a^13*b^3*c^5*d^13+64*a^13*b^6*c^9*d^18+567*a^13*b^21*c^8*d^9+35*a^13*b^24*d^8+35*a^13*b^24*c^10*d^15+36*a^14*b^5*c^7*d^15+4*a^14*b^10*c^3*d^12+429*a^14*b^13*c^8*d^12+24*a^14*b^15*c^12*d^6+415*a^14*b^16*d^11+415*a^14*b^16*c^10*d^18+390*a^14*b^18*c^4*d^5+390*a^14*b^18*c^14*d^12+693*a^14*b^19*c^9*d^14+77*a^14*b^24*c^5*d^11+45*a^15*b^4*c^16*d^15+42*a^15*b^5*c^5*d^14+24*a^15*b^5*c^5*d^15+5*a^15*b^9*c^12*d^12+28*a^15*b^10*d^16+324*a^15*b^19*c^8*d^6+267*a^15*b^21*c^9*d^6+20*a^15*b^22*d^5+20*a^15*b^22*c^10*d^12+405*a^15*b^24*c*d^5+539*a^15*b^24*c^2*d^15+405*a^15*b^24*c^11*d^12+332*a^16*b^2*d^19+312*a^16*b^4*c^4*d^13+72*a^16*b^4*c^9*d^15+8*a^16*b^9*c^5*d^12+35*a^16*b^9*c^9*d^16+48*a^16*b^9*c^11*d^20+97*a^16*b^16*c^2*d^18+761*a^16*b^18*c^6*d^12+415*a^17*b*c^9*d^19+390*a^17*b^3*c^13*d^13+16*a^17*b^8*d^13+56*a^17*b^9*c^2*d^16+324*a^17*b^10*c*d^13+308*a^17*b^22*c^2*d^12+992*a^17*b^24*c^3*d^12+664*a^18*b*c^2*d^19+624*a^18*b^3*c^6*d^13+20*a^18*b^7*c^9*d^13+405*a^18*b^9*c^10*d^13+32*a^19*b^7*c^2*d^13+54*a^19*b^7*c^11*d^17+648*a^19*b^9*c^3*d^13+6*a^19*b^12*c^7*d^14+42*a^20*b^12*c^4*d^18+498*a^21*b^4*c^4*d^21+468*a^21*b^6*c^8*d^15+24*a^22*b^10*c^4*d^15+486*a^22*b^12*c^5*d^15", domain, LEX),
                    b = parse("12*c^6*d^2+234*b^8*c^8*d^6+28*a^2*b^2*c^6*d^3+16*a^2*b^2*c^6*d^4+3*a^2*b^7*c^5*d+546*a^2*b^10*c^8*d^7+312*a^2*b^10*c^8*d^8+32*a^3*b^6*c^12*d^9+624*a^3*b^14*c^14*d^13+7*a^4*b^9*c^5*d^2+4*a^4*b^9*c^5*d^3+8*a^5*b^13*c^11*d^8+12*a^6*b*c^6+36*a^6*b^4*c^12*d^6+4*a^6*b^9*c^8*d^3+702*a^6*b^12*c^14*d^10+78*a^6*b^17*c^10*d^7+28*a^7*b^9*c^5*d^7+546*a^7*b^17*c^7*d^11+3*a^8*c*d^3+332*a^8*b*c^5*d^10+28*a^8*b^3*c^6*d+16*a^8*b^3*c^6*d^2+312*a^8*b^3*c^9*d^4+180*a^8*b^9*c^7*d^14+9*a^8*b^11*c^11*d^5+839*a^8*b^11*c^11*d^8+a^8*b^16*c^7*d^2+16*a^9*b^7*c^5*d^4+32*a^9*b^7*c^12*d^7+324*a^9*b^9*c^6*d^4+312*a^9*b^15*c^7*d^8+7*a^9*b^16*c^4*d^6+24*a^9*b^17*c^8*d^8+7*a^10*b^2*c*d^4+4*a^10*b^2*c*d^5+83*a^10*b^8*c^4*d^9+78*a^10*b^10*c^8*d^3+8*a^11*b^6*c^7*d^10+4*a^11*b^14*c^4*d^3+81*a^11*b^16*c^5*d^3+36*a^12*b^5*c^12*d^4+4*a^12*b^10*c^8*d+28*a^13*b^10*c^5*d^5+332*a^14*b^2*c^5*d^8+9*a^14*b^4*c^7*d^7+312*a^14*b^4*c^9*d^2+a^14*b^9*c^3*d^4+16*a^15*b^8*c^5*d^2+7*a^15*b^9*d^8+324*a^15*b^10*c^6*d^2+83*a^16*b*d^11+78*a^16*b^3*c^4*d^5+4*a^17*b^7*d^5+81*a^17*b^9*c*d^5", domain, LEX),
                    base = parse("3*c+7*a^2*b^2*c*d+4*a^2*b^2*c*d^2+8*a^3*b^6*c^7*d^7+9*a^6*b^4*c^7*d^4+a^6*b^9*c^3*d+7*a^7*b^9*d^5+17492158*a^8*b*d^8+17492153*a^8*b^3*c^4*d^2+4*a^9*b^7*d^2+17492156*a^9*b^9*c*d^2", domain, LEX);

            lMultivariatePolynomial
                    la = asLongPolyZp(a),
                    lb = asLongPolyZp(b),
                    skeleton = asLongPolyZp(base);
            lMultivariatePolynomial lgcd = ZippelGCD(la, lb);
            lMultivariatePolynomial intrp = null;
            try {
                rnd.setSeed(i);
                intrp = interpolateGCD(la, lb, skeleton, rnd);
                if (intrp != null)
                    assertEquals(lgcd.monic(), intrp.monic());
            } catch (Throwable e) {
                System.out.println(i);
                System.out.println(lgcd);
                System.out.println(intrp);
            }
        }
    }

    @Test
    public void testSparseInterpolation6() throws Exception {
        RandomGenerator rnd = PrivateRandom.getRandom();
        rnd.setSeed(743);
        IntegersModulo domain = new IntegersModulo(1049);
        MultivariatePolynomial<BigInteger>
                a = parse("15*a*b^5*c^5*d^3+27*a^2*b^10*c^4*d+35*a^3*b^7*c^5*d^4+20*a^3*b^7*c^5*d^5+40*a^4*b^11*c^11*d^10+63*a^4*b^12*c^4*d^2+36*a^4*b^12*c^4*d^3+72*a^5*b^16*c^10*d^8+243*a^6*b^12*c^9*d^4+15*a^6*b^15*c*d^3+15*a^6*b^15*c^11*d^10+45*a^7*b^9*c^11*d^7+5*a^7*b^14*c^7*d^4+12*a^8*b*c*d^11+35*a^8*b^14*c^4*d^8+567*a^8*b^14*c^9*d^5+324*a^8*b^14*c^9*d^6+81*a^8*b^14*c^10*d^5+231*a^8*b^15*c^3*d^10+35*a^8*b^17*c*d^4+20*a^8*b^17*c*d^5+35*a^8*b^17*c^11*d^11+20*a^8*b^17*c^11*d^12+9*a^8*b^19*c^6*d^2+15*a^9*c^10*d^11+415*a^9*b^6*c^4*d^11+390*a^9*b^8*c^8*d^5+648*a^9*b^18*c^15*d^11+63*a^9*b^19*c^3*d^6+40*a^9*b^21*c^7*d^10+40*a^9*b^21*c^17*d^17+24*a^10*c^3*d^11+28*a^10*b^3*c*d^12+16*a^10*b^3*c*d^13+747*a^10*b^11*c^3*d^9+20*a^10*b^12*c^4*d^5+702*a^10*b^13*c^7*d^3+405*a^10*b^14*c^5*d^5+539*a^10*b^17*c^3*d^11+308*a^10*b^17*c^3*d^12+35*a^11*b^2*c^10*d^12+20*a^11*b^2*c^10*d^13+32*a^11*b^7*c^7*d^18+36*a^11*b^17*c^3*d^3+729*a^11*b^19*c^4*d^3+616*a^11*b^21*c^9*d^17+56*a^12*b^2*c^3*d^12+32*a^12*b^2*c^3*d^13+40*a^12*b^6*c^16*d^18+729*a^12*b^16*c^15*d^8+45*a^12*b^19*c^7*d^7+45*a^12*b^19*c^17*d^14+81*a^12*b^21*c^11*d^5+5*a^12*b^24*c^3*d^4+5*a^12*b^24*c^13*d^11+18*a^13*b^3*c^5*d^13+64*a^13*b^6*c^9*d^18+567*a^13*b^21*c^8*d^9+35*a^13*b^24*d^8+35*a^13*b^24*c^10*d^15+36*a^14*b^5*c^7*d^15+4*a^14*b^10*c^3*d^12+429*a^14*b^13*c^8*d^12+24*a^14*b^15*c^12*d^6+415*a^14*b^16*d^11+415*a^14*b^16*c^10*d^18+390*a^14*b^18*c^4*d^5+390*a^14*b^18*c^14*d^12+693*a^14*b^19*c^9*d^14+77*a^14*b^24*c^5*d^11+45*a^15*b^4*c^16*d^15+42*a^15*b^5*c^5*d^14+24*a^15*b^5*c^5*d^15+5*a^15*b^9*c^12*d^12+28*a^15*b^10*d^16+324*a^15*b^19*c^8*d^6+267*a^15*b^21*c^9*d^6+20*a^15*b^22*d^5+20*a^15*b^22*c^10*d^12+405*a^15*b^24*c*d^5+539*a^15*b^24*c^2*d^15+405*a^15*b^24*c^11*d^12+332*a^16*b^2*d^19+312*a^16*b^4*c^4*d^13+72*a^16*b^4*c^9*d^15+8*a^16*b^9*c^5*d^12+35*a^16*b^9*c^9*d^16+48*a^16*b^9*c^11*d^20+97*a^16*b^16*c^2*d^18+761*a^16*b^18*c^6*d^12+415*a^17*b*c^9*d^19+390*a^17*b^3*c^13*d^13+16*a^17*b^8*d^13+56*a^17*b^9*c^2*d^16+324*a^17*b^10*c*d^13+308*a^17*b^22*c^2*d^12+992*a^17*b^24*c^3*d^12+664*a^18*b*c^2*d^19+624*a^18*b^3*c^6*d^13+20*a^18*b^7*c^9*d^13+405*a^18*b^9*c^10*d^13+32*a^19*b^7*c^2*d^13+54*a^19*b^7*c^11*d^17+648*a^19*b^9*c^3*d^13+6*a^19*b^12*c^7*d^14+42*a^20*b^12*c^4*d^18+498*a^21*b^4*c^4*d^21+468*a^21*b^6*c^8*d^15+24*a^22*b^10*c^4*d^15+486*a^22*b^12*c^5*d^15", domain, LEX),
                b = parse("12*c^6*d^2+234*b^8*c^8*d^6+28*a^2*b^2*c^6*d^3+16*a^2*b^2*c^6*d^4+3*a^2*b^7*c^5*d+546*a^2*b^10*c^8*d^7+312*a^2*b^10*c^8*d^8+32*a^3*b^6*c^12*d^9+624*a^3*b^14*c^14*d^13+7*a^4*b^9*c^5*d^2+4*a^4*b^9*c^5*d^3+8*a^5*b^13*c^11*d^8+12*a^6*b*c^6+36*a^6*b^4*c^12*d^6+4*a^6*b^9*c^8*d^3+702*a^6*b^12*c^14*d^10+78*a^6*b^17*c^10*d^7+28*a^7*b^9*c^5*d^7+546*a^7*b^17*c^7*d^11+3*a^8*c*d^3+332*a^8*b*c^5*d^10+28*a^8*b^3*c^6*d+16*a^8*b^3*c^6*d^2+312*a^8*b^3*c^9*d^4+180*a^8*b^9*c^7*d^14+9*a^8*b^11*c^11*d^5+839*a^8*b^11*c^11*d^8+a^8*b^16*c^7*d^2+16*a^9*b^7*c^5*d^4+32*a^9*b^7*c^12*d^7+324*a^9*b^9*c^6*d^4+312*a^9*b^15*c^7*d^8+7*a^9*b^16*c^4*d^6+24*a^9*b^17*c^8*d^8+7*a^10*b^2*c*d^4+4*a^10*b^2*c*d^5+83*a^10*b^8*c^4*d^9+78*a^10*b^10*c^8*d^3+8*a^11*b^6*c^7*d^10+4*a^11*b^14*c^4*d^3+81*a^11*b^16*c^5*d^3+36*a^12*b^5*c^12*d^4+4*a^12*b^10*c^8*d+28*a^13*b^10*c^5*d^5+332*a^14*b^2*c^5*d^8+9*a^14*b^4*c^7*d^7+312*a^14*b^4*c^9*d^2+a^14*b^9*c^3*d^4+16*a^15*b^8*c^5*d^2+7*a^15*b^9*d^8+324*a^15*b^10*c^6*d^2+83*a^16*b*d^11+78*a^16*b^3*c^4*d^5+4*a^17*b^7*d^5+81*a^17*b^9*c*d^5", domain, LEX);

        lMultivariatePolynomial
                la = asLongPolyZp(a),
                lb = asLongPolyZp(b);

        lMultivariatePolynomial lgcd = ZippelGCD(la, lb);
        assertTrue(dividesQ(la, lgcd));
        assertTrue(dividesQ(lb, lgcd));


        rnd.setSeed(701);
        lMultivariatePolynomial intrp = interpolateGCD(la, lb, lgcd, rnd);
        if (intrp != null)
            assertEquals(lgcd.monic(), intrp.monic());
    }

    @Test
    public void testModularGCD1() throws Exception {
        MultivariatePolynomial<BigInteger>
                a = parse("a + 17*b + 2*c"),
                b = parse("3*a + b - c"),
                gcd = parse("1273465812736485821734523745*a*b - 21475715234*b - c");
        assertEquals(gcd, ModularGCD(a.clone().multiply(gcd), b.clone().multiply(gcd)));
    }

    @Test
    public void testModularGCD2() throws Exception {
        MultivariatePolynomial<BigInteger>
                a = parse("1234324234*a + 12317*b + 2*c"),
                b = parse("3*a + 143423423423423412314*b - c"),
                gcd = parse("1273465812736485821734523745*a*b - 21475715234*b - 143423423423423412314123123*c");
        assertEquals(gcd, ModularGCD(a.clone().multiply(gcd), b.clone().multiply(gcd)));
    }

    @Test
    public void testModularGCD3() throws Exception {
        PrivateRandom.getRandom().setSeed(29);
        MultivariatePolynomial<BigInteger>
                a = parse("5*b^6*c^15*d^3+4*b^8*c*d^11+17492152*a^2*b^8*c^15*d^10+8*a^2*b^10*d^11+9*a^3*b^2*c^10*d+5*a^4*b*c^5*d^3+6*a^4*b^13*c^3*d^13+17492156*a^8*b^6*c^12*d^4+5*a^9*b^9*d^11+5*a^10*b^6*c^15*d^10"),
                b = parse("b^8*d^3+a^4*b^2*c^7*d+4*a^5*d^2+4*a^5*b^6*c+17492153*a^7*c^8*d^6"),
                gcd = parse("7*a^2*b^7*c^9*d^6+17492158*a^2*b^8*c*d^9+4*a^2*b^9*c^7*d^3+3*a^3*d+7*a^3*b^2*c^2*d^2+4*a^3*b^2*c^2*d^3+17492156*a^3*b^9*c^9*d^3+a^5*b^6*c^9*d^2+17492153*a^6*b^8*c^3*d^3+8*a^9*b^3*c^6*d^8+9*a^9*b^6*c^4*d^5");
        assertEquals(gcd, ModularGCD(a.clone().multiply(gcd), b.clone().multiply(gcd)));
    }

    @Test
    public void testModularGCD4() throws Exception {
        PrivateRandom.getRandom().setSeed(29);
        MultivariatePolynomial<BigInteger>
                a = parse("4*a*b^7*c*d^2+a^2*b^6*c^8*d^4+7*a^3*b^5*c^6*d^4+5*a^4*b^3*c*d^7+6*a^5*b^4*c^7+8*a^7*c^8*d+a^8*b^5*c^3*d^2"),
                b = parse("25987600*b^18*c^17*d^14+25987597*a*b^9*c^9*d^2+2*a^2*b^7*c^12*d^7+4*a^4*b^14*c^11*d^2+6*a^6*b^2*d+2*a^6*b^3*c^16*d^14+5*a^9*b^17*c^16*d^2+a^14*b^18*c^17*d^4"),
                gcd = parse("25987593*a^4*c^4*d^4+9*a^6*c^3*d^10+7*a^6*b^14*c^4*d^7+8*a^7*b^9*c^13*d+7*a^9*b^2*c^13*d^4+2*a^10*b^6*c^9*d^7+2*a^11*b^5*c^7*d^3+2*a^11*b^12*c^13*d^14+7*a^14*b^8*c^14*d^3+6*a^14*b^13*c^4*d^11");
        assertEquals(gcd, ModularGCD(a.clone().multiply(gcd), b.clone().multiply(gcd)));
    }

    @Test
    public void testModularGCD5() throws Exception {
        PrivateRandom.getRandom().setSeed(29);
        MultivariatePolynomial<BigInteger>
                a = parse("5*a*b^5*c^10*d^7*e^16+2*a^4*b^3*c^9*d^6*e^8+5*a^4*b^6*c^16*d^11*e^2+a^4*b^13*d^5*e^6+30844060*a^5*b*c^9*d^8*e^12+4*a^8*b*c^17*d^11*e^3+9*a^8*b^13*c^16*d^17*e^11+a^9*b^2*c^2*d^10*e^14+5*a^9*b^6*c^3*d^7*e^4+7*a^9*b^8*c^3*d^16*e^2+9*a^14*b^5*c^2*d^3*e^16"),
                b = parse("7*b^6*c^18*d^5*e+30844053*a^2*b^8*c^10*d^8*e^6+a^3*b^14*c^4*d^11*e^7+a^4*b^10*c*d^15*e^18+3*a^15*b^9*c^3*e^11+5*a^18*b^13*c^16*d^15*e^15"),
                gcd = parse("9*a^3*b^11*c^7*d^4*e^6+30844059*a^5*b^6*c^15*d^8*e^10+8*a^5*b^10*c^15*d^2*e^9+5*a^10*b^11*c^7*d^9*e^16+2*a^13*b^3*c^13*d^6*e^2+30844060*a^14*b^3*c^6*d^3*e^13+30844055*a^14*b^6*c^4*d^13+30844055*a^14*b^17*c^2*d^8*e^13+2*a^17*b^5*c^7*d*e^11");
        assertTrue(dividesQ(ModularGCD(a.clone().multiply(gcd), b.clone().multiply(gcd)), gcd));
    }

    @Test
    public void testModularGCD6() throws Exception {
        for (int i = 46; i < 100; i++) {
            PrivateRandom.getRandom().setSeed(46);
            MultivariatePolynomial<BigInteger>
                    a = parse("8*a*c^5*d^10*e^5+31118523*a*b^3*c^5*d^8*e^10+a^2*b^7*c*d*e^12+4*a^2*b^8*c*d^9*e^10+31118524*a^3*b^5*c^14*d^5*e^13+31118529*a^4*b^3*c^12*d^6*e^8+3*a^5*b^4*d^11*e^9+31118526*a^5*b^8*c^6*d^12*e+4*a^7*b^13*c^11*d^3+31118529*a^9*b^12*c^4*d^2*e^11+5*a^11*b^9*c^2*d*e^11+8*a^13*b^13*c^7*d^2*e^8+8*a^14*b^5*c^14*d^6*e^4"),
                    b = parse("31118526*c^3*d^4*e^2+31118530*b^4*c^6*d^5*e^6+5*a*b*c^4*d^4*e^3+31118527*a*b^3*d*e^2+31118525*a^2*b*c^7*d*e^4+5*a^2*b^4*c^8*d^2*e^5+6*a^2*b^6*d^7*e^5+9*a^2*b^7*c^8*d*e^5+4*a^4*b^6*e^7+3*a^5*b^2*c^6*d^4*e^3+31118529*a^7*b*c^2*d^5*e^8+8*a^7*b^3*c^3*d^4*e^5+7*a^8*b*c^2*d^5*e^8+6*a^8*b^3*c^3*d^5*e^3"),
                    gcd = parse("2*c^3*d*e^5+31118524*b^6*c^2*d^3*e^4+31118528*a^2*b^3*c^2*d^3+7*a^3*b*c^3*d^2+5*a^3*b^3*c^4*d^5*e^2+31118527*a^4*c^2*d^3+7*a^4*b*c*d*e^4+9*a^4*b*c^6*d^3*e^4+5*a^5*d^2*e^2+4*a^6*b^2*c^4*e+7*a^6*b^3*c^5*d^4*e^3");
            assertTrue(dividesQ(ModularGCD(a.clone().multiply(gcd), b.clone().multiply(gcd)), gcd));
        }
    }

    @Test
    public void testModularGCD7() throws Exception {
        PrivateRandom.getRandom().setSeed(46);
        MultivariatePolynomial<BigInteger>
                a = parse("5*b*d^4+2*a^3*b*c*d^4+a^5*b^2*c^2*d^6+6*a^5*b^5*c^3*d^6+4*a^6*b*c^2+8*a^6*b^5*c^5*d^5"),
                b = parse("8*a*b*c^3*d^6+4*a*b^4*c*d+4*a*b^5*c*d^3+3*a^3*b^4*c^2"),
                gcd = parse("5*a^7*b^2*c^13*d^4");
        assertTrue(dividesQ(ModularGCD(a.clone().multiply(gcd), b.clone().multiply(gcd)), gcd));
    }

    @Test
    public void testModularGCD_random1() throws Exception {
        int nIterations = its(1000, 3000);
        RandomGenerator rnd = getRandom();
        TripletPort sampleData = new TripletPort(3, 5, 5, 15, 5, 15, rnd);
        for (int n = 0; n < nIterations; n++) {
            PrivateRandom.getRandom().setSeed(rnd.nextLong());
            if (n % 100 == 0)
                System.out.println(n);
            GCDTriplet gcdTriplet = sampleData.nextSample(false, false).asZ();
            MultivariatePolynomial<BigInteger> actualGCD = null;
            try {
                actualGCD = ModularGCD(gcdTriplet.aGCD, gcdTriplet.bGCD);
                assertTrue(dividesQ(actualGCD, gcdTriplet.gcd));
            } catch (Throwable thr) {
                System.out.println(n);
                System.out.println(gcdTriplet.domain);
                System.out.println("a   : " + gcdTriplet.a);
                System.out.println("b   : " + gcdTriplet.b);
                System.out.println("gcd : " + gcdTriplet.gcd);
                System.out.println("err : " + actualGCD);
                throw thr;
            }
        }
    }

    @Ignore
    @Test
    public void testModularGCD_performance() throws Exception {
        PrivateRandom.getRandom().setSeed(1232);
        String[] vars = {"a", "b", "c", "d", "e"};
        Domain<BigInteger> domain = Integers;
        MultivariatePolynomial<BigInteger>
                a = parse("2147483167*a^4*b^60*c^57*d^26*e+44*a^8*b^39*c^67*d^22*e^17+38*a^32*b^6*c^13*d^10*e^3+357*a^36*b^34*c^60*d^2*e^59+563*a^42*b^41*c^45*d^52*e^14+257*a^44*b^68*c^43*d^2*e^73+613*a^48*b^55*c^22*d^32*e^19+2147483093*a^52*b^26*c^4*d^72*e^32+19*a^52*b^40*c^26*d^45*e^55+639*a^55*b^72*c^55*d^65", domain, LEX, vars),
                b = parse("2147483150*b^25*c^18*d^62*e^59+2147482723*a^4*b^5*c^65*d^26*e^7+261*a^15*b^60*c^59*d^63*e^53+394*a^27*b^22*c^34*d^54*e^13+952*a^39*b^48*c^17*d^54*e^16+243*a^60*b^15*c^3*d^51*e^46+40*a^61*b^56*c^39*d^40*e^21+555*a^62*b^20*c^20*d^60*e^47+627*a^67*b^8*c^22*d^67*e^61+447*a^70*b^59*c^71*d^24*e^5", domain, LEX, vars),
                gcd = parse("35*a*b^36*c^74*d^62*e^51+376*a^2*b^28*c^64*e^53+893*a^6*b^13*c^60*d^44*e^42+23*a^8*b^71*c^40*d^36*e^11+783*a^20*b^28*c^12*d^31*e^68+2147482938*a^31*b^30*c^40*d^65*e^72+2147482960*a^31*b^49*c^38*d^71*e^55+737*a^47*b^15*c^71*d^13*e^72+868*a^53*b^30*c^40*d^29*e^46+898*a^61*b^71*c^13*d^50*e^66", domain, LEX, vars);

        a = a.clone().multiply(gcd);
        b = b.clone().multiply(gcd);
        System.out.println(a);
        System.out.println(b);

        for (int i = 0; i < 1000; i++) {
            long start = System.nanoTime();
            System.out.println(ModularGCD(a.clone().increment(), b));
//            assertTrue(dividesQ(ModularGCD(a, b), gcd));
            System.out.println(TimeUnits.nanosecondsToString(System.nanoTime() - start));
        }
    }

    private static final class GCDTriplet {
        final MultivariatePolynomial<BigInteger> a, b, gcd, aGCD, bGCD;
        final Domain<BigInteger> domain;

        public GCDTriplet(MultivariatePolynomial<BigInteger> a, MultivariatePolynomial<BigInteger> b, MultivariatePolynomial<BigInteger> gcd) {
            this.a = a;
            this.b = b;
            this.gcd = gcd;
            this.domain = a.domain;
            this.aGCD = a.clone().multiply(gcd);
            this.bGCD = b.clone().multiply(gcd);
        }

        private GCDTriplet(MultivariatePolynomial<BigInteger> a, MultivariatePolynomial<BigInteger> b, MultivariatePolynomial<BigInteger> gcd, MultivariatePolynomial<BigInteger> aGCD, MultivariatePolynomial<BigInteger> bGCD, Domain<BigInteger> domain) {
            this.a = a;
            this.b = b;
            this.gcd = gcd;
            this.aGCD = aGCD;
            this.bGCD = bGCD;
            this.domain = domain;
        }

        public GCDTriplet evaluate(int variable, BigInteger value) {
            return new GCDTriplet(a.evaluate(variable, value), b.evaluate(variable, value), gcd.evaluate(variable, value),
                    aGCD.evaluate(variable, value), bGCD.evaluate(variable, value), domain);
        }

        GCDTriplet asZ() {
            return new GCDTriplet(a.setDomain(Integers), b.setDomain(Integers), gcd.setDomain(Integers));
        }
    }

    private static final class TripletPort {
        final int nVarsMin, nVarsMax,
                minDegree, maxDegree,
                minSize, maxSize;
        final RandomGenerator rnd;
        final RandomDataGenerator rndd;

        public TripletPort(int nVarsMin, int nVarsMax, int minDegree, int maxDegree, int minSize, int maxSize, RandomGenerator rnd) {
            this.nVarsMin = nVarsMin;
            this.nVarsMax = nVarsMax;
            this.minDegree = minDegree;
            this.maxDegree = maxDegree;
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.rnd = rnd;
            this.rndd = new RandomDataGenerator(rnd);
        }

        long counter = 0;

        public GCDTriplet nextSample(boolean primitive, boolean monic) {
            PrivateRandom.getRandom().setSeed(counter++);
            long modulus = getModulusRandom(25);
            IntegersModulo domain = new IntegersModulo(modulus);
            BigInteger bound = BigInteger.valueOf(10);

            int nVariables = rndd.nextInt(nVarsMin, nVarsMax);
            MultivariatePolynomial<BigInteger>
                    a = randomPolynomial(nVariables, rndd.nextInt(minDegree, maxDegree), rndd.nextInt(minSize, maxSize), bound, domain, LEX, rnd),
                    b = randomPolynomial(nVariables, rndd.nextInt(minDegree, maxDegree), rndd.nextInt(minSize, maxSize), bound, domain, LEX, rnd),
                    gcd = randomPolynomial(nVariables, rndd.nextInt(minDegree, maxDegree), rndd.nextInt(minSize, maxSize), bound, domain, LEX, rnd);


            if (primitive) {
                a = asNormalMultivariate(a.asOverUnivariate(0).primitivePart(), 0);
                b = asNormalMultivariate(b.asOverUnivariate(0).primitivePart(), 0);
                gcd = asNormalMultivariate(gcd.asOverUnivariate(0).primitivePart(), 0);
            }
            if (monic) {
                a.add(new MonomialTerm<>(a.nVariables, 0, a.degree() + 1, BigInteger.ONE));
                b.add(new MonomialTerm<>(a.nVariables, 0, b.degree() + 1, BigInteger.ONE));
                gcd.add(new MonomialTerm<>(a.nVariables, 0, gcd.degree() + 1, BigInteger.ONE));
            }
            return new GCDTriplet(a, b, gcd);
        }
    }

    @SuppressWarnings("unchecked")
    private static void checkConsistency(MultivariatePolynomial... polys) {
        Arrays.stream(polys).forEach(MultivariateGCDTest::checkConsistency);
    }

    private static <E> void checkConsistency(MultivariatePolynomial<E> poly) {
        Domain<E> domain = poly.domain;
        for (MonomialTerm<E> e : poly.terms) {
            E value = e.coefficient;
            assertFalse(domain.isZero(value));
            assertTrue(value == domain.valueOf(value));
            if (domain instanceof IntegersModulo) {
                assertTrue(domain.signum(value) > 0);
                assertTrue(((BigInteger) value).compareTo(((IntegersModulo) domain).modulus) <= 0);
            }
        }
    }

//    @Test
//    public void trash() throws Exception {
//        String[] vars = {"a", "b", "c"};
//        IntegersModulo domain = new IntegersModulo(BigPrimes.nextPrime(56423421232L));
//        MultivariatePolynomial<BigInteger>
//                a = parse("5*a^1123*c^2+5*a^2*b^2*c^2+5*a^2*b^4*c^3+9*a^2213*b^523*c^5+25709547*a^3*b^6*c^611+8*a^4*b*c^3+a^4*b^3*c+5*a^4*b^3*c^6+a^1500", domain, GREVLEX, vars),
//                b = parse("3*a*b^2*c^2+2*a^2*b^421+25709540*a^4*b*c^6+7*a^5*c^1232+8*a^6*b^876*c^3+a^1500", domain, GREVLEX, vars),
//                gcd = parse("5*b^2*c^6+2*a^412*b^4*c^5+25709543*a^5*b^892*c^512+9*a^6*c+25709540*a^6*c^3+a^1500", domain, GREVLEX, vars);
//        RandomGenerator rnd = getRandom();
//
//        int variable = a.nVariables - 1;
//        a = fromZp(convertZp(a, 0).primitivePart(), domain, 0);
//        b = fromZp(convertZp(b, 0).primitivePart(), domain, 0);
//        gcd = fromZp(convertZp(gcd, 0).primitivePart(), domain, 0);
//
//        gcd = gcd.monic();
//        a = a.clone().monic().multiply(gcd);
//        b = b.clone().monic().multiply(gcd);
//
//
//        System.out.println(a);
//        System.out.println(b);
//        System.out.println(domain.modulus);
//        System.out.println(Zippel(a, b));
//    }
}