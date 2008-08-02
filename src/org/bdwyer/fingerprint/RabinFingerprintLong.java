package org.bdwyer.fingerprint;

import java.math.BigInteger;

import org.bdwyer.polynomial.Polynomial;

/**
 * A {@link Fingerprint} builder that uses longs and lookup tables to increase
 * performance.
 * 
 * 
 * Note, the polynomial must be of degree 64 - 8 - 1 - 1 = 54 or less!
 * <pre>
 *   64 for the size of a long
 *    8 for the space we need when shifting
 *    1 for the sign bit (Java doesn't support unsigned longs)
 *    1 for the conversion between degree and bit offset.
 * </pre>
 * 
 * Some good choices are 53, 47, 31, 15
 * 
 * @see RabinFingerprintPolynomial for a rundown of the math
 */
public class RabinFingerprintLong extends AbstractFingerprint {

	private final int degree;
	private long fingerprint;

	private final long[] pushTable = new long[512];
	private final long[] popTable = new long[256];
	
	public RabinFingerprintLong( Polynomial poly ){
		this( poly, 0 );
	}
	
	public RabinFingerprintLong( Polynomial poly, long bytesPerWindow ) {
		super( poly, bytesPerWindow );
		this.degree = poly.degree().intValue();
		precomputeTables();
	}

	/**
	 * Precomputes the results of pushing and popping bytes. These use the more
	 * accurate Polynomial methods (they won't overflow like longs, and they
	 * compute in GF(2^k)).
	 * 
	 * These algorithms should be synonymous with
	 * {@link RabinFingerprintPolynomial#pushByte} and
	 * {@link RabinFingerprintPolynomial#popByte}, but the results are stored
	 * to be xor'red with the fingerprint in the inner loop of our own
	 * {@link #pushByte} and {@link #popByte}
	 */
	private void precomputeTables() {
		for ( int i = 0; i < 512; i++ ) {
			Polynomial f = Polynomial.createFromLong( i );
			f = f.shiftLeft( poly.degree() );
			f = f.xor( f.mod( poly ) );
			pushTable[i] = f.toBigInteger().longValue();
		}

		for ( int i = 0; i < 256; i++ ) {
			Polynomial f = Polynomial.createFromLong( i );
			f = f.shiftLeft( BigInteger.valueOf( bytesPerWindow * 8 ) );
			f = f.mod( poly );
			popTable[i] = f.toBigInteger().longValue();
		}
	}

	/**
	 * Adds one byte to the fingerprint.
	 * 
	 * {@link RabinFingerprintPolynomial#pushByte}
	 */
	@Override
	public synchronized RabinFingerprintLong pushByte( byte b ) {
		long f = fingerprint;
		int i = (int) ( f >> ( degree - 8 ) & 0x1FF );
		f <<= 8;
		f |= b & 0xFF;
		f ^= pushTable[i];

		fingerprint = f;
		byteCount++;

		if ( bytesPerWindow > 0 ) {
			byteWindow.add( b );
			if ( byteCount > bytesPerWindow ) popByte();
		}
		return this;
	}
	/**
	 * Removes the contribution of the first byte in the byte queue from the
	 * fingerprint.
	 * 
	 * {@link RabinFingerprintPolynomial#popByte}
	 */
	@Override
	public synchronized RabinFingerprintLong popByte() {
		long f = fingerprint;
		byte b = byteWindow.poll();
		f ^= popTable[(int) ( b & 0xFF )];

		fingerprint = f;
		byteCount--;

		return this;
	}

	@Override
	public synchronized RabinFingerprintLong reset() {
		super.reset();
		this.fingerprint = 0L;
		return this;
	}

	@Override
	public synchronized Polynomial getFingerprint() {
		return Polynomial.createFromLong( fingerprint );
	}

	public synchronized long getFingerprintLong() {
		return fingerprint;
	}
}
