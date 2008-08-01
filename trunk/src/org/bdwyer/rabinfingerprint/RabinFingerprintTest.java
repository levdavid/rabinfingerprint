package org.bdwyer.rabinfingerprint;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Random;

import org.bdwyer.galoisfield.Polynomial;

public class RabinFingerprintTest {

	public static void main( String[] args ) throws Exception {
		testAgainstMaple( true );
		// fingerprintFiles();
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

			// fingerprint
			final Polynomial f;
			if ( usePolynomials ) {
				RabinFingerprintPolynomial rabin = new RabinFingerprintPolynomial( p ).appendBytes( data );
				f = rabin.getFingerprint();
			} else {
				RabinFingerprintLong rabin = new RabinFingerprintLong( p.toBigInteger().longValue() ).appendBytes( data );
				f = Polynomial.createFromLong( rabin.getFingerprint() );
			}

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

	private static void fingerprintFiles() throws Exception {
		// generate random irreducible polynomial
		Polynomial p = Polynomial.createIrreducible( 31 );
		RabinFingerprintLong rabin = new RabinFingerprintLong( p.toBigInteger().longValue() );
		System.out.println( "fingerprinting:" );
		fingerprintFile( "1.mp3", rabin );
		fingerprintFile( "2.mp3", rabin );
		fingerprintFile( "3.mp3", rabin );
	}

	private static void fingerprintFile( String filename, RabinFingerprintLong rabin ) throws Exception {
		rabin.reset();

		InputStream stream = new FileInputStream( filename );
		int next;
		try {
			byte[] data = new byte[2048];
			while ( ( next = stream.read( data ) ) >= 0 ) {
				for ( int i = 0; i < next; i++ ) {
					rabin.appendByte( data[i] );
				}
			}
		} finally {
			stream.close();
		}
		Long f = rabin.getFingerprint();
		System.out.println( filename + ": " + Long.toHexString( f ).toUpperCase() );

	}
}
