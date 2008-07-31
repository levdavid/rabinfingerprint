package org.bdwyer.galoisfield;

import java.util.Random;

import org.bdwyer.galoisfield.Polynomial.Reducibility;

public class PolynomialTest {

	public static void main( String[] args ) {
		final int degree = 15;
//		printOutSomePolys();
//		generateIrreducibles( degree );
//		testAgainstMaple( degree );
		testSpread( degree );
	}

	private static void generateIrreducibles( final int degree ) {
		for ( int i = 0; i < 10; i++ ) {
			Polynomial p = Polynomial.createIrreducible( degree );
			System.out.println( p.toPolynomialString() );
		}
	}

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

	private static void testAgainstMaple( final int degree ) {
		for ( int i = 0; i < 10; i++ ) {
			Polynomial p = Polynomial.createIrreducible( degree );
			StringBuffer str = new StringBuffer();
			str.append( " if ((Irreduc(" + p.toPolynomialString() + ") mod 2) = false ) then " );
			str.append( "\"incorrect for poly " + p.toPolynomialString() + " equiv to decimal " + p.toDecimalString() + "\";" );
			str.append( "Factor(" + p.toPolynomialString() + ") mod 2;" );
			str.append( "end if;" );
			System.out.println( str.toString() );
		}
	}

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
