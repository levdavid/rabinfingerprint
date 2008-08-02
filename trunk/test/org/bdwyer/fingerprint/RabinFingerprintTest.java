package org.bdwyer.fingerprint;


import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Random;

import org.bdwyer.fingerprint.Fingerprint;
import org.bdwyer.fingerprint.RabinFingerprintLong;
import org.bdwyer.fingerprint.RabinFingerprintPolynomial;
import org.bdwyer.polynomial.Polynomial;

public class RabinFingerprintTest {

	public static void main( String[] args ) throws Exception {
		// testAgainstMaple( true );
		// testAgainstMaple( false );
		// fingerprintFiles( true );
		// fingerprintFiles( false );
		testWindowing( true );
		testWindowing( false );
	}
	
	private static void testWindowing( boolean usePolynomials ) {
		for ( int i = 0; i < 10; i++ ) {
			// generate random data
			byte[] data = new byte[64];
			Random random = new Random();
			random.nextBytes( data );

			// generate random irreducible polynomial
			Polynomial p = Polynomial.createIrreducible( 53 );
			
			int window = 8;

			System.out.println("Round " + i);
			final Fingerprint< Polynomial > rabin0, rabin1;
			if ( usePolynomials ) {
				rabin0 = new RabinFingerprintPolynomial( p, window );
				rabin1 = new RabinFingerprintPolynomial( p, 0 );
			} else {
				rabin0 = new RabinFingerprintLong( p, window );
				rabin1 = new RabinFingerprintLong( p, 0 );
			}
			
			for ( int j = 0; j < window*3; j++ ) {
				rabin0.pushByte( data[j] );
			}
			
			for ( int j = window*3; j < window*4; j++ ) {
				rabin0.pushByte( data[j] );
				rabin1.pushByte( data[j] );
			}

			if ( rabin0.getFingerprint().compareTo( rabin1.getFingerprint() ) != 0 ) {
				System.out.println( "Incorrect fingerprint:" );
				System.out.println( "\t" + rabin0.getFingerprint().toHexString() );
				System.out.println( "\t" + rabin1.getFingerprint().toHexString() );
			}

		}
	}

	private static void testAgainstMaple( boolean usePolynomials ) {
		for ( int i = 0; i < 5; i++ ) {
			// generate random data
			byte[] data = new byte[64];
			Random random = new Random();
			random.nextBytes( data );
			Polynomial msg = Polynomial.createFromBytes( data );

			// generate random irreducible polynomial
			Polynomial p = Polynomial.createIrreducible( 53 );

			// fingerprinter
			final Fingerprint< Polynomial > rabin;
			if ( usePolynomials ) {
				rabin = new RabinFingerprintPolynomial( p );
			} else {
				rabin = new RabinFingerprintLong( p );
			}

			// fingerprint
			final Polynomial f = rabin.pushBytes( data ).getFingerprint();

			// compare with rabin's fingerprint function
			StringBuffer str = new StringBuffer();
			str.append( "if (" );
			str.append( "modpol(" + msg.toPolynomialString() + ", " + p.toPolynomialString() + ", x, 2) - (" + f.toPolynomialString() + ")" );
			str.append( " != 0) then " );
			str.append( "\"incorrect fingerprint\"" );
			str.append( " end if;" );
			System.out.println( str.toString() );
		}
	}

	private static void fingerprintFiles( boolean usePolynomials ) throws Exception {
		// generate random irreducible polynomial
		Polynomial p = Polynomial.createIrreducible( 31 );
		
		// choose a fingerprinting method
		final Fingerprint<?> rabin;
		if ( usePolynomials ) {
			rabin = new RabinFingerprintPolynomial( p, 128 );
		} else {
			rabin = new RabinFingerprintLong( p, 128 );
		}
		
		// time fingerprints
		System.out.println( "fingerprinting:" );
		fingerprintFile( "1.mp3", rabin );
		fingerprintFile( "2.mp3", rabin );
		fingerprintFile( "3.mp3", rabin );
	}

	private static void fingerprintFile( String filename, Fingerprint<?> rabin ) throws Exception {
		rabin.reset();
		long start = System.currentTimeMillis();
		InputStream stream = new FileInputStream( filename );
		int next;
		try {
			// buffering data like this is MUCH faster
			byte[] data = new byte[4096];
			while ( ( next = stream.read( data ) ) >= 0 ) {
				for ( int i = 0; i < next; i++ ) {
					rabin.pushByte( data[i] );
				}
			}
		} finally {
			stream.close();
		}

		long end = System.currentTimeMillis();
		System.out.println( filename + ": " + rabin.toString() + " in " + (end - start)/1000.0 + " seconds");

	}
}
