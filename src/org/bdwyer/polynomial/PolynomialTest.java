package org.bdwyer.polynomial;

import java.util.Random;

import org.bdwyer.polynomial.Polynomial.Reducibility;

public class PolynomialTest {

	public static void main( String[] args ) {
		final int degree = 15;
		// printOutSomePolys();
		// generateIrreducibles( degree );
		// testAgainstMaple( degree );
		// generateLargeIrreducible();
		testSpread( degree );
	}

	/**
	 * Generates a handful of irreducible polynomials of the specified degree.
	 */
	private static void generateIrreducibles( final int degree ) {
		for ( int i = 0; i < 10; i++ ) {
			Polynomial p = Polynomial.createIrreducible( degree );
			System.out.println( p.toPolynomialString() );
		}
	}

	/**
	 * Generates a large irreducible polynomial and prints out its
	 * representation in ascii and hex.
	 */
	private static void generateLargeIrreducible() {
		Polynomial p = Polynomial.createIrreducible( 127 );
		System.out.println( p.toPolynomialString() );
		System.out.println( p.toHexString() );
	}

	/**
	 * Tests loading and printing out of polynomials.
	 * 
	 * The polys used are from here:
	 * http://en.wikipedia.org/wiki/Finite_field_arithmetic#Rijndael.27s_finite_field
	 */
	private static void printOutSomePolys() {
		Polynomial pa = Polynomial.createFromLong( 0x53 );
		Polynomial pb = Polynomial.createFromLong( 0xCA );
		Polynomial pm = Polynomial.createFromLong( 0x11B );
		System.out.println( "A: " + pa.toPolynomialString() );
		System.out.println( "B: " + pb.toPolynomialString() );
		System.out.println( "M: " + pm.toPolynomialString() );
		Polynomial px = pa.multiply( pb );
		System.out.println( "AxB: " + px.toPolynomialString() );
		Polynomial pabm = px.mod( pm );
		System.out.println( "AxB mod M: " + pabm.toPolynomialString() );
	}

	/**
	 * This prints out code that can be pasted into Maple for comparison. We
	 * assume Maple is correct in this test, and verify our reducibility results
	 * with theirs.
	 * 
	 * If everything is correct, there should be NO OUTPUT.
	 * 
	 * This is a good correctness verification.
	 */
	private static void testAgainstMaple( final int degree ) {
		// this will give us 10 irreducible polys on average
		for ( int i = 0; i < degree * 10; i++ ) {
			Polynomial p = Polynomial.createRandom( degree );
			Reducibility r = p.getReducibility();
			String rstr = ( r == Reducibility.IRREDUCIBLE ? "true" : "false" );
			StringBuffer str = new StringBuffer();
			str.append( " if ((Irreduc(" + p.toPolynomialString() + ") mod 2) != " + rstr + ") then " );
			str.append( "\"" + r.toString() + " is incorrect for poly " + p.toPolynomialString() + " equiv to decimal " + p.toDecimalString() + "\";" );
			str.append( "Factor(" + p.toPolynomialString() + ") mod 2;" );
			str.append( "end if;" );
			System.out.println( str.toString() );
		}
	}

	/**
	 * According to Rabin, the expected number of tests required to find an
	 * irreducible polynomial from a randomly chosen monic polynomial of degree
	 * k is k!
	 * 
	 * Therefore, we should see an average spread of k reducible polynomials
	 * between irreducible ones. This test computes the running average of these
	 * spreads for verification.
	 * 
	 * This is not a perfect correctness verification, but it is a good "mine
	 * canary".
	 */
	private static void testSpread( final int degree ) {
		int i = 0;
		int last_i = 0;
		float spread_accum = 0;
		float spread_count = 0;

		while ( true ) {
			Polynomial f = Polynomial.createRandom( degree );
			Reducibility r = f.getReducibility();

			if ( r == Reducibility.IRREDUCIBLE ) {
				int spread = i - last_i;
				spread_accum += spread;
				spread_count += 1;
				float avg_spread = spread_accum / spread_count;
				if ( spread_count % 10 == 0 ) System.out.println( "avg spread: " + avg_spread + " should converge to around " + degree );
				last_i = i;
			}
			i++;
		}
	}

}
