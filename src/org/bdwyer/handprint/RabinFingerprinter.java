package org.bdwyer.handprint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * We compute the checksum using Broder s implementation of
 * Rabin s fingerprinting algorithm. Fingerprints offer 
 * provably strong probabilistic guarantees that two 
 * different strings will not have the same fingerprint. 
 * Other checksum algorithms, such as MD5 and SHA, do not 
 * offer such provable guarantees, and are also more 
 * expensive to compute than Rabin fingerprint.
 *
 * A disadvantage is that these faster functions are 
 * efficiently invertible (that is, one can easily build an 
 * URL that hashes to a particular location), a fact that  
 * might be used by malicious users to nefarious purposes.
 *
 * Using the Rabin's fingerprinting function, the probability of
 * collision of two strings s1 and s2 can be bounded (in a adversarial
 * model for s1 and s2) by max(|s1|,|s2|)/2**(l-1), where |s1| is the 
 * length of the string s1 in bits.
 * 
 * The advantage of choosing Rabin fingerprints (which are based on random
 * irreducible polynomials) rather than some arbitrary hash function is that
 * their probability of collision os well understood. Furthermore Rabin 
 * fingerprints can be computed very efficiently in software and we can
 * take advantage of their algebraic properties when we compute the
 * fingerprints of "sliding windows".
 *
 * M. O. Rabin
 * Fingerprinting by random polynomials.
 * Center for Research in Computing Technology
 * Harvard University Report TR-15-81
 * 1981
 * 
 * A. Z. Broder
 * Some applications of Rabin's fingerprinting method
 * In R.Capicelli, A. De Santis and U. Vaccaro editors
 * Sequences II:Methods in Communications, Security, and Computer Science
 * pages 143-152
 * Springer-Verlag
 * 1993
 *
 */
public class RabinFingerprinter {

	private final static int P_DEGREE = 64;
	private final static int X_P_DEGREE = 1 << ( P_DEGREE - 1 );
	private final static long POLY = Long.decode( "0x0060034000F0D50A" ).longValue();

	private final static long[] table32 = new long[256];
	private final static long[] table40 = new long[256];
	private final static long[] table48 = new long[256];
	private final static long[] table54 = new long[256];
	private final static long[] table62 = new long[256];
	private final static long[] table70 = new long[256];
	private final static long[] table78 = new long[256];
	private final static long[] table84 = new long[256];
	
	public interface WindowVisitor {
		public void visitWindow( int offset, long fingerprint );
	}

	static {
		long[] mods = new long[P_DEGREE];
		mods[0] = POLY;
		for ( int i = 0; i < 256; i++ ) {
			table32[i] = 0;
			table40[i] = 0;
			table48[i] = 0;
			table54[i] = 0;
			table62[i] = 0;
			table70[i] = 0;
			table78[i] = 0;
			table84[i] = 0;
		}
		for ( int i = 1; i < P_DEGREE; i++ ) {
			mods[i] = mods[i - 1] << 1;
			if ( ( mods[i - 1] & X_P_DEGREE ) != 0 ) {
				mods[i] = mods[i] ^ POLY;
			}
		}
		for ( int i = 0; i < 256; i++ ) {
			long c = i;
			for ( int j = 0; j < 8 && c != 0; j++ ) {
				if ( ( c & 1 ) != 0 ) {
					table32[i] = table32[i] ^ mods[j];
					table40[i] = table40[i] ^ mods[j + 8];
					table48[i] = table48[i] ^ mods[j + 16];
					table54[i] = table54[i] ^ mods[j + 24];
					table62[i] = table62[i] ^ mods[j + 32];
					table70[i] = table70[i] ^ mods[j + 40];
					table78[i] = table78[i] ^ mods[j + 48];
					table84[i] = table84[i] ^ mods[j + 56];
				}
				c >>>= 1;
			}
		}
	}
	
	/**
	 * Return the Rabin hash value of an array of bytes.
	 */
	public static long hash( byte[] bytes) {
		return hash( bytes, 0, bytes.length, 0);
	}

	/**
	 * Constructs rabin hash of bytes
	 */
	public static long hash( byte[] bytes, int offset, int length, long fi) {
		long f = fi;
		int start = length % 8;
		for ( int i = offset; i < offset + start; i++ ) {
			f = ( f << 8 ) ^ ( bytes[i] & 0xFF );
		}
		for ( int i = offset + start; i < length + offset; i += 8 ) {
			f = table32[(int) (f & 0xFF)]
				^ table40[(int) ((f >>> 8) & 0xFF)]
				^ table48[(int) ((f >>> 16) & 0xFF)]
				^ table54[(int) ((f >>> 24) & 0xFF)]
				^ table62[(int) ((f >>> 32) & 0xFF)]
				^ table70[(int) ((f >>> 40) & 0xFF)]
				^ table78[(int) ((f >>> 48) & 0xFF)]
				^ table84[(int) ((f >>> 56) & 0xFF)]
				^ (long) (bytes[i] << 56)
				^ (long) (bytes[i + 1] << 48)
				^ (long) (bytes[i + 2] << 40)
				^ (long) (bytes[i + 3] << 32)
				^ (long) (bytes[i + 4] << 24)
				^ (long) (bytes[i + 5] << 16)
				^ (long) (bytes[i + 6] << 8)
				^ (long) (bytes[i + 7]);
		}
		return f;
	}

}
