package au.nerago.mopgear.util;

public class BigNum implements Comparable<BigNum> {
    public static final BigNum ONE = new BigNum(0, 1);
    public static final BigNum TWO = new BigNum(0, 2);
    public static final BigNum THREE = new BigNum(0, 3);
    public static final BigNum FOUR = new BigNum(0, 4);
    private static final long FIELD_OVERFLOW = 0x1000_0000_0000_0000L;
    private static final long MASK_60 = 0x0FFF_FFFF_FFFF_FFFFL;
    private static final long MASK_30 = 0x0000_0000_3FFF_FFFFL;
    public static final int FIELD_SHIFT = 60;
    public static final int HALF_SHIFT = 30;

    // each only uses positive range 63 bits
    private final long hi, lo;

    private BigNum(long hi, long lo) {
        if (hi < 0 || hi >= FIELD_OVERFLOW || lo < 0 || lo >= FIELD_OVERFLOW)
            throw new IllegalArgumentException();
        this.hi = hi;
        this.lo = lo;
    }

    public static BigNum valueOf(long value) {
        return new BigNum(value >>> FIELD_SHIFT, value & MASK_60);
    }

    public BigNum add(BigNum other) {
        long new_lo = lo + other.lo;
        if (new_lo < FIELD_OVERFLOW) {
            return new BigNum(hi + other.hi, new_lo);
        } else {
            return new BigNum(hi + other.hi + 1, new_lo & MASK_60);
        }
    }

    public BigNum subtract(BigNum other) {
        long new_lo = lo - other.lo;
        if (new_lo >= 0) {
            return new BigNum(hi - other.hi, new_lo);
        } else {
            return new BigNum(hi - other.lo - 1, new_lo & MASK_60);
        }
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public BigNum multiply(BigNum other) {
        // break down into 30 bit pieces, worst multiply then goes to 60, can be shifted back
        long a3 = this.hi >>> HALF_SHIFT;
        long a2 = this.hi & MASK_30;
        long a1 = this.lo >>> HALF_SHIFT;
        long a0 = this.lo & MASK_30;
        long b3 = other.hi >>> HALF_SHIFT;
        long b2 = other.hi & MASK_30;
        long b1 = other.lo >>> HALF_SHIFT;
        long b0 = other.lo & MASK_30;

        // result probably just overflow
        if ((a3 != 0) && (b3 != 0 || b2 != 0 || b1 != 0))
            throw new ArithmeticException();
        else if ((a3 != 0 || a2 != 0 || a1 != 0) && (b3 != 0))
            throw new ArithmeticException();
        else if (a2 != 0 && b2 != 0)
            throw new ArithmeticException();

        // 0 shifts
        long mul00 = a0 * b0;
        long new_lo = mul00;

        // 1 shift
        long mul01 = a0 * b1, mul10 = a1 * b0;
        new_lo += ((mul01 << HALF_SHIFT) & MASK_60) + ((mul10 << HALF_SHIFT) & MASK_60);
        long new_hi = (new_lo >>> FIELD_SHIFT) + (mul01 >>> HALF_SHIFT) + (mul10 >>> HALF_SHIFT);

        // 2 shifts
        long mul11 = a1 * b1;
        long mul02 = a0 * b2, mul20 = a2 * b0;
        new_hi += mul11 + mul02 + mul20;

        // 3 shifts
        long mul03 = a0 * b3, mul30 = a3 * b0;
        long mul12 = a1 * b2, mul21 = a2 * b1;
        new_hi += (mul03 << HALF_SHIFT) + (mul30 << HALF_SHIFT) + (mul12 << HALF_SHIFT) + (mul21 << HALF_SHIFT);

        return new BigNum(new_lo, new_hi);
    }

    public BigNum divide(BigNum other) {
        //   (1000a + b) / (1000c + d)
        // = 1000a / (1000c + d)      +     b / (1000c + d)
        // decision on if C is zero?

        // if C is zero then
        // = hi[1000a / d] + lo[b / d]

        // if C is nonzero then
        // = hi[0] lo[a / c]


        // is there a more general version with limit bit shifts

        return null;
    }

    public BigNum[] divideAndRemainder(BigNum other) {
        BigNum div = this.divide(other);
        BigNum mul = div.multiply(other);
        BigNum rem = this.subtract(mul);
        return new BigNum[] { div, rem };
    }

    // a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
    @Override
    public int compareTo(BigNum other) {
        if (this.hi < other.hi)
            return -1;
        else if (this.hi > other.hi)
            return 1;
        else
            return Long.compare(this.lo, other.lo);
    }

    public boolean fitsMaxLong() {
        return hi <= 0x7;
    }

    public long longValue() {
        return lo + ((hi & 0x7) << FIELD_SHIFT);
    }

    public int intValueExact() {
        if (hi != 0 || lo > Integer.MAX_VALUE)
            throw new ArithmeticException();
        return (int) lo;
    }
}
