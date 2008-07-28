package org.bdwyer.rabinhash;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class IrreduciblePolynomial {
	
	public static enum REDUCIBILITY {
		REDUCIBLE, IRREDUCIBLE
	};

	/** number of elements in the finite field GF(2) */
	private static final long Q = 2L;
	
	/** binary representation of the polynomial "p(x) = x" */
	private static final BigInteger X = BigInteger.valueOf(2L);

	/**
	 * Returns an irreducible monic polynomial of the finite field GF(2) of
	 * degree 63 (resulting in a 64 bit number).
	 */
	public static long getIrreducibleLong( ){
		BigInteger f = getIrreducible( 63 );
		return f.longValue();
	}
	/**
	 * Returns an irreducible monic polynomial of the finite field GF(2) of
	 * degree n (resulting in an n + 1 bit number).
	 */
	public static BigInteger getIrreducible( int n ){
		while (true) {
			Random random = new Random();
			random.setSeed( System.currentTimeMillis() );
			
			BigInteger f = new BigInteger(n, random); // 2^n - 1
			f = f.setBit(n); // 1<<n 
			
			if( getReducibiltiyBenOr(f) == REDUCIBILITY.IRREDUCIBLE ){
				return f;
			}
		}
	}
	
	/**
	 * BenOr Reducibility Test
	 * 
	 * Tests and Constructions of Irreducible Polynomials over Finite Fields (1997)
	 * Shuhong Gao, Daniel Panario
	 * 
	 * http://citeseer.ist.psu.edu/cache/papers/cs/27167/http:zSzzSzwww.math.clemson.eduzSzfacultyzSzGaozSzpaperszSzGP97a.pdf/gao97tests.pdf
	 */
	public static REDUCIBILITY getReducibiltiyBenOr( BigInteger f ){
		final float degree = f.bitLength() - 1;
		for( int i = 1; i <= (int)Math.floor( degree/2.0f ); i++ ){
			BigInteger b = exponentMod(i, f);
			BigInteger g = f.gcd(b);
			if( g.compareTo( BigInteger.ONE ) != 0 ) return REDUCIBILITY.REDUCIBLE;
		}
		return REDUCIBILITY.IRREDUCIBLE;
	}
	
	/**
	 * Computes ( x^q^p - x ) mod f
	 */
	private static BigInteger exponentMod(final int p, BigInteger f) {
		// compute  (x^q^p mod f)
		BigInteger q_to_p = BigInteger.valueOf(Q).pow(p);
		BigInteger x_to_q_to_p = X.modPow(q_to_p, f);

		// subtract (x mod f)
		return x_to_q_to_p.xor(X.negate());
	}
	
	public static void main(String[] args) throws Exception {

		//long f = getIrreducible( 63 ).longValue();
		//System.out.println("0x" + Long.toHexString(f).toUpperCase());
		
		final int BITS = 128;
		BigInteger f;
		REDUCIBILITY r;
		int i = 0;
		int j = 0;
		int last_i = 0;
		int spreads = 0;
		int spread_accum = 0;
		while (true) {
			Random random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed( System.currentTimeMillis() );
			f = new BigInteger(BITS, random);
			f = f.setBit(BITS - 1);
			r = getReducibiltiyBenOr(f);

			if( i % 100000 == 0 ) System.out.println( "Tested " + i + " polynomials");
			
			if (r == REDUCIBILITY.IRREDUCIBLE){
				//System.out.println(i + ": " + f.toString() + " is " + r.name());
				spread_accum += i - last_i;
				spreads++;
				if( j++ % 100 == 0 ) System.out.println( "Avg Spread: " + spread_accum / spreads + " should be about " + (BITS - 1));
				last_i = i;
			}

			i++;
		}

	}
	
}
