package au.nerago.mopgear.util;

import java.math.BigInteger;

public class Primes {
    public static BigInteger roundToPrime(BigInteger val) {
        if (val.isProbablePrime(90)) {
            return val;
        } else {
            return val.nextProbablePrime();
        }
    }

    public static int roundToPrimeInt(int val) {
        return roundToPrime(BigInteger.valueOf(val)).intValueExact();
    }
}
