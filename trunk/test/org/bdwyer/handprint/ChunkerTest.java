package org.bdwyer.handprint;

import java.io.File;
import java.io.IOException;

import org.bdwyer.fingerprint.RabinFingerprintLong;
import org.bdwyer.polynomial.Polynomial;

public class ChunkerTest {

	private final static long WINDOW_SIZE = 8;
	
	public static void main( String[] args ) throws Exception {
		testChunkingFiles();
	}

	private static void testChunkingFiles() throws IOException {
		Polynomial p = Polynomial.createIrreducible( 53 );
		RabinFingerprintLong rabin = new RabinFingerprintLong( p, WINDOW_SIZE );
		StreamChunker chunker1 = new StreamChunker( new File( "1.mp3" ), rabin );
		StreamChunker chunker2 = new StreamChunker( new File( "2.mp3" ), rabin );
		StreamChunker chunker3 = new StreamChunker( new File( "3.mp3" ), rabin );

		chunker1.chunk();
		System.out.println( "|chunks| 1: " + chunker1.chunks.size() );
		
		chunker2.chunk();
		System.out.println( "|chunks| 2: " + chunker2.chunks.size() );
		
		chunker3.chunk();
		System.out.println( "|chunks| 3: " + chunker3.chunks.size() );

		HandPrint hand1 = chunker1.getHandPrint();
		HandPrint hand2 = chunker2.getHandPrint();
		HandPrint hand3 = chunker3.getHandPrint();

		double sim12 = 100.0 * (double) HandPrint.countOverlap( hand1, hand2 ) / (double) Math.max( hand1.chunks.size(), hand2.chunks.size() );
		double sim13 = 100.0 * (double) HandPrint.countOverlap( hand1, hand3 ) / (double) Math.max( hand1.chunks.size(), hand3.chunks.size() );
		
		System.out.println( "hand 1: " + hand1 );
		System.out.println( "hand 2: " + hand2 );
		System.out.println( "hand 3: " + hand3 );
		
		System.out.println( "1->2: " + HandPrint.countOverlap( hand1, hand2 ) + " (" + sim12 + "%)" );
		System.out.println( "1->3: " + HandPrint.countOverlap( hand1, hand3 ) + " (" + sim13 + "%)" );
	}

}
