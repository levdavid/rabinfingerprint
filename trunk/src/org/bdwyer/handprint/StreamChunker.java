package org.bdwyer.handprint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.bdwyer.fingerprint.Fingerprint;
import org.bdwyer.fingerprint.RabinFingerprintLong;
import org.bdwyer.polynomial.Polynomial;

public class StreamChunker {

	private final static long CHUNK_BOUNDARY = 0x3FF;
	private final static int BUFFER_SIZE = 4096;

	protected final File file;
	protected final RabinFingerprintLong fingerprinter;
	protected final Collection< Integer > offsets = new LinkedHashSet< Integer >();
	protected final List< Chunk > chunks = new ArrayList< Chunk >();

	public StreamChunker( File file, RabinFingerprintLong fingerprinter ) {
		this.file = file;
		this.fingerprinter = fingerprinter;
	}

	public void chunk() throws IOException {
		InputStream stream = new FileInputStream( file );
		try {
			findChunkBoundaries( stream );
		} finally {
			stream.close();
		}

		stream = new FileInputStream( file );
		try {
			fingerprintChunks( stream );
		} finally {
			stream.close();
		}
	}

	protected void findChunkBoundaries( InputStream stream ) throws IOException {

		offsets.clear();
		fingerprinter.reset();

		int offset = 0;
		int lastOffset = 0;
		int bytesRead = 0;

		byte[] buffer = new byte[BUFFER_SIZE];
		while ( ( bytesRead = stream.read( buffer ) ) >= 0 ) {
			for ( int i = 0; i < bytesRead; i++ ) {
				fingerprinter.pushByte( buffer[i] );

				int chunkSize = offset - lastOffset;
				if ( ( fingerprinter.getFingerprintLong() & CHUNK_BOUNDARY ) == CHUNK_BOUNDARY ) {
					offsets.add( offset );
					lastOffset = offset;
				}

				offset++;
			}
			// if ( offset % (1024 * 1024) == 0 ) System.out.println( ( offset /
			// 1024 ) + "KB" );
		}
	}

	protected void fingerprintChunks( InputStream stream ) throws IOException {
		chunks.clear();
		int i0 = 0;
		for ( int i1 : offsets ) {
			int size = i1 - i0;
			if ( size == 0 ) continue;
			
			int bytesRead = 0;
			byte[] bytes = new byte[size];
			
			// read all bytes in chunk
			while ( bytesRead < size ) {
				bytesRead += stream.read( bytes, bytesRead, size - bytesRead );
			}

			fingerprinter.reset();
			fingerprinter.pushBytes( bytes, 0, size );
			chunks.add( new Chunk( i0, i1, fingerprinter.getFingerprintLong() ) );
			i0 = i1;
		}
	}

	public Collection< Integer > getOffsets() {
		return offsets;
	}

	protected List< Chunk > getChunks() {
		return chunks;
	}

	public HandPrint getHandPrint() {
		return new HandPrint( chunks );
	}

}
