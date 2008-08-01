package org.bdwyer.rabinfingerprint;

import java.math.BigInteger;
import java.util.Random;

import org.bdwyer.galoisfield.Polynomial;
import org.bdwyer.rabinhash.RabinFingerPrint;

/**
 * Overview of Rabin's scheme given by Broder
 * 
 * Some Applications of Rabin's Fingerprinting Method
 * http://citeseer.ist.psu.edu/cache/papers/cs/752/ftp:zSzzSzftp.digital.comzSzpubzSzDECzSzSRCzSzpublicationszSzbroderzSzfing-appl.pdf/broder93some.pdf
 */
public interface Fingerprint<T> {
	public Fingerprint<T> appendBytes( byte[] bytes );
	public Fingerprint<T> appendByte( byte b );	
	public Fingerprint<T> reset() ;
	public T getFingerprint() ;
}
