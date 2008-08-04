package org.bdwyer.handprint;

import java.io.File;
import java.io.FileInputStream;

import org.bdwyer.fingerprint.RabinFingerprintLong;
import org.bdwyer.polynomial.Polynomial;

public class Thumbprinter {

	/** 4 KB = 1 OS Page for most platforms */
	private final static int BUFFER_SIZE = 4096;

	public static long getThumbprint( File file, Polynomial p ) {
		final RabinFingerprintLong fingerprinter = new RabinFingerprintLong( p );
		final FileInputStream stream = StreamWrapper.getStream( file );

		while ( true ) {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = StreamWrapper.getBytes( stream, buffer );
			if ( bytesRead < 0 ) break;
			fingerprinter.pushBytes( buffer, 0, bytesRead );
		}
		StreamWrapper.closeStream( stream );

		return fingerprinter.getFingerprintLong();
	}

}
