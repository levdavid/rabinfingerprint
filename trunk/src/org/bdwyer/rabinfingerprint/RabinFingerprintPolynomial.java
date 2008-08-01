package org.bdwyer.rabinfingerprint;

import java.math.BigInteger;

import org.bdwyer.galoisfield.Polynomial;

public class RabinFingerprintPolynomial implements Fingerprint< Polynomial > {

	private final Polynomial poly;
	private final Polynomial poly_xor;
	private final BigInteger degree;
	private long bits;
	private Polynomial fingerprint;

	public RabinFingerprintPolynomial( Polynomial poly ) {
		this.poly = poly;
		this.poly_xor = poly.clearDegree( poly.degree() );
		this.degree = poly.degree();

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
		f = f.shiftLeft( BigInteger.valueOf( 8L ) );
		f = f.or( Polynomial.createFromLong( b & 0xFFL ) );
		f = f.mod( poly );
		fingerprint = f;
		bits += 8;
		return this;
	}

	private Polynomial shiftInBit( Polynomial f, boolean inboundBit ) {
		// get output bit
		boolean r1 = f.hasDegree( degree );
		// shift
		f = f.shiftLeft( BigInteger.ONE );
		// set input bit
		if ( inboundBit ) f = f.setDegree( BigInteger.ZERO );
		// xor on output == 1
		if ( r1 ) f = f.xor( poly_xor );
		return f.mod( poly );
	}
	
	public synchronized RabinFingerprintPolynomial reset() {
		this.bits = 0;
		this.fingerprint = new Polynomial();
		return this;
	}

	public synchronized Polynomial getFingerprint() {
		return fingerprint;
	}

}
