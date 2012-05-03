package org.rabinfingerprint.handprint;

import java.io.File;
import java.io.IOException;

import org.rabinfingerprint.polynomial.Polynomial;
import org.rabinfingerprint.polynomial.Stats;

public class HandprintTest {
	public static void main( String[] args ) throws Exception {
		testChunkingFiles();
		testSpeed();
	}

	private static void testChunkingFiles() throws IOException {
		Polynomial p = Polynomial.createIrreducible(53);
		FingerFactory factory = new FingerFactory(p, FingerFactory.WINDOW_SIZE);
		Handprint hand1 = new Handprint(new File("samples/1.mp3"), factory);
		Handprint hand2 = new Handprint(new File("samples/2.mp3"), factory);
		Handprint hand3 = new Handprint(new File("samples/3.mp3"), factory);
		Handprint hand4 = new Handprint(new File("samples/4.mp3"), factory);

		System.out.println("thumb 1: " + hand1.getPalm());
		System.out.println("thumb 2: " + hand2.getPalm());
		System.out.println("thumb 3: " + hand3.getPalm());
		System.out.println("thumb 4: " + hand4.getPalm());

		System.out.println("|chunks| 1: " + hand1.getFingerCount());
		System.out.println("|chunks| 2: " + hand2.getFingerCount());
		System.out.println("|chunks| 3: " + hand3.getFingerCount());
		System.out.println("|chunks| 4: " + hand4.getFingerCount());

		System.out.println("hand 1: " + hand1.toString());
		System.out.println("hand 2: " + hand2.toString());
		System.out.println("hand 3: " + hand3.toString());
		System.out.println("hand 4: " + hand4.toString());

		double sim12 = 100.0 * Handprints.getSimilarity(hand1, hand2);
		double sim13 = 100.0 * Handprints.getSimilarity(hand1, hand3);
		double sim14 = 100.0 * Handprints.getSimilarity(hand1, hand4);

		System.out.println("1->2: " + Handprints.countOverlap(hand1, hand2) + " (" + sim12 + "%)");
		System.out.println("1->3: " + Handprints.countOverlap(hand1, hand3) + " (" + sim13 + "%)");
		System.out.println("1->4: " + Handprints.countOverlap(hand1, hand4) + " (" + sim14 + "%)");
	}
	
	private static void testSpeed() throws IOException {
		final Polynomial p = Polynomial.createIrreducible( 53 );
		final FingerFactory factory = new FingerFactory( p, FingerFactory.WINDOW_SIZE );
		
		final File file = new File( "samples/1.mp3" );
		final long size = file.length();
		final double kb = (size / 1024);
		System.out.println( "File Size: " + ( size / 1024 ) + " KB" );

		final Stats statsThumb = new Stats();
		final Stats statsHand = new Stats();
		while ( true ) {
			long start = System.currentTimeMillis();
			Handprint hand = new Handprint( file, factory );
			hand.getPalm();
			long endThumb = System.currentTimeMillis();
			statsThumb.add( endThumb - start );
			
			hand.getAllFingers();
			long endHand = System.currentTimeMillis();
			statsHand.add( endHand - start );

			System.out.println( "Average Thumb Speed " + (int) ( kb / statsThumb.average() * 1000.0 ) + " KB/s" );
			System.out.println( "Average Hand Speed " + (int) ( kb / statsHand.average() * 1000.0 ) + " KB/s" );
		}
	}
}
