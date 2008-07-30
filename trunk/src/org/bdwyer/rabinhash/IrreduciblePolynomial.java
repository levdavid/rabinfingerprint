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
	private static final BigInteger X = BigInteger.valueOf( 2L );

	/**
	 * Returns an irreducible monic polynomial of the finite field GF(2) of
	 * degree n (resulting in an n + 1 bit number).
	 */
	public static BigInteger getIrreducible( int n ) {

		Random random = new Random();
		long initseed = System.nanoTime();
		while ( true ) {
			random.setSeed( initseed++ );
			BigInteger f = new BigInteger( n, random ); // 2^n - 1
			f = f.setBit( n ); // 1<<n

			if ( getReducibiltiyRabin( f ) == REDUCIBILITY.IRREDUCIBLE ) {
				return f;
			}
		}
	}
	
	/**
	 * BenOr Reducibility Test
	 * 
	 * Tests and Constructions of Irreducible Polynomials over Finite Fields
	 * (1997) Shuhong Gao, Daniel Panario
	 * 
	 * http://citeseer.ist.psu.edu/cache/papers/cs/27167/http:zSzzSzwww.math.clemson.eduzSzfacultyzSzGaozSzpaperszSzGP97a.pdf/gao97tests.pdf
	 */
	public static REDUCIBILITY getReducibiltiyBenOr( BigInteger f ) {
		final float degree = f.bitLength() - 1;
		for ( int i = 1; i <= (int) Math.floor( degree / 2.0f ); i++ ) {
			BigInteger b = exponentMod( i, f );
			BigInteger g = polyGCD( f, b );
			if ( g.compareTo( BigInteger.ONE ) != 0 ) return REDUCIBILITY.REDUCIBLE;
		}

		return REDUCIBILITY.IRREDUCIBLE;
	}
	
	public static BigInteger polyGCD( BigInteger a, BigInteger b ) {
		while ( b.bitCount() != 0 ) {
			BigInteger t = BigInteger.ZERO.or( b );
			b = a.mod( b );
			a = t;
		}
		return a;
	}
	

	
	public static REDUCIBILITY getReducibiltiyRabin( BigInteger f ) {
		
		int degree = 17;
//		int[] factors = new int[]{degree/3,degree/5}; 
//		for ( int i = 0; i < factors.length; i++ ) {
//			int n_i = factors[i];
//			BigInteger b = exponentMod( n_i, f );
//			BigInteger g = f.gcd( b );
//			if ( g.compareTo( BigInteger.ONE ) != 0 ) return REDUCIBILITY.REDUCIBLE;
//		}
		
		BigInteger g = exponentMod( degree, f );
		if ( g.compareTo( BigInteger.ZERO ) != 0 ) return REDUCIBILITY.REDUCIBLE;
		
		return REDUCIBILITY.IRREDUCIBLE;
	}

	/**
	 * Computes ( x^q^p - x ) mod f
	 */
	private static BigInteger exponentMod( final int p, BigInteger f ) {
		// compute (x^q^p mod f)
		BigInteger q_to_p = BigInteger.valueOf( Q ).pow( p );
		BigInteger x_to_q_to_p = X.modPow(q_to_p, f );

		// subtract (x mod f)
		return x_to_q_to_p.xor( X ).mod( f );
	}
	
	public static String toPolynomialString( BigInteger bi ) {
		StringBuffer str = new StringBuffer();
		for ( int i = bi.bitLength(); i >= 0; i-- ) {
			boolean bit = bi.testBit( i );
			if ( bit ) {
				if ( str.length() != 0 ) {
					str.append( " + " );
				}
				str.append( "x^" + i );
			}
		}
		return str.toString();
	}

	public static void main( String[] args ) throws Exception {

		//BigInteger test = BigInteger.valueOf( 214085 );
		//REDUCIBILITY r = getReducibiltiyRabin( test );
		
		testAgainstMaple();
		
		
//		BigInteger f = getIrreducible( 63 );
//		System.out.println( "0x" + Long.toHexString( f.longValue() ).toUpperCase() );
//		System.out.println( toPolynomialString( f ) );

//		for ( int i = 0; i < 100; i++ ) {
//			BigInteger f = getIrreducible( 15 );
//			System.out.println( "if ((Irreduc(" + toPolynomialString( f ) + ") mod 2) = false) then \"" + toPolynomialString( f ) + " or 0x"
//					+ f.toString( ) + "\"; Factor(" + toPolynomialString( f ) + ") mod 2; end if;" );
//		}
		
		//testAgainstMaple();
		
	}

	private static void testAgainstMaple() {
		final int DEGREE = 12;
		for ( int i = 0; i < 50; i++ ) {
			Random random = new Random();
			random.setSeed( System.nanoTime() + i );
			BigInteger f = new BigInteger( DEGREE, random ); // 2^n - 1
			f = f.setBit( DEGREE ); // 1<<n
			
			REDUCIBILITY r = getReducibiltiyBenOr( f );
			
			StringBuffer str = new StringBuffer();
			String wrong = ( r == REDUCIBILITY.IRREDUCIBLE ? "false" : "true" );
			str.append( " if ((Irreduc(" + toPolynomialString( f ) + ") mod 2) = " + wrong + " ) then " );
			str.append( "\"wrong with " + wrong + " poly " + toPolynomialString( f ) + " equiv " + f.toString() + "\";" );
			str.append( "Factor(" + toPolynomialString( f ) + ") mod 2;" );
			str.append( "end if;" );
			
			System.out.println(str.toString());
		}
	}

	private static void testSpread() {
		final int DEGREE = 15;
		BigInteger f;
		REDUCIBILITY r;
		int i = 0;
		int j = 0;
		int last_i = 0;
		int spreads = 0;
		int spread_accum = 0;
		while ( true ) {
			Random random = new Random();
			random.setSeed( System.nanoTime() + i );
			f = new BigInteger( DEGREE, random ); // 2^n - 1
			f = f.setBit( DEGREE ); // 1<<n
			
			r = getReducibiltiyRabin( f );

			if ( i % 100000 == 0 ) System.out.println( "Tested " + i + " polynomials" );

			if ( r == REDUCIBILITY.IRREDUCIBLE ) {
				spread_accum += i - last_i;
				spreads++;
				if ( j++ % 10 == 0 ) System.out.println( "Avg Spread: " + spread_accum / spreads + " should be about " + DEGREE );
				last_i = i;
			}

			i++;
		}
	}

}
