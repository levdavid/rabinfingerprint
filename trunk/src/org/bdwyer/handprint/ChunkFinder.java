package org.bdwyer.handprint;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.bdwyer.fingerprint.RabinFingerprintLong;
import org.bdwyer.polynomial.Polynomial;

public class ChunkFinder extends StreamWrapper {

	private final static long CHUNK_BOUNDARY = 0xFFF;
	private final static long WINDOW_SIZE = 8;
	
	/** 4 KB = 1 OS Page for most platforms */
	private final static int BUFFER_SIZE = 4096;

	public static Collection<Integer> getOffsets( File file, Polynomial p ) {
		final Collection<Integer> offsets = new LinkedHashSet<Integer>();
		final RabinFingerprintLong  fingerprinter = new RabinFingerprintLong( p, WINDOW_SIZE );
		final FileInputStream stream = StreamWrapper.getStream( file );

		int offset = 0;
		while ( true ) {
			// get some bytes
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = StreamWrapper.getBytes( stream, buffer );
			if ( bytesRead < 0 ) break;
			
			// determine offsets
			for ( int i = 0; i < bytesRead; i++ ) {
				fingerprinter.pushByte( buffer[i] );
				if ( ( fingerprinter.getFingerprintLong() & CHUNK_BOUNDARY ) == CHUNK_BOUNDARY ) {
					offsets.add( offset + i );
				}
				offset++;
			}
		}
		StreamWrapper.closeStream( stream );
		
		return offsets;
	}


}
