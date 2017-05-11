package cc.r2.core.poly.univar2;

import cc.r2.core.combinatorics.IntCombinationsGenerator;
import cc.r2.core.number.BigInteger;
import cc.r2.core.number.primes.BigPrimes;
import cc.r2.core.number.primes.SmallPrimes;
import cc.r2.core.poly.IntegersDomain;
import cc.r2.core.poly.LongArithmetics;
import cc.r2.core.poly.ModularDomain;
import cc.r2.core.poly.univar2.HenselLifting.QuadraticLiftAbstract;
import cc.r2.core.util.ArraysUtil;

import java.util.List;

import static cc.r2.core.poly.LongArithmetics.fits32bitWord;
import static cc.r2.core.poly.LongArithmetics.safeMultiply;
import static cc.r2.core.poly.univar2.DistinctDegreeFactorization.DistinctDegreeFactorization;
import static cc.r2.core.poly.univar2.EqualDegreeFactorization.CantorZassenhaus;
import static cc.r2.core.poly.univar2.SquareFreeFactorization.SquareFreeFactorization;
import static cc.r2.core.poly.univar2.gMutablePolynomial.*;

/**
 * Factorization of univariate polynomials.
 *
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class Factorization {
    private Factorization() {}

    /* ************************** Factorization in Zp[x] ************************** */

    static void ensureFiniteFieldDomain(IMutablePolynomial poly) {
        if (!poly.isOverFiniteField())
            throw new IllegalArgumentException("Polynomial over finite field is expected, but got " + poly.getClass());
    }

    /** x^n * poly */
    private static final class FactorMonomial<T extends IMutablePolynomial<T>> {
        final T theRest, monomial;

        FactorMonomial(T theRest, T monomial) {
            this.theRest = theRest;
            this.monomial = monomial;
        }
    }

    /** factor out common monomial term (x^n) */
    private static <Poly extends IMutablePolynomial<Poly>> FactorMonomial<Poly> factorOutMonomial(Poly poly) {
        int i = poly.firstNonZeroCoefficientPosition();

        if (i == 0)
            return new FactorMonomial<>(poly, poly.createOne());
        return new FactorMonomial<>(poly.clone().shiftLeft(i), poly.createMonomial(i));
    }

    /** early check for trivial cases */
    private static <Poly extends IMutablePolynomial<Poly>> FactorDecomposition<Poly> earlyFactorizationChecks(Poly poly) {
        if (poly.degree() <= 1 || poly.isMonomial())
            return FactorDecomposition.oneFactor(poly.lcAsPoly(), poly.isMonic() ? poly : poly.clone().monic());

        return null;
    }

    /**
     * Factors polynomial in Zp[x].
     *
     * @param poly the polynomial
     * @return factor decomposition
     */
    public static <Poly extends IMutablePolynomial<Poly>> FactorDecomposition<Poly> factorZp(Poly poly) {
        ensureFiniteFieldDomain(poly);
        FactorDecomposition<Poly> result = earlyFactorizationChecks(poly);
        if (result != null)
            return result;
        result = FactorDecomposition.empty(poly);
        factorZp(poly, result);
        return result.setConstantFactor(poly.lcAsPoly());
    }

    private static <T extends IMutablePolynomial<T>> void factorZp(T poly, FactorDecomposition<T> result) {
        FactorMonomial<T> base = factorOutMonomial(poly);
        result.addFactor(base.monomial, 1);

        //do square-free factorization
        FactorDecomposition<T> sqf = SquareFreeFactorization(base.theRest);
        for (int i = 0; i < sqf.size(); ++i) {
            //for each square-free factor
            T sqfFactor = sqf.get(i);
            int sqfExponent = sqf.getExponent(i);

            //do distinct-degree factorization
            FactorDecomposition<T> ddf = DistinctDegreeFactorization(sqfFactor);
            for (int j = 0; j < ddf.size(); ++j) {
                //for each distinct-degree factor
                T ddfFactor = ddf.get(j);
                int ddfExponent = ddf.getExponent(j);

                //do equal-degree factorization
                FactorDecomposition<T> edf = CantorZassenhaus(ddfFactor, ddfExponent);
                for (T irreducibleFactor : edf.factors)
                    //put final irreducible factor into the result
                    result.addFactor(irreducibleFactor.monic(), sqfExponent);
            }
        }
    }

    /* ************************** Factorization in Z[x] ************************** */


    /** assertion for correct Hensel structure */
    static <T extends IMutablePolynomial<T>> void assertHenselLift(QuadraticLiftAbstract<T> lift) {
        assert lift.polyMod().equals(lift.aFactor.clone().multiply(lift.bFactor)) : lift.toString();
        assert (lift.aCoFactor == null && lift.bCoFactor == null)
                || lift.aFactor.clone().multiply(lift.aCoFactor).add(lift.bFactor.clone().multiply(lift.bCoFactor)).isOne() : lift.toString();
    }

    /** cache of references **/
    private static int[][] naturalSequenceRefCache = new int[32][];

    private static int[] createSeq(int n) {
        int[] r = new int[n];
        for (int i = 0; i < n; i++)
            r[i] = i;
        return r;
    }

    /** returns sequence of natural numbers */
    private static int[] naturalSequenceRef(int n) {
        if (n >= naturalSequenceRefCache.length)
            return createSeq(n);
        if (naturalSequenceRefCache[n] != null)
            return naturalSequenceRefCache[n];
        return naturalSequenceRefCache[n] = createSeq(n);
    }

    /** select elements by their positions */
    private static int[] select(int[] data, int[] positions) {
        int[] r = new int[positions.length];
        int i = 0;
        for (int p : positions)
            r[i++] = data[p];
        return r;
    }

    /** reconstruct true factors by enumerating all combinations */
    static FactorDecomposition<lMutablePolynomialZ> reconstructFactorsZ(
            lMutablePolynomialZ poly,
            FactorDecomposition<lMutablePolynomialZp> modularFactors) {

        if (modularFactors.isTrivial())
            return FactorDecomposition.oneFactor(1, poly);

        lMutablePolynomialZp factory = modularFactors.get(0);

        int[] modIndexes = naturalSequenceRef(modularFactors.size());
        FactorDecomposition<lMutablePolynomialZ> trueFactors = FactorDecomposition.empty(poly);
        lMutablePolynomialZ fRest = poly;
        int s = 1;

        factor_combinations:
        while (2 * s <= modIndexes.length) {
            IntCombinationsGenerator combinations = new IntCombinationsGenerator(modIndexes.length, s);
            for (int[] combination : combinations) {
                int[] indexes = select(modIndexes, combination);

                lMutablePolynomialZp mFactor = factory.createConstant(fRest.lc());
                for (int i : indexes)
                    mFactor = mFactor.multiply(modularFactors.get(i));
                lMutablePolynomialZ factor = mFactor.normalSymmetricForm().primitivePart();

                if (fRest.lc() % factor.lc() != 0 || fRest.cc() % factor.cc() != 0)
                    continue;

                lMutablePolynomialZp mRest = factory.createConstant(fRest.lc() / factor.lc());
                int[] restIndexes = ArraysUtil.intSetDifference(modIndexes, indexes);
                for (int i : restIndexes)
                    mRest = mRest.multiply(modularFactors.get(i));
                lMutablePolynomialZ rest = mRest.normalSymmetricForm().primitivePart();

                if (safeMultiply(factor.lc(), rest.lc()) != fRest.lc()
                        || safeMultiply(factor.cc(), rest.cc()) != fRest.cc())
                    continue;
                if (rest.clone().multiplyUnsafe(factor).equals(fRest)) {
                    modIndexes = restIndexes;
                    trueFactors.addFactor(factor, 1);
                    fRest = rest.primitivePart();
                    continue factor_combinations;
                }
            }
            ++s;
        }

        if (!fRest.isConstant())
            trueFactors.addFactor(fRest, 1);

        return trueFactors;
    }


    /** reconstruct true factors by enumerating all combinations */
    static FactorDecomposition<gMutablePolynomial<BigInteger>> reconstructFactorsZ(
            gMutablePolynomial<BigInteger> poly,
            FactorDecomposition<gMutablePolynomial<BigInteger>> modularFactors) {

        if (modularFactors.isTrivial())
            return FactorDecomposition.oneFactor(poly.createOne(), poly);

        gMutablePolynomial<BigInteger> factory = modularFactors.get(0);

        int[] modIndexes = naturalSequenceRef(modularFactors.size());
        FactorDecomposition<gMutablePolynomial<BigInteger>> trueFactors = FactorDecomposition.empty(poly);
        gMutablePolynomial<BigInteger> fRest = poly;
        int s = 1;

        factor_combinations:
        while (2 * s <= modIndexes.length) {
            IntCombinationsGenerator combinations = new IntCombinationsGenerator(modIndexes.length, s);
            for (int[] combination : combinations) {
                int[] indexes = select(modIndexes, combination);

                gMutablePolynomial<BigInteger> mFactor = factory.createConstant(fRest.lc());
                for (int i : indexes)
                    mFactor = mFactor.multiply(modularFactors.get(i));
                gMutablePolynomial<BigInteger> factor = asPolyZSymmetric(mFactor).primitivePart();

                if (!fRest.lc().remainder(factor.lc()).isZero() || !fRest.cc().remainder(factor.cc()).isZero())
                    continue;

                gMutablePolynomial<BigInteger> mRest = factory.createConstant(fRest.lc().divide(factor.lc()));
                int[] restIndexes = ArraysUtil.intSetDifference(modIndexes, indexes);
                for (int i : restIndexes)
                    mRest = mRest.multiply(modularFactors.get(i));
                gMutablePolynomial<BigInteger> rest = asPolyZSymmetric(mRest).primitivePart();

                if (!factor.lc().multiply(rest.lc()).equals(fRest.lc())
                        || !factor.cc().multiply(rest.cc()).equals(fRest.cc()))
                    continue;
                if (rest.clone().multiply(factor).equals(fRest)) {
                    modIndexes = restIndexes;
                    trueFactors.addFactor(factor, 1);
                    fRest = rest.primitivePart();
                    continue factor_combinations;
                }
            }
            ++s;
        }

        if (!fRest.isConstant())
            trueFactors.addFactor(fRest, 1);

        return trueFactors;
    }

    private static final double
            MAX_PRIME_GAP = 382,
            MIGNOTTE_MAX_DOUBLE_32 = (2.0 * Integer.MAX_VALUE) - 10 * MAX_PRIME_GAP,
            MIGNOTTE_MAX_DOUBLE_64 = MIGNOTTE_MAX_DOUBLE_32 * MIGNOTTE_MAX_DOUBLE_32;

    private static final int
            LOWER_RND_MODULUS_BOUND = 1 << 24,
            UPPER_RND_MODULUS_BOUND = 1 << 30;

    private static int randomModulusInf() {
        return LOWER_RND_MODULUS_BOUND + PrivateRandom.getRandom().nextInt(UPPER_RND_MODULUS_BOUND - LOWER_RND_MODULUS_BOUND);
    }

    private static int next32BitPrime(int val) {
        if (val < 0) {
            long l = BigPrimes.nextPrime(Integer.toUnsignedLong(val));
            assert fits32bitWord(l);
            return (int) l;
        } else
            return SmallPrimes.nextPrime(val);
    }

    final static int N_MODULAR_FACTORIZATION_TRIALS = 2;

    /**
     * Factors primitive square-free polynomial using Hensel lifting
     *
     * @param poly Z[x] primitive square-free polynomial
     */
    static FactorDecomposition<gMutablePolynomial<BigInteger>> factorSquareFree(gMutablePolynomial<BigInteger> poly) {
        assert poly.content().isOne();
        assert poly.lc().signum() > 0;

        BigInteger bound2 = BigInteger.TWO.multiply(mignotteBound(poly)).multiply(poly.lc().abs());
        if (bound2.compareTo(LongArithmetics.b_MAX_SUPPORTED_MODULUS) < 0)
            return FactorDecomposition.convert(factorSquareFree(asLongPolyZ(poly)));

        // choose prime at random
        long modulus = -1;
        lMutablePolynomialZp moduloImage;
        FactorDecomposition<lMutablePolynomialZp> lModularFactors = null;

        for (int attempt = 0; attempt < N_MODULAR_FACTORIZATION_TRIALS; attempt++) {
            long tmpModulus;
            do {
                tmpModulus = SmallPrimes.nextPrime(randomModulusInf());
                moduloImage = asLongPolyZp(poly.setDomain(new ModularDomain(tmpModulus)));
            }
            while (moduloImage.cc() == 0 || moduloImage.degree() != poly.degree() || !SquareFreeFactorization.isSquareFree(moduloImage));

            // do modular factorization
            FactorDecomposition<lMutablePolynomialZp> tmpFactors = factorZp(moduloImage.monic());
            if (tmpFactors.size() == 1)
                return FactorDecomposition.oneFactor(poly.createOne(), poly);

            if (lModularFactors == null || lModularFactors.size() > tmpFactors.size()) {
                lModularFactors = tmpFactors;
                modulus = tmpModulus;
            }

            if (lModularFactors.size() <= 3)
                break;
        }

        List<gMutablePolynomial<BigInteger>> modularFactors = HenselLifting.liftFactorization(BigInteger.valueOf(modulus), bound2, poly, lModularFactors.factors);
        assert modularFactors.get(0).domain.size().compareTo(bound2) >= 0;
        return reconstructFactorsZ(poly, FactorDecomposition.create(modularFactors));
    }

    /** determines the lower bound for the possible modulus for Zp trials */
    private static int chooseModulusLowerBound(double bound2) {
        long infinum;
        if (bound2 < MIGNOTTE_MAX_DOUBLE_32) {
            // we can use single 32-bit modulus
            infinum = (long) bound2;
        } else if (bound2 < MIGNOTTE_MAX_DOUBLE_64) {
            // we still can use machine-size words
            // two options possible:
            // 1) use 64-bit "(long) bound2" as modulus (which is more than 32-bit and thus slow)
            // 2) use 32-bit modulus and perform a single Hensel step
            // we use 2) option

            infinum = (long) Math.sqrt(bound2);
        } else {
            // coefficient bound is large -> we anyway need several Hensel steps
            // so we just pick 32-bit prime at random and must use BigInteger arithmetics

            throw new IllegalArgumentException();
            //infinum = randomModulusInf();
        }

        assert infinum < MIGNOTTE_MAX_DOUBLE_32;
        return (int) infinum;
    }

    /**
     * Factors square-free polynomial using Hensel lifting
     *
     * @param poly Z[x] square-free polynomial
     */
    static FactorDecomposition<lMutablePolynomialZ> factorSquareFree(lMutablePolynomialZ poly) {
        assert poly.content() == 1;
        assert poly.lc() > 0;

        long lc = poly.lc();
        double bound2 = 2.0 * poly.mignotteBound() * Math.abs(lc);
        // choose prime at random
        int trial32Modulus = chooseModulusLowerBound(bound2) - 1;
        long modulus;
        lMutablePolynomialZp moduloImage;
        do {
            trial32Modulus = next32BitPrime(trial32Modulus + 1);
            modulus = Integer.toUnsignedLong(trial32Modulus);
            moduloImage = poly.modulus(modulus, true);
        } while (!SquareFreeFactorization.isSquareFree(moduloImage));

        // do modular factorization
        FactorDecomposition<lMutablePolynomialZp> modularFactors = factorZp(moduloImage.monic());

        // do Hensel lifting
        // determine number of Hensel steps
        int henselIterations = 0;
        long liftedModulus = modulus;
        while (liftedModulus < bound2) {
            liftedModulus = LongArithmetics.safeMultiply(liftedModulus, liftedModulus);
            ++henselIterations;
        }
        // actual lift
        if (henselIterations > 0)
            modularFactors = FactorDecomposition.create(modularFactors.constantFactor, HenselLifting.liftFactorization(modulus, liftedModulus, henselIterations, poly, modularFactors.factors, true));

        //reconstruct true factors
        return reconstructFactorsZ(poly, modularFactors);
    }

    @SuppressWarnings("unchecked")
    private static <PolyZ extends IMutablePolynomial<PolyZ>> FactorDecomposition<PolyZ> factorSquareFree(PolyZ poly) {
        if (poly instanceof lMutablePolynomialZ)
            return (FactorDecomposition<PolyZ>) factorSquareFree((lMutablePolynomialZ) poly);
        else
            return (FactorDecomposition<PolyZ>) factorSquareFree((gMutablePolynomial) poly);
    }

    private static void ensureIntegersDomain(IMutablePolynomial poly) {
        if (poly instanceof lMutablePolynomialZ ||
                (poly instanceof gMutablePolynomial && ((gMutablePolynomial) poly).domain == IntegersDomain.IntegersDomain))
            return;
        throw new IllegalArgumentException("Not an integers domain for factorization in Z[x]");
    }

    /**
     * Factors polynomial in Z[x].
     *
     * @param poly the polynomial
     * @return factor decomposition
     */
    public static <Poly extends IMutablePolynomial<Poly>> FactorDecomposition<Poly> factorZ(Poly poly) {
        if (poly.degree() <= 1 || poly.isMonomial())
            return FactorDecomposition.oneFactor(
                    poly.isMonic() ? poly.createOne() : poly.contentAsPoly(),
                    poly.isMonic() ? poly : poly.clone().primitivePart());

        FactorDecomposition<Poly> result = FactorDecomposition.empty(poly);
        Poly content = poly.contentAsPoly();
        if (poly.signum() < 0)
            content = content.negate();
        factorZ(poly.clone().divideByLC(content), result);
        return result.setConstantFactor(content);
    }

    private static <T extends IMutablePolynomial<T>> void factorZ(T poly, FactorDecomposition<T> result) {
        FactorMonomial<T> base = factorOutMonomial(poly);
        result.addFactor(base.monomial, 1);

        //do square-free factorization
        FactorDecomposition<T> sqf = SquareFreeFactorization(base.theRest);
        for (int i = 0; i < sqf.size(); ++i) {
            //for each square-free factor
            T sqfFactor = sqf.get(i);
            int sqfExponent = sqf.getExponent(i);

            //do distinct-degree factorization
            FactorDecomposition<T> cz = factorSquareFree(sqfFactor);
            //do equal-degree factorization
            for (T irreducibleFactor : cz.factors)
                //put final irreducible factor into the result
                result.addFactor(irreducibleFactor, sqfExponent);
        }
    }
}
