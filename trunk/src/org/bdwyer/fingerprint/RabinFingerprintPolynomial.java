package org.bdwyer.fingerprint;

import java.math.BigInteger;

import org.bdwyer.polynomial.Polynomial;

/**
 * This class implements Rabin's Fingerprinting scheme using a Polynomial class
 * which handles calculations in the finite field GF(2). This is slower than
 * {@link RabinFingerprintLong}, but can support monic irreducible polynomials
 * of nearly any degree.
 * 
 * Rabin's Fingerprinting Scheme: http://en.wikipedia.org/wiki/Rabin_fingerprint
 * 
 * <pre>
 *     Given an n-bit message m_0, ..., m_n-1, we view it as a polynomial of degree
 *     n-1 over the finite field GF(2).
 * 
 *         m(x) = m_0 + m_1 x + ... + m_n-1 x_n-1
 * 
 *     We then pick a random irreducible polynomial p(x) of degree k over GF(2), and
 *     we define the fingerprint of m to be
 * 
 *         f(x) = m(x) mod p(x)
 *   
 *     which can be viewed as a polynomial of degree k-1 or as a k-bit number.
 * </pre>
 * 
 * Because we are operating in the space defined by mod p(x), we only ever need
 * to store a fingerprint of k bits. All new bits are shifted in and modded with
 * p(x), resulting in a new k-bit number.
 * 
 * <pre>
 *     This follows from the fact that given an n-bit message:
 * 
 *         m(x) = m_0 x_n-1 + .... + m_n-2 x + m_n-1; 
 *         
 *     and its fingerprint
 *     
 *         f(x) = m(x) mod p(x)
 *              = r_0 x_k-1 + ... + r_k-2 x + r_k-1
 *         
 *     appending one more bit simplifies to
 *         
 *         f({m(x)*x + m_n}) = {m(x)*x + m_n} mod p(x);
 * </pre>
 * 
 * This means that to add one bit, we need merely shift in that bit and re-mod
 * with p(x). Similarly, we can add entire bytes, words, etc, by shifting them
 * in and modding with p(x). This can become untennable and slow if we shift too
 * much at once, as the mod calculation is done with synthetic division and will
 * take on the order of {number of bits shifted in} to complete.
 * 
 * A table lookup method is obviously possible, and that is exactly what we do
 * in {@link RabinFingerprintLong}.
 * 
 * Michael O. Rabin, "Fingerprinting by Random Polynomials" (1981)
 * http://www.xmailserver.org/rabin.pdf
 * 
 * Andrei Z. Broder, "Some applications of Rabin's fingerprinting method" (1993)
 * http://citeseer.ist.psu.edu/broder93some.html
 * 
 */
public class RabinFingerprintPolynomial extends AbstractFingerprint {

	private static final BigInteger BYTE_SHIFT = BigInteger.valueOf( 8L );

	private Polynomial fingerprint;

	public RabinFingerprintPolynomial( Polynomial poly ) {
		this( poly, 0 );
	}

	public RabinFingerprintPolynomial( Polynomial poly, long bytesPerWindow ) {
		super( poly, bytesPerWindow );
	}

	/**
	 * Shifts in byte b and mods with poly
	 * 
	 * If we have passed overflowed our window, we pop a byte
	 */
	@Override
	public synchronized RabinFingerprintPolynomial pushByte( byte b ) {
		Polynomial f = fingerprint;
		f = f.shiftLeft( BYTE_SHIFT );
		f = f.or( Polynomial.createFromLong( b & 0xFFL ) );
		f = f.mod( poly );

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
	 * Note that the shift necessary to calculate it's contribution can be
	 * sizeable, and the mod calculation will be similarly slow. It is therefore
	 * done with the Polynomial to support arbitrary sizes.
	 * 
	 * Note that despite this massive shift, the fingerprint will still result
	 * in a k-bit number at the end of the calculation.
	 */
	@Override
	public synchronized RabinFingerprintPolynomial popByte() {
		byte b = byteWindow.poll();
		Polynomial f = Polynomial.createFromLong( b & 0xFFL );
		f = f.shiftLeft( BigInteger.valueOf( bytesPerWindow * 8 ) );
		f = f.mod( poly );

		fingerprint = fingerprint.xor( f );
		byteCount--;

		return this;
	}

	@Override
	public synchronized RabinFingerprintPolynomial reset() {
		super.reset();
		this.fingerprint = new Polynomial();
		return this;
	}

	@Override
	public synchronized Polynomial getFingerprint() {
		return fingerprint;
	}
}
