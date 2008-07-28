package org.bdwyer.rabinhash;

public class RabinPolynomials {

	public static enum POLYNOMIAL {
		REDUCIBLE, IRREDUCIBLE
	}

	public static POLYNOMIAL isReducible( long p ) {
		
		int k = 64;
		for( int i = 0; i < k; k++ ){
			// ni = n/pi;
		}

		long g = 0;
		for( int i = 0; i < k; k++ ){
			g = gcd( p, p/* xqi - x mod f*/);
			if( g == 1 ) return POLYNOMIAL.REDUCIBLE;
		}
		g = gcd( p, p/* xqn - x mod f*/);
		if( g == 1 ) return POLYNOMIAL.IRREDUCIBLE;
		
		return POLYNOMIAL.REDUCIBLE;
	}

	public static long gcd( long m, long n ) {

		if ( m < n ) {
			long t = m;
			m = n;
			n = t;
		}

		long r = m % n;

		if ( r == 0 ) {
			return n;
		} else {
			return gcd( n, r );
		}

	}

	public static long divide( long n, long p ) {
		return 0;
	}

	public static long mod( long n, long p ) {
		return 0;
	}

	public static long add( long n, long p ) {
		return 0;
	}

	public static long multiply( long n, long p ) {
		return 0;
	}

}
