package org.bdwyer.handprint;

import java.io.File;
import java.io.IOException;

import org.bdwyer.polynomial.Polynomial;

public class ChunkerTest {
	
	public static void main( String[] args ) throws Exception {
		testChunkingFiles();
	}

	private static void testChunkingFiles() throws IOException {
		Polynomial p = Polynomial.createIrreducible( 53 );
		HandPrint hand1 = new HandPrint( new File( "1.mp3" ), p );
		HandPrint hand2 = new HandPrint( new File( "2.mp3" ), p );
		HandPrint hand3 = new HandPrint( new File( "3.mp3" ), p );
		HandPrint hand4 = new HandPrint( new File( "4.mp3" ), p );
		
		System.out.println( "thumb 1: " + hand1.getThumbprint() );
		System.out.println( "thumb 2: " + hand2.getThumbprint() );
		System.out.println( "thumb 3: " + hand3.getThumbprint() );
		System.out.println( "thumb 4: " + hand4.getThumbprint() );

		System.out.println( "|chunks| 1: " + hand1.getChunkCount() );
		System.out.println( "|chunks| 2: " + hand2.getChunkCount() );
		System.out.println( "|chunks| 3: " + hand3.getChunkCount() );
		System.out.println( "|chunks| 4: " + hand4.getChunkCount() );

		System.out.println( "hand 1: " + hand1.toString() );
		System.out.println( "hand 2: " + hand2.toString() );
		System.out.println( "hand 3: " + hand3.toString() );
		System.out.println( "hand 4: " + hand4.toString() );
		
		double sim12 = 100.0 * (double) HandprintUtils.countOverlap( hand1, hand2 ) / (double) Math.max( hand1.getChunkCount(), hand2.getChunkCount() );
		double sim13 = 100.0 * (double) HandprintUtils.countOverlap( hand1, hand3 ) / (double) Math.max( hand1.getChunkCount(), hand3.getChunkCount() );
		double sim14 = 100.0 * (double) HandprintUtils.countOverlap( hand1, hand4 ) / (double) Math.max( hand1.getChunkCount(), hand4.getChunkCount() );		
		
		System.out.println( "1->2: " + HandprintUtils.countOverlap( hand1, hand2 ) + " (" + sim12 + "%)" );
		System.out.println( "1->3: " + HandprintUtils.countOverlap( hand1, hand3 ) + " (" + sim13 + "%)" );
		System.out.println( "1->4: " + HandprintUtils.countOverlap( hand1, hand4 ) + " (" + sim14 + "%)" );
	}

}
