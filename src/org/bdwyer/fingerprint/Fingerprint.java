package org.bdwyer.fingerprint;


/**
 * Overview of Rabin's scheme given by Broder
 * 
 * Some Applications of Rabin's Fingerprinting Method
 * http://citeseer.ist.psu.edu/cache/papers/cs/752/ftp:zSzzSzftp.digital.comzSzpubzSzDECzSzSRCzSzpublicationszSzbroderzSzfing-appl.pdf/broder93some.pdf
 */
public interface Fingerprint<T> {
	public void pushBytes( byte[] bytes );
	public void pushBytes( byte[] bytes, int offset, int length );
	public void pushByte( byte b );	
	public void popByte();	
	public void reset() ;
	public T getFingerprint() ;
	public long getBytesPerWindow();
}
