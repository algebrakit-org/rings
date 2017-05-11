package cc.r2.core.poly.univar2;

import cc.r2.core.number.BigInteger;
import cc.r2.core.number.BigIntegerArithmetics;
import cc.r2.core.poly.Domain;
import cc.r2.core.poly.ModularDomain;
import cc.r2.core.util.ArraysUtil;

import java.util.Arrays;

import static cc.r2.core.number.BigInteger.ONE;
import static cc.r2.core.number.BigInteger.ZERO;
import static cc.r2.core.number.BigIntegerArithmetics.abs;
import static cc.r2.core.number.BigIntegerArithmetics.max;
import static cc.r2.core.poly.IntegersDomain.IntegersDomain;

/**
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class gMutablePolynomial<E> implements IMutablePolynomial<gMutablePolynomial<E>> {
    final Domain<E> domain;
    /** list of coefficients { x^0, x^1, ... , x^degree } */
    E[] data;
    /** points to the last non zero element in the data array */
    int degree;

    private gMutablePolynomial(Domain<E> domain, E[] data, int degree) {
        this.domain = domain;
        this.data = data;
        this.degree = degree;
    }

    private gMutablePolynomial(Domain<E> domain, E[] data) {
        this(domain, data, data.length - 1);
        fixDegree();
    }

    /**
     * Creates new univariate polynomial over specified domain
     *
     * @param domain the domain
     * @param data   the data
     * @return new univariate polynomial over specified domain with specified elements
     */
    public static <E> gMutablePolynomial<E> create(Domain<E> domain, E... data) {
        domain.setToValueOf(data);
        return new gMutablePolynomial<>(domain, data);
    }

    public static <E> gMutablePolynomial<E> createUnsafe(Domain<E> domain, E... data) {
        return new gMutablePolynomial<>(domain, data);
    }

    /**
     * Creates new univariate polynomial over specified domain
     *
     * @param domain the domain
     * @param data   the data
     * @return new univariate polynomial over specified domain with specified elements
     */
    public static gMutablePolynomial<BigInteger> create(Domain<BigInteger> domain, long... data) {
        return create(domain, domain.valueOf(data));
    }

    /**
     * Creates new univariate polynomial over Z
     *
     * @param data the data
     * @return new univariate polynomial over Z with specified elements
     */
    public static gMutablePolynomial<BigInteger> create(long... data) {
        return create(IntegersDomain, data);
    }

    /**
     * Converts poly over BigIntegers to machine-sized polynomial in Z
     *
     * @param poly the polynomial over BigIntegers
     * @return machine-sized polynomial in Z
     * @throws ArithmeticException if some of {@code poly} elements is out of long range
     */
    public static lMutablePolynomialZ asLongPolyZ(gMutablePolynomial<BigInteger> poly) {
        long[] data = new long[poly.degree + 1];
        for (int i = 0; i < data.length; i++)
            data[i] = poly.data[i].longValueExact();
        return lMutablePolynomialZ.create(data);
    }

    /**
     * Converts Z/p poly over BigIntegers to machine-sized polynomial in Z/p
     *
     * @param poly the Z/p polynomial over BigIntegers
     * @return machine-sized polynomial in Z/p
     * @throws IllegalArgumentException if {@code poly.domain} is not {@link ModularDomain}
     * @throws ArithmeticException      if some of {@code poly} elements is out of long range
     */
    public static lMutablePolynomialZp asLongPolyZp(gMutablePolynomial<BigInteger> poly) {
        if (!(poly.domain instanceof ModularDomain))
            throw new IllegalArgumentException("Not a modular domain: " + poly.domain);
        long[] data = new long[poly.degree + 1];
        for (int i = 0; i < data.length; i++)
            data[i] = poly.data[i].longValueExact();
        return lMutablePolynomialZp.create(((ModularDomain) poly.domain).modulus.longValueExact(), data);
    }


    /**
     * Returns Z[x] polynomial formed from the coefficients of the poly.
     *
     * @param poly the polynomial
     * @param copy whether to copy the internal data
     * @return Z[x] version of the poly
     */
    public static gMutablePolynomial<BigInteger> asPolyZ(gMutablePolynomial<BigInteger> poly, boolean copy) {
        return gMutablePolynomial.createUnsafe(IntegersDomain, copy ? poly.data.clone() : poly.data);
    }

    /**
     * Converts Zp[x] polynomial to Z[x] polynomial formed from the coefficients of this
     * represented in symmetric modular form ({@code -modulus/2 <= cfx <= modulus/2}).
     *
     * @param poly Zp polynomial
     * @return Z[x] version of the poly with coefficients represented in symmetric modular form ({@code -modulus/2 <= cfx <= modulus/2}).
     * @throws IllegalArgumentException is {@code poly.domain} is not a {@link ModularDomain}
     */
    public static gMutablePolynomial<BigInteger> asPolyZSymmetric(gMutablePolynomial<BigInteger> poly) {
        if (!(poly.domain instanceof ModularDomain))
            throw new IllegalArgumentException();
        ModularDomain domain = (ModularDomain) poly.domain;
        BigInteger[] newData = new BigInteger[poly.degree + 1];
        for (int i = poly.degree; i >= 0; --i)
            newData[i] = domain.symMod(poly.data[i]);
        return gMutablePolynomial.create(IntegersDomain, newData);
    }

    @Override
    public int degree() {return degree;}

    /**
     * Returns i-th element of this poly
     */
    public E get(int i) { return data[i];}

    @Override
    public int firstNonZeroCoefficientPosition() {
        int i = 0;
        while (domain.isZero(data[i])) ++i;
        assert i < data.length;
        return i;
    }

    /**
     * Set domain to a new domain and return a
     *
     * @param newDomain the new domain
     * @return a copy of this with specified new domain
     */
    public gMutablePolynomial<E> setDomain(Domain<E> newDomain) {
        E[] newData = Arrays.copyOf(data, degree + 1);
        newDomain.setToValueOf(newData);
        return new gMutablePolynomial<>(newDomain, newData);
    }

    public gMutablePolynomial<E> setDomainUnsafe(Domain<E> newDomain) {
        return new gMutablePolynomial<>(newDomain, data, degree);
    }

    /**
     * Returns the leading coefficient of the poly
     *
     * @return leading coefficient
     */
    public E lc() {return data[degree];}

    @Override
    public gMutablePolynomial<E> lcAsPoly() {return createConstant(lc());}

    /**
     * Returns the constant coefficient of the poly
     *
     * @return constant coefficient
     */
    public E cc() {return data[0];}

    /**
     * Ensures that the capacity of internal storage is enough for storing polynomial of the {@code desiredDegree}.
     * The degree of {@code this} is set to {@code desiredDegree} if the latter is greater than the former.
     *
     * @param desiredDegree desired degree
     */
    final void ensureCapacity(int desiredDegree) {
        if (degree < desiredDegree)
            degree = desiredDegree;

        if (data.length < (desiredDegree + 1)) {
            int oldLen = data.length;
            data = Arrays.copyOf(data, desiredDegree + 1);
            fillZeroes(data, oldLen, data.length);
        }
    }

    /**
     * Removes zeroes from the end of {@code data} and adjusts the degree
     */
    final void fixDegree() {
        int i = degree;
        while (i >= 0 && domain.isZero(data[i])) --i;
        if (i < 0) i = 0;

        if (i != degree) {
            degree = i;
            fillZeroes(data, degree + 1, data.length);
        }
    }

    @Override
    public gMutablePolynomial<E> getRange(int from, int to) {
        return new gMutablePolynomial<>(domain, Arrays.copyOfRange(data, from, to));
    }

    @Override
    @SuppressWarnings("unchecked")
    public gMutablePolynomial<E>[] arrayNewInstance(int length) {
        return new gMutablePolynomial[length];
    }

    @Override
    @SuppressWarnings("unchecked")
    public gMutablePolynomial<E>[] arrayNewInstance(gMutablePolynomial<E> a, gMutablePolynomial<E> b) {
        return new gMutablePolynomial[]{a, b};
    }

    @Override
    public void checkCompatible(gMutablePolynomial<E> oth) {
        if (!domain.equals(oth.domain))
            throw new IllegalArgumentException("Mixing polynomials from different domains: " + this.domain + " and " + oth.domain);
    }

    /**
     * Factory
     *
     * @param data the data
     * @return polynomial
     */
    public gMutablePolynomial<E> createFromArray(E[] data) {
        domain.setToValueOf(data);
        return new gMutablePolynomial<E>(domain, data);
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> createMonomial(int degree) {return createMonomial(domain.getOne(), degree);}

    /**
     * Creates linear polynomial of form {@code cc + x * lc}
     *
     * @param cc the  constant coefficient
     * @param lc the  leading coefficient
     * @return {@code cc + x * lc}
     */
    public gMutablePolynomial<E> createLinear(E cc, E lc) {
        return createFromArray(domain.createArray(cc, lc));
    }

    /**
     * Creates monomial {@code coefficient * x^degree}
     *
     * @param coefficient monomial coefficient
     * @param degree      monomial degree
     * @return {@code coefficient * x^degree}
     */
    public gMutablePolynomial<E> createMonomial(E coefficient, int degree) {
        coefficient = domain.valueOf(coefficient);
        E[] data = domain.createZeroesArray(degree + 1);
        data[degree] = coefficient;
        return new gMutablePolynomial<>(domain, data);
    }

    /**
     * Creates constant polynomial with specified value
     *
     * @param val the value
     * @return constant polynomial with specified value
     */
    public gMutablePolynomial<E> createConstant(E val) {
        E[] array = domain.createArray(1);
        array[0] = val;
        return createFromArray(array);
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> createZero() {return createConstant(domain.getZero());}

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> createOne() {return createConstant(domain.getOne());}

    @Override
    public boolean isZeroAt(int i) {return domain.isZero(data[i]);}

    /** {@inheritDoc} */
    @Override
    public boolean isZero() {return domain.isZero(data[degree]);}

    /** {@inheritDoc} */
    @Override
    public boolean isOne() {return degree == 0 && domain.isOne(data[0]);}

    /** {@inheritDoc} */
    @Override
    public boolean isMonic() {return domain.isOne(lc());}

    /** {@inheritDoc} */
    @Override
    public boolean isUnitCC() {return domain.isOne(cc());}

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {return degree == 0;}

    /** {@inheritDoc} */
    @Override
    public boolean isMonomial() {
        for (int i = degree - 1; i >= 0; --i)
            if (!domain.isZero(data[i]))
                return false;
        return true;
    }

    @Override
    public int signum() {
        return domain.signum(lc());
    }

    @Override
    public boolean isOverField() {
        return domain.isField();
    }

    @Override
    public boolean isOverFiniteField() {
        return domain.isFinite();
    }

    @Override
    public BigInteger domainCardinality() {
        return domain.size();
    }

    /**
     * Returns Mignotte's bound (sqrt(n+1) * 2^n max |this|) of the poly
     */
    public static BigInteger mignotteBound(gMutablePolynomial<BigInteger> poly) {
        return ONE.shiftLeft(poly.degree).multiply(norm2(poly));
    }

    /**
     * Returns L1 norm of the polynomial, i.e. sum of abs coefficients
     */
    public static BigInteger norm1(gMutablePolynomial<BigInteger> poly) {
        BigInteger norm = ZERO;
        for (int i = poly.degree; i >= 0; --i)
            norm = norm.add(abs(poly.data[i]));
        return norm;
    }

    /**
     * Returns L2 norm of the polynomial, i.e. a square root of a sum of coefficient squares.
     */
    public static BigInteger norm2(gMutablePolynomial<BigInteger> poly) {
        BigInteger norm = ZERO;
        for (int i = poly.degree; i >= 0; --i)
            norm = norm.add(poly.data[i].multiply(poly.data[i]));
        return BigIntegerArithmetics.sqrtCeil(norm);
    }

    /**
     * Returns L2 norm of the poly, i.e. a square root of a sum of coefficient squares.
     */
    public static double norm2Double(gMutablePolynomial<BigInteger> poly) {
        double norm = 0;
        for (int i = poly.degree; i >= 0; --i) {
            double d = poly.data[i].doubleValue();
            norm += d * d;
        }
        return Math.sqrt(norm);
    }

    /**
     * Returns max coefficient (by BigInteger's absolute value) of the poly
     */
    public static BigInteger normMax(gMutablePolynomial<BigInteger> poly) {
        return maxAbsCoefficient(poly);
    }

    /**
     * Returns max coefficient (by BigInteger's absolute value) of the poly
     */
    public static BigInteger maxAbsCoefficient(gMutablePolynomial<BigInteger> poly) {
        BigInteger max = abs(poly.data[0]);
        for (int i = poly.degree; i >= 0; --i)
            max = max(abs(poly.data[i]), max);
        return max;
    }

    private void fillZeroes(E[] data, int from, int to) {
        for (int i = from; i < to; ++i)
            data[i] = domain.getZero(); //invoke getZero() at each cycle
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> toZero() {
        fillZeroes(data, 0, degree + 1);
        degree = 0;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> set(gMutablePolynomial<E> oth) {
        this.data = oth.data.clone();
        this.degree = oth.degree;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> shiftLeft(int offset) {
        if (offset == 0)
            return this;
        if (offset > degree)
            return toZero();

        System.arraycopy(data, offset, data, 0, degree - offset + 1);
        fillZeroes(data, degree - offset + 1, degree + 1);
        degree = degree - offset;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> shiftRight(int offset) {
        if (offset == 0)
            return this;
        int degree = this.degree;
        ensureCapacity(offset + degree);
        System.arraycopy(data, 0, data, offset, degree + 1);
        fillZeroes(data, 0, offset);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> truncate(int newDegree) {
        if (newDegree >= degree)
            return this;
        fillZeroes(data, newDegree + 1, degree + 1);
        degree = newDegree;
        fixDegree();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> reverse() {
        ArraysUtil.reverse(data, 0, degree + 1);
        fixDegree();
        return this;
    }

    /**
     * Returns the content of the poly
     *
     * @return polynomial content
     */
    public E content() {
        if (degree == 0)
            return data[0];
        E gcd = data[degree];
        for (int i = degree - 1; i >= 0; --i)
            gcd = domain.gcd(gcd, data[i]);
        return gcd;
    }

    @Override
    public gMutablePolynomial<E> contentAsPoly() {
        return createConstant(content());
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> primitivePart() {
        E content = content();
        if (domain.signum(lc()) < 0)
            content = domain.negate(content);
        if (domain.isMinusOne(content))
            return negate();
        return primitivePart0(content);
    }

    @Override
    public gMutablePolynomial<E> primitivePartSameSign() {
        return primitivePart0(content());
    }

    private gMutablePolynomial<E> primitivePart0(E content) {
        if (domain.isOne(content))
            return this;
        for (int i = degree; i >= 0; --i) {
            data[i] = domain.divideOrNull(data[i], content);
            if (data[i] == null)
                return null;
        }
        return this;
    }

    /**
     * Evaluates this poly at a given {@code point} (via Horner method).
     *
     * @param point {@code point}
     * @return value at {@code point}
     */
    public E evaluate(long point) {
        return evaluate(domain.valueOf(point));
    }

    /**
     * Evaluates this poly at a given {@code point} (via Horner method).
     *
     * @param point {@code point}
     * @return value at {@code point}
     */
    public E evaluate(E point) {
        if (domain.isZero(point))
            return cc();

        point = domain.valueOf(point);
        E res = domain.getZero();
        for (int i = degree; i >= 0; --i)
            res = domain.add(domain.multiply(res, point), data[i]);
        return res;
    }

    /**
     * Add constant to this.
     *
     * @param val some number
     * @return this + val
     */
    public gMutablePolynomial<E> add(E val) {
        data[0] = domain.add(data[0], domain.valueOf(val));
        fixDegree();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> decrement() {
        return subtract(createOne());
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> increment() {
        return add(createOne());
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> add(gMutablePolynomial<E> oth) {
        if (oth.isZero())
            return this;
        if (isZero())
            return set(oth);

        checkCompatible(oth);
        ensureCapacity(oth.degree);
        for (int i = oth.degree; i >= 0; --i)
            data[i] = domain.add(data[i], oth.data[i]);
        fixDegree();
        return this;
    }

    /**
     * Adds {@code coefficient*x^exponent} to {@code this}
     *
     * @param coefficient monomial coefficient
     * @param exponent    monomial exponent
     * @return {@code this + coefficient*x^exponent}
     */
    public gMutablePolynomial<E> addMonomial(E coefficient, int exponent) {
        if (domain.isZero(coefficient))
            return this;

        ensureCapacity(exponent);
        data[exponent] = domain.add(data[exponent], domain.valueOf(coefficient));
        fixDegree();
        return this;
    }

    /**
     * Adds {@code oth * factor} to {@code this}
     *
     * @param oth    the polynomial
     * @param factor the factor
     * @return {@code this + oth * factor} modulo {@code modulus}
     */
    public gMutablePolynomial<E> addMul(gMutablePolynomial<E> oth, E factor) {
        if (oth.isZero())
            return this;

        factor = domain.valueOf(factor);
        if (domain.isZero(factor))
            return this;

        checkCompatible(oth);
        ensureCapacity(oth.degree);
        for (int i = oth.degree; i >= 0; --i)
            data[i] = domain.add(data[i], domain.multiply(factor, oth.data[i]));
        fixDegree();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> subtract(gMutablePolynomial<E> oth) {
        if (oth.isZero())
            return this;
        if (isZero())
            return set(oth).negate();

        checkCompatible(oth);
        ensureCapacity(oth.degree);
        for (int i = oth.degree; i >= 0; --i)
            data[i] = domain.subtract(data[i], oth.data[i]);
        fixDegree();
        return this;
    }

    /**
     * Subtracts {@code factor * x^exponent * oth} from {@code this}
     *
     * @param oth      the polynomial
     * @param factor   the factor
     * @param exponent the exponent
     * @return {@code this - factor * x^exponent * oth}
     */
    public gMutablePolynomial<E> subtract(gMutablePolynomial<E> oth, E factor, int exponent) {
        if (oth.isZero())
            return this;

        factor = domain.valueOf(factor);
        if (domain.isZero(factor))
            return this;

        checkCompatible(oth);
        for (int i = oth.degree + exponent; i >= exponent; --i)
            data[i] = domain.subtract(data[i], domain.multiply(factor, oth.data[i - exponent]));

        fixDegree();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public gMutablePolynomial<E> negate() {
        for (int i = degree; i >= 0; --i)
            if (!domain.isZero(data[i]))
                data[i] = domain.negate(data[i]);
        return this;
    }

    /**
     * Raises {@code this} by the {@code factor}
     *
     * @param factor the factor
     * @return {@code} this multiplied by the {@code factor}
     */
    public gMutablePolynomial<E> multiply(E factor) {
        factor = domain.valueOf(factor);
        if (domain.isOne(factor))
            return this;

        if (domain.isZero(factor))
            return toZero();

        for (int i = degree; i >= 0; --i)
            data[i] = domain.multiply(data[i], factor);
        return this;
    }

    @Override
    public gMutablePolynomial<E> multiply(long factor) {
        return multiply(domain.valueOf(factor));
    }

    @Override
    public gMutablePolynomial<E> divideByLC(gMutablePolynomial<E> other) {
        return divideOrNull(other.lc());
    }

    /**
     * Divides this polynomial by a {@code factor} or returns {@code null} (causing loss of internal data) if some of the elements can't be exactly
     * divided by the {@code factor}. NOTE: is {@code null} is returned, the content of {@code this} is destroyed.
     *
     * @param factor the factor
     * @return {@code this} divided by the {@code factor} or {@code null}
     */
    public gMutablePolynomial<E> divideOrNull(E factor) {
        if (domain.isZero(factor))
            throw new ArithmeticException("Divide by zero");
        if (domain.isOne(factor))
            return this;
        for (int i = degree; i >= 0; --i) {
            E l = domain.divideOrNull(data[i], factor);
            if (l == null)
                return null;
            data[i] = l;
        }
        return this;
    }

    @Override
    public gMutablePolynomial<E> monic() {
        return divideOrNull(lc());
    }

    public gMutablePolynomial<E> monic(E factor) {
        E lc = lc();
        return multiply(factor).divideOrNull(lc);
    }

    @Override
    @SuppressWarnings("unchecked")
    public gMutablePolynomial<E> multiply(gMutablePolynomial<E> oth) {
        if (isZero())
            return this;
        if (oth.isZero())
            return toZero();
        if (this == oth)
            return square();

        if (oth.degree == 0)
            return multiply(oth.data[0]);
        if (degree == 0) {
            E factor = data[0];
            data = oth.data.clone();
            degree = oth.degree;
            return multiply(factor);
        }

        if (domain instanceof ModularDomain) {
            // faster method with exact operations
            gMutablePolynomial<E>
                    iThis = setDomainUnsafe((Domain<E>) IntegersDomain),
                    iOth = oth.setDomainUnsafe((Domain<E>) IntegersDomain);
            data = iThis.multiply(iOth).data;
            domain.setToValueOf(data);
        } else
            data = multiplySafe0(oth);
        degree += oth.degree;
        fixDegree();
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public gMutablePolynomial<E> square() {
        if (isZero())
            return this;
        if (degree == 0)
            return multiply(data[0]);

        if (domain instanceof ModularDomain) {
            // faster method with exact operations
            gMutablePolynomial<E> iThis = setDomainUnsafe((Domain<E>) IntegersDomain);
            data = iThis.square().data;
            domain.setToValueOf(data);
        } else
            data = squareSafe0();
        degree += degree;
        fixDegree();
        return this;
    }

    @Override
    public gMutablePolynomial<E> derivative() {
        if (isConstant())
            return createZero();
        E[] newData = domain.createArray(degree);
        for (int i = degree; i > 0; --i)
            newData[i - 1] = domain.multiply(data[i], domain.valueOf(i));
        return createFromArray(newData);
    }

    @Override
    public gMutablePolynomial<E> clone() {
        return new gMutablePolynomial<>(domain, data.clone(), degree);
    }

    public E[] getDataReferenceUnsafe() {return data;}

    @Override
    public int compareTo(gMutablePolynomial<E> o) {
        int c = Integer.compare(degree, o.degree);
        if (c != 0)
            return c;
        for (int i = degree; i >= 0; --i) {
            c = domain.compare(data[i], o.data[i]);
            if (c != 0)
                return c;
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            if (domain.isZero(data[i]))
                continue;
            if (i != 0 && domain.isOne(data[i])) {
                if (sb.length() != 0)
                    sb.append("+");
                sb.append("x^").append(i);
            } else {
                String c = String.valueOf(data[i]);
                if (!c.startsWith("-") && sb.length() != 0)
                    sb.append("+");
                sb.append(c);
                if (i != 0)
                    sb.append("x^").append(i);
            }
        }

        if (sb.length() == 0)
            return "0";
        return sb.toString();
    }

    String toStringForCopy() {
        String s = ArraysUtil.toString(data, 0, degree + 1, x -> "new BigInteger(\"" + x + "\")");
        return "create(" + s.substring(1, s.length() - 1) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass())
            return false;
        @SuppressWarnings("unchecked")
        gMutablePolynomial<E> oth = (gMutablePolynomial<E>) obj;
        if (degree != oth.degree)
            return false;
        for (int i = 0; i <= degree; ++i)
            if (!(data[i].equals(oth.data[i])))
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = degree; i >= 0; --i)
            result = 31 * result + data[i].hashCode();
        return result;
    }

    /* =========================== Exact multiplication with safe arithmetics =========================== */


    /** switch to classical multiplication */
    static final long KARATSUBA_THRESHOLD = 1024L;
    /** when use Karatsuba fast multiplication */
    static final long
            MUL_CLASSICAL_THRESHOLD = 256L * 256L,
            MUL_MOD_CLASSICAL_THRESHOLD = 128L * 128L;

    /** switch algorithms */
    final E[] multiplySafe0(gMutablePolynomial<E> oth) {
        if (1L * (degree + 1) * (degree + 1) <= MUL_CLASSICAL_THRESHOLD)
            return multiplyClassicalSafe(data, 0, degree + 1, oth.data, 0, oth.degree + 1);
        else
            return multiplyKaratsubaSafe(data, 0, degree + 1, oth.data, 0, oth.degree + 1);
    }

    /** switch algorithms */
    final E[] squareSafe0() {
        if (1L * (degree + 1) * (degree + 1) <= MUL_CLASSICAL_THRESHOLD)
            return squareClassicalSafe(data, 0, degree + 1);
        else
            return squareKaratsubaSafe(data, 0, degree + 1);
    }

    /**
     * Classical n*m multiplication algorithm
     *
     * @param a     the first multiplier
     * @param aFrom begin in a
     * @param aTo   end in a
     * @param b     the second multiplier
     * @param bFrom begin in b
     * @param bTo   end in b
     * @return the result
     */
    final E[] multiplyClassicalSafe(final E[] a, final int aFrom, final int aTo, final E[] b, final int bFrom, final int bTo) {
        E[] result = domain.createZeroesArray(aTo - aFrom + bTo - bFrom - 1);
        multiplyClassicalSafe(result, a, aFrom, aTo, b, bFrom, bTo);
        return result;
    }

    /**
     * Classical n*m multiplication algorithm
     *
     * @param result where to write the result
     * @param a      the first multiplier
     * @param aFrom  begin in a
     * @param aTo    end in a
     * @param b      the second multiplier
     * @param bFrom  begin in b
     * @param bTo    end in b
     */
    final void multiplyClassicalSafe(final E[] result, final E[] a, final int aFrom, final int aTo, final E[] b, final int bFrom, final int bTo) {
        if (aTo - aFrom > bTo - bFrom) {
            multiplyClassicalSafe(result, b, bFrom, bTo, a, aFrom, aTo);
            return;
        }
        for (int i = 0; i < aTo - aFrom; ++i) {
            E c = a[aFrom + i];
            if (!domain.isZero(c))
                for (int j = 0; j < bTo - bFrom; ++j)
                    result[i + j] = domain.add(result[i + j], domain.multiply(c, b[bFrom + j]));
        }
    }

    /**
     * Karatsuba multiplication
     *
     * @param f     the first multiplier
     * @param g     the second multiplier
     * @param fFrom begin in f
     * @param fTo   end in f
     * @param gFrom begin in g
     * @param gTo   end in g
     * @return the result
     */
    E[] multiplyKaratsubaSafe(
            final E[] f, final int fFrom, final int fTo,
            final E[] g, final int gFrom, final int gTo) {
        // return zero
        if (fFrom >= fTo || gFrom >= gTo)
            return domain.createArray(0);

        // single element in f
        if (fTo - fFrom == 1) {
            E[] result = domain.createArray(gTo - gFrom);
            for (int i = gFrom; i < gTo; ++i)
                result[i - gFrom] = domain.multiply(f[fFrom], g[i]);
            return result;
        }
        // single element in g
        if (gTo - gFrom == 1) {
            E[] result = domain.createArray(fTo - fFrom);
            //single element in b
            for (int i = fFrom; i < fTo; ++i)
                result[i - fFrom] = domain.multiply(g[gFrom], f[i]);
            return result;
        }
        // linear factors
        if (fTo - fFrom == 2 && gTo - gFrom == 2) {
            E[] result = domain.createArray(3);
            //both a and b are linear
            result[0] = domain.multiply(f[fFrom], g[gFrom]);
            result[1] = domain.add(domain.multiply(f[fFrom], g[gFrom + 1]), domain.multiply(f[fFrom + 1], g[gFrom]));
            result[2] = domain.multiply(f[fFrom + 1], g[gFrom + 1]);
            return result;
        }
        //switch to classical
        if (1L * (fTo - fFrom) * (gTo - gFrom) < KARATSUBA_THRESHOLD)
            return multiplyClassicalSafe(g, gFrom, gTo, f, fFrom, fTo);

        if (fTo - fFrom < gTo - gFrom)
            return multiplyKaratsubaSafe(g, gFrom, gTo, f, fFrom, fTo);


        //we now split a and b into 2 parts:
        int split = (fTo - fFrom + 1) / 2;
        //if we can't split b
        if (gFrom + split >= gTo) {
            E[] f0g = multiplyKaratsubaSafe(f, fFrom, fFrom + split, g, gFrom, gTo);
            E[] f1g = multiplyKaratsubaSafe(f, fFrom + split, fTo, g, gFrom, gTo);

            int oldLen = f0g.length, newLen = fTo - fFrom + gTo - gFrom - 1;
            E[] result = Arrays.copyOf(f0g, newLen);
            fillZeroes(result, oldLen, newLen);
            for (int i = 0; i < f1g.length; i++)
                result[i + split] = domain.add(result[i + split], f1g[i]);
            return result;
        }

        int fMid = fFrom + split, gMid = gFrom + split;
        E[] f0g0 = multiplyKaratsubaSafe(f, fFrom, fMid, g, gFrom, gMid);
        E[] f1g1 = multiplyKaratsubaSafe(f, fMid, fTo, g, gMid, gTo);

        // f0 + f1
        E[] f0_plus_f1 = domain.createArray(Math.max(fMid - fFrom, fTo - fMid));
        System.arraycopy(f, fFrom, f0_plus_f1, 0, fMid - fFrom);
        fillZeroes(f0_plus_f1, fMid - fFrom, f0_plus_f1.length);
        for (int i = fMid; i < fTo; ++i)
            f0_plus_f1[i - fMid] = domain.add(f0_plus_f1[i - fMid], f[i]);

        //g0 + g1
        E[] g0_plus_g1 = domain.createArray(Math.max(gMid - gFrom, gTo - gMid));
        System.arraycopy(g, gFrom, g0_plus_g1, 0, gMid - gFrom);
        fillZeroes(g0_plus_g1, gMid - gFrom, g0_plus_g1.length);
        for (int i = gMid; i < gTo; ++i)
            g0_plus_g1[i - gMid] = domain.add(g0_plus_g1[i - gMid], g[i]);

        E[] mid = multiplyKaratsubaSafe(f0_plus_f1, 0, f0_plus_f1.length, g0_plus_g1, 0, g0_plus_g1.length);

        if (mid.length < f0g0.length) {
            int oldLen = mid.length;
            mid = Arrays.copyOf(mid, f0g0.length);
            fillZeroes(mid, oldLen, mid.length);
        }
        if (mid.length < f1g1.length) {
            int oldLen = mid.length;
            mid = Arrays.copyOf(mid, f1g1.length);
            fillZeroes(mid, oldLen, mid.length);
        }

        //subtract f0g0, f1g1
        for (int i = 0; i < f0g0.length; ++i)
            mid[i] = domain.subtract(mid[i], f0g0[i]);
        for (int i = 0; i < f1g1.length; ++i)
            mid[i] = domain.subtract(mid[i], f1g1[i]);


        int oldLen = f0g0.length;
        E[] result = Arrays.copyOf(f0g0, (fTo - fFrom) + (gTo - gFrom) - 1);
        fillZeroes(result, oldLen, result.length);
        for (int i = 0; i < mid.length; ++i)
            result[i + split] = domain.add(result[i + split], mid[i]);
        for (int i = 0; i < f1g1.length; ++i)
            result[i + 2 * split] = domain.add(result[i + 2 * split], f1g1[i]);

        return result;
    }

    E[] squareClassicalSafe(E[] a, int from, int to) {
        E[] x = domain.createZeroesArray((to - from) * 2 - 1);
        squareClassicalSafe(x, a, from, to);
        return x;
    }


    /**
     * Square the poly {@code data} using classical algorithm
     *
     * @param result result destination
     * @param data   the data
     * @param from   data from
     * @param to     end point in the {@code data}
     */
    void squareClassicalSafe(final E[] result, E[] data, int from, int to) {
        int len = to - from;
        for (int i = 0; i < len; ++i) {
            E c = data[from + i];
            if (!domain.isZero(c))
                for (int j = 0; j < len; ++j)
                    result[i + j] = domain.add(result[i + j], domain.multiply(c, data[from + j]));
        }
    }

    /**
     * Karatsuba squaring
     *
     * @param f     the data
     * @param fFrom begin in f
     * @param fTo   end in f
     * @return the result
     */
    E[] squareKaratsubaSafe(final E[] f, final int fFrom, final int fTo) {
        if (fFrom >= fTo)
            return domain.createArray(0);
        if (fTo - fFrom == 1) {
            E[] r = domain.createArray(1);
            r[0] = domain.multiply(f[fFrom], f[fFrom]);
            return r;
        }
        if (fTo - fFrom == 2) {
            E[] result = domain.createArray(3);
            result[0] = domain.multiply(f[fFrom], f[fFrom]);
            result[1] = domain.multiply(domain.valueOf(2), domain.multiply(f[fFrom], f[fFrom + 1]));
            result[2] = domain.multiply(f[fFrom + 1], f[fFrom + 1]);
            return result;
        }
        //switch to classical
        if (1L * (fTo - fFrom) * (fTo - fFrom) < KARATSUBA_THRESHOLD)
            return squareClassicalSafe(f, fFrom, fTo);


        //we now split a and b into 2 parts:
        int split = (fTo - fFrom + 1) / 2;
        int fMid = fFrom + split;
        E[] f0g0 = squareKaratsubaSafe(f, fFrom, fMid);
        E[] f1g1 = squareKaratsubaSafe(f, fMid, fTo);

        // f0 + f1
        E[] f0_plus_f1 = domain.createArray(Math.max(fMid - fFrom, fTo - fMid));
        System.arraycopy(f, fFrom, f0_plus_f1, 0, fMid - fFrom);
        fillZeroes(f0_plus_f1, fMid - fFrom, f0_plus_f1.length);
        for (int i = fMid; i < fTo; ++i)
            f0_plus_f1[i - fMid] = domain.add(f0_plus_f1[i - fMid], f[i]);

        E[] mid = squareKaratsubaSafe(f0_plus_f1, 0, f0_plus_f1.length);

        if (mid.length < f0g0.length) {
            int oldLen = mid.length;
            mid = Arrays.copyOf(mid, f0g0.length);
            fillZeroes(mid, oldLen, mid.length);
        }
        if (mid.length < f1g1.length) {
            int oldLen = mid.length;
            mid = Arrays.copyOf(mid, f1g1.length);
            fillZeroes(mid, oldLen, mid.length);
        }


        //subtract f0g0, f1g1
        for (int i = 0; i < f0g0.length; ++i)
            mid[i] = domain.subtract(mid[i], f0g0[i]);
        for (int i = 0; i < f1g1.length; ++i)
            mid[i] = domain.subtract(mid[i], f1g1[i]);


        int oldLen = f0g0.length;
        E[] result = Arrays.copyOf(f0g0, 2 * (fTo - fFrom) - 1);
        fillZeroes(result, oldLen, result.length);
        for (int i = 0; i < mid.length; ++i)
            result[i + split] = domain.add(result[i + split], mid[i]);
        for (int i = 0; i < f1g1.length; ++i)
            result[i + 2 * split] = domain.add(result[i + 2 * split], f1g1[i]);

        return result;
    }
}
