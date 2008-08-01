package org.bdwyer.rabinfingerprint;

import java.math.BigInteger;

import org.bdwyer.galoisfield.Polynomial;

/**
 * Constructs fingerprints in Longs.
 * 
 * Note, the polynomial must be of degree 64 - 8 - 1 - 1 = 54 or less!
 * 64 for the size of a long
 * 8 for the space we need when shifting
 * 1 for the sign bit (Java doesn't support unsigned longs)
 * 1 for the conversion between degree and bit offset
 * 
 */
public class RabinFingerprintLong implements Fingerprint< Long > {

	private final Long poly;
	private Long bits;
	private Long fingerprint;

	public RabinFingerprintLong( Long poly ) {
		this.poly = poly;
		reset();
	}

	public RabinFingerprintLong appendBytes( byte[] bytes ) {
		for ( byte b : bytes ) {
			appendByte( b );
		}
		return this;
	}

	public synchronized RabinFingerprintLong appendByte( byte b ) {
		Long f = fingerprint;
		f = f << 8;
		f = f | ( b & 0xFF );
		f = mod( f, poly );
		fingerprint = f;
		bits += 8;
		return this;
	}

	public synchronized RabinFingerprintLong reset() {
		this.bits = 0L;
		this.fingerprint = 0L;
		return this;
	}

	public synchronized Long getFingerprint() {
		return fingerprint;
	}

	protected long mod( long a, long b ) {
		int ma = getMaxBit( a );
		int mb = getMaxBit( b );
		for ( int i = ma - mb; i >= 0; i-- ) {
			if ( testBit( a, ( i + mb ) ) ) {
				long shifted = b << i;
				a = a ^ shifted;
			}
		}
		return a;
	}

	protected int getMaxBit( long l ) {
		for ( int i = 64 - 1; i >= 0; i-- ) {
			if ( testBit( l, i ) ) return i;
		}
		return -1;
	}

	protected boolean testBit( long l, int index ) {
		return ( ( ( l >> index ) & 1 ) == 1 );
	}

}
