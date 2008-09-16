package org.bdwyer.handprint;

import java.io.File;
import java.io.IOException;

import org.bdwyer.fingerprint.RabinFingerprintLong;
import org.bdwyer.fingerprint.RabinFingerprintLongWindowed;
import org.bdwyer.polynomial.Polynomial;

public class ChunkerTest {
	
	public static void main( String[] args ) throws Exception {
		// testChunkingFiles();
		testSpeed();
	}

	private static void testChunkingFiles() throws IOException {
		Polynomial p = Polynomial.createIrreducible( 53 );

		RabinFingerprintLong finger = new RabinFingerprintLong( p );
		RabinFingerprintLongWindowed fingerWindow = new RabinFingerprintLongWindowed( p, HandprintUtils.WINDOW_SIZE );
		
		HandPrint hand1 = new HandPrint( new File( "samples/1.mp3" ), finger, fingerWindow );
		HandPrint hand2 = new HandPrint( new File( "samples/2.mp3" ), finger, fingerWindow );
		HandPrint hand3 = new HandPrint( new File( "samples/3.mp3" ), finger, fingerWindow );
		HandPrint hand4 = new HandPrint( new File( "samples/4.mp3" ), finger, fingerWindow );
		
		System.out.println( "thumb 1: " + hand1.getThumb() );
		System.out.println( "thumb 2: " + hand2.getThumb() );
		System.out.println( "thumb 3: " + hand3.getThumb() );
		System.out.println( "thumb 4: " + hand4.getThumb() );

		System.out.println( "|chunks| 1: " + hand1.getFingerCount() );
		System.out.println( "|chunks| 2: " + hand2.getFingerCount() );
		System.out.println( "|chunks| 3: " + hand3.getFingerCount() );
		System.out.println( "|chunks| 4: " + hand4.getFingerCount() );

		System.out.println( "hand 1: " + hand1.toString() );
		System.out.println( "hand 2: " + hand2.toString() );
		System.out.println( "hand 3: " + hand3.toString() );
		System.out.println( "hand 4: " + hand4.toString() );
		
		double sim12 = 100.0 * HandprintUtils.getSimilarity( hand1, hand2 );
		double sim13 = 100.0 * HandprintUtils.getSimilarity( hand1, hand3 );
		double sim14 = 100.0 * HandprintUtils.getSimilarity( hand1, hand4 );		
		
		System.out.println( "1->2: " + HandprintUtils.countOverlap( hand1, hand2 ) + " (" + sim12 + "%)" );
		System.out.println( "1->3: " + HandprintUtils.countOverlap( hand1, hand3 ) + " (" + sim13 + "%)" );
		System.out.println( "1->4: " + HandprintUtils.countOverlap( hand1, hand4 ) + " (" + sim14 + "%)" );
	}
	
	//  -agentlib:yjpagent
	private static void testSpeed() throws IOException {

		final Polynomial p = Polynomial.createIrreducible( 53 );
		final File file = new File( "samples/1.mp3" );
		final long size = file.length();
		final double kb = (size / 1024);
		System.out.println( "File Size: " + ( size / 1024 ) + " KB" );
		
		RabinFingerprintLong finger = new RabinFingerprintLong( p );
		RabinFingerprintLongWindowed fingerWindow = new RabinFingerprintLongWindowed( p, HandprintUtils.WINDOW_SIZE );

		final Stats statsThumb = new Stats();
		final Stats statsHand = new Stats();
		while ( true ) {
			long start = System.currentTimeMillis();
			HandPrint hand = new HandPrint( file, finger, fingerWindow );
			hand.getThumb();
			long endThumb = System.currentTimeMillis();
			statsThumb.accumulate( endThumb - start );
			
			hand.getAllFingers();
			long endHand = System.currentTimeMillis();
			statsHand.accumulate( endHand - start );

			System.out.println( "Average Thumb Speed " + (int) ( kb / statsThumb.average() * 1000.0 ) + " KB/s" );
			System.out.println( "Average Hand Speed " + (int) ( kb / statsHand.average() * 1000.0 ) + " KB/s" );
		}
	}
	
	public static class Stats {
		long count;
		long accum;

		public void accumulate( long value ) {
			accum += value;
			count++;
		}

		public long average() {
			if ( count == 0 ) return 0;
			return accum / count;
		}
	}

}
