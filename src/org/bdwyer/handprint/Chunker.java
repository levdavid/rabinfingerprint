package org.bdwyer.handprint;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bdwyer.fingerprint.RabinFingerprintLong;
import org.bdwyer.polynomial.Polynomial;

public class Chunker {


	public static List< Chunk > getChunks( File file, Polynomial  p, Collection<Integer> offsets ) {
		
		final RabinFingerprintLong fingerprinter = new RabinFingerprintLong( p );
		final List<Chunk> chunks = new ArrayList<Chunk>();
		final FileInputStream stream = StreamWrapper.getStream( file );

		int i0 = 0;
		for ( int i1 : offsets ) {
			int size = i1 - i0;
			if ( size <= 0 ) continue;

			byte[] buffer = new byte[size];
			int bytesRead = StreamWrapper.getBytes( stream, buffer );
			if ( bytesRead < 0 ) break;

			fingerprinter.reset();
			fingerprinter.pushBytes( buffer );
			long f = fingerprinter.getFingerprintLong();
			chunks.add( new Chunk( i0, i1, f ) );
			i0 = i1;
		}
		
		StreamWrapper.closeStream( stream );
		return chunks;
		
	}

}
