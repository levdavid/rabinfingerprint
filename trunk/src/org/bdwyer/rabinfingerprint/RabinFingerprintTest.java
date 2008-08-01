package org.bdwyer.rabinfingerprint;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Random;

import org.bdwyer.galoisfield.Polynomial;

public class RabinFingerprintTest {

	private static byte[] bytes = new byte[] {
		0x71, 0x01, 0x01, 0x01, 0x04, 0x05, 0x06, 0x07,
		0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
		0x20, 0x21,	0x22, 0x23, 0x24, 0x25, 0x26, 0x27,
		0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
		};

	public static void main( String[] args ) throws Exception {
		//testAgainstMaple();
		fingerprintFiles();
	}


	private static void testAgainstMaple() {
		for ( int i = 0; i < 10; i++ ) {
			// generate random data
			byte[] data = new byte[64];
			Random random = new Random();
			random.nextBytes( data );
			Polynomial msg = Polynomial.createFromBytes( data );
			// generate random irreducible polynomial
			Polynomial p = Polynomial.createIrreducible( 31 );
			// fingerprint
			//RabinFingerprintPolynomial rabin = new RabinFingerprintPolynomial( p ).appendBytes( data );
			//Polynomial f = rabin.getFingerprint();

			RabinFingerprintLong rabin = new RabinFingerprintLong( p.toBigInteger().longValue() ).appendBytes( data );
			Polynomial f = Polynomial.createFromLong( rabin.getFingerprint() );
			
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

		fingerprintFile( "1.mp3", rabin );
		fingerprintFile( "2.mp3", rabin );
		fingerprintFile( "3.mp3", rabin );
	}

	private static void fingerprintFile( String filename, RabinFingerprintLong rabin ) throws Exception {
		rabin.reset();
		
		InputStream stream = new FileInputStream( filename );
		long count = 0;
		long kb = 0;
		int next;
		try {
			while ( ( next = stream.read() ) >= 0 ) {
				rabin.appendByte( (byte) next );
				count++;
				if ( count % (100*1024) == 0 ) {
					kb+=100;
					//System.out.println( kb + "KB read" );
				}
			}
		} finally {
			stream.close();
		}
		Long f = rabin.getFingerprint();
		System.out.println( filename + ": " + Long.toHexString( f ).toUpperCase() + ", " + count + " bytes" );

	}
}
