package org.bdwyer.rabinfingerprint;

import java.math.BigInteger;

import org.bdwyer.galoisfield.Polynomial;

public class RabinFingerprintPolynomial implements Fingerprint< Polynomial > {

	private static final BigInteger BYTE_SHIFT = BigInteger.valueOf( 8L );
	
	private final Polynomial poly;
	private Polynomial fingerprint;
	private long bits;

	public RabinFingerprintPolynomial( Polynomial poly ) {
		this.poly = poly;
		reset();
	}

	public RabinFingerprintPolynomial appendBytes( byte[] bytes ) {
		for ( byte b : bytes ) {
			appendByte( b );
		}
		return this;
	}

	public synchronized RabinFingerprintPolynomial appendByte( byte b ) {
		Polynomial f = fingerprint;
		f = f.shiftLeft( BYTE_SHIFT );
		f = f.or( Polynomial.createFromLong( b & 0xFFL ) );
		f = f.mod( poly );
		fingerprint = f;
		bits += 8;
		return this;
	}

	public synchronized RabinFingerprintPolynomial reset() {
		this.bits = 0;
		this.fingerprint = new Polynomial();
		return this;
	}

	public synchronized Polynomial getFingerprint() {
		return fingerprint;
	}

	public synchronized long getBits() {
		return bits;
	}
}
