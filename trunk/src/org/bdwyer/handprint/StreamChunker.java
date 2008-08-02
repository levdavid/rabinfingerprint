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

import org.bdwyer.handprint.RabinFingerprinter.WindowVisitor;

public class StreamChunker implements WindowVisitor {

	private final static int MIN_CHUNK_SIZE = 1024;
	private final static int MAX_CHUNK_SIZE = 1024 * 32;
	private final static int CHUNK_BOUNDARY = 0xFFF;
	private final static int WINDOW_SIZE = 64;
	private final static int BUFFER_SIZE = WINDOW_SIZE * 64;

	protected int lastOffset = 0;

	protected final Collection< Integer > offsets = new LinkedHashSet< Integer >();
	protected final List< Chunk > chunks = new ArrayList< Chunk >();
	protected final File file;

	public StreamChunker( File file ) {
		this.file = file;
	}

	public void chunk() throws IOException {
		InputStream stream = new FileInputStream( file );
		try {
			findOffsets( stream );
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

	protected void findOffsets( InputStream stream ) throws IOException {
		lastOffset = 0;
		offsets.clear();
		hash( stream );
	}

	protected void hash( InputStream stream ) throws IOException {
		int bytesRead = 0;

		byte[] buffer = new byte[BUFFER_SIZE];
		int bufferCursor = 0;

		for ( int i = 0; i < WINDOW_SIZE; i++ )
			buffer[i] = (byte) 0;

		byte[] oneByte = new byte[1];

		int offset = 0;
		while ( ( bytesRead = stream.read( oneByte ) ) > 0 ) {
			bufferCursor = addByte( buffer, oneByte[0], bufferCursor );
			long fingerprint = RabinFingerprinter.hash( buffer, bufferCursor, WINDOW_SIZE, 0 );
			offset++;
			bufferCursor++;
			visitWindow( offset, fingerprint );
			if ( offset % ( 1024 * 1024 ) == 0 ) System.out.println( ( offset / 1024 ) + "KB" );
		}
	}

	private int addByte( byte[] buffer, byte b, int bufferCursor ) {
		if ( bufferCursor + WINDOW_SIZE >= buffer.length ) {
			System.arraycopy( buffer, bufferCursor, buffer, 0, WINDOW_SIZE );
			bufferCursor = 0;
		}
		buffer[bufferCursor + WINDOW_SIZE] = b;
		return bufferCursor;
	}

	public void visitWindow( int windowOffset, long fingerprint ) {
		int chunkSize = windowOffset - lastOffset;

		if ( chunkSize < MIN_CHUNK_SIZE ) {
			return;
		} else if ( chunkSize >= MAX_CHUNK_SIZE ) {
			addChunkOffset( windowOffset );
		} else if ( ( fingerprint & CHUNK_BOUNDARY ) == 0 ) {
			addChunkOffset( windowOffset );
		}
	}

	protected void addChunkOffset( int windowOffset ) {
		offsets.add( windowOffset );
		lastOffset = windowOffset;
	}

	protected void fingerprintChunks( InputStream stream ) throws IOException {
		chunks.clear();
		int i0 = 0;
		for ( int i1 : offsets ) {
			int size = i1 - i0;
			if ( size == 0 ) continue;
			try {
				int bytesRead = 0;
				byte[] bytes = new byte[size];
				// read all bytes in chunk
				while ( bytesRead < size ) {
					bytesRead += stream.read( bytes, bytesRead, size - bytesRead );
				}
				MessageDigest sha = MessageDigest.getInstance( "SHA" );
				sha.update( bytes, 0, size );
				chunks.add( new Chunk( i0, i1, sha.digest() ) );
			} catch ( NoSuchAlgorithmException e ) {
			} finally {
				i0 = i1;
			}
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

	public static void main( String[] args ) throws Exception {

		StreamChunker chunker1 = new StreamChunker( new File( "1.mp3" ) );
		StreamChunker chunker2 = new StreamChunker( new File( "2.mp3" ) );
		StreamChunker chunker3 = new StreamChunker( new File( "3.mp3" ) );

		chunker1.chunk();
		chunker2.chunk();
		chunker3.chunk();

		System.out.println( "1: " + chunker1.chunks.size() + "\n2: " + chunker2.chunks.size() + "\n3: " + chunker3.chunks.size() );

		HandPrint hand1 = chunker1.getHandPrint();
		HandPrint hand2 = chunker2.getHandPrint();
		HandPrint hand3 = chunker3.getHandPrint();

		double sim12 = 100.0 * (double) HandPrint.countOverlap( hand1, hand2 ) / (double) Math.max( hand1.chunks.size(), hand2.chunks.size() );
		double sim13 = 100.0 * (double) HandPrint.countOverlap( hand1, hand3 ) / (double) Math.max( hand1.chunks.size(), hand3.chunks.size() );
		System.out.println( "1: " + hand1 + "\n2: " + hand2 + "\n3: " + hand3 );
		System.out.println( "1->2: " + HandPrint.countOverlap( hand1, hand2 ) + " (" + sim12 + "%)" + "\n1->3: " + HandPrint.countOverlap( hand1, hand2 )
				+ " (" + sim13 + "%)" );
	}

}
