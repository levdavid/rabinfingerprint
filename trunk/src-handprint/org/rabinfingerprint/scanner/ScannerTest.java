package org.rabinfingerprint.scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.rabinfingerprint.handprint.FingerFactory;
import org.rabinfingerprint.handprint.Handprint;
import org.rabinfingerprint.polynomial.Polynomial;
import org.rabinfingerprint.polynomial.Stats;

public class ScannerTest {
	public static void main( String[] args ) throws Exception {
		testMatcher();
		//testConcurrency();
	}

	private static void testMatcher() throws FileNotFoundException {
		MatchModel matcher = new MatchModel();
		matcher.getMatches( "samples/one", "samples/two" );
	}

	private static Collection< Handprint > fingerprintPath( final Polynomial p, final String path ) throws FileNotFoundException {
		final File dir = new File( path );
		final List< File > files = FileListing.getFileListing( dir );
		final List< Handprint > hands = new ArrayList< Handprint >();

		FingerFactory factory = new FingerFactory( p, FingerFactory.WINDOW_SIZE );
		
		long start = System.currentTimeMillis();

		for ( File file : files ) {
			if ( !file.isFile() ) continue;
			Handprint hand = new Handprint( file, factory );
			hands.add( hand );
			System.out.println( "Thumbprinting " + file.toString() );
			System.out.println( Long.toHexString( hand.getPalm() ).toUpperCase() );
		}

		long end = System.currentTimeMillis();
		System.out.println( "That took " + ( end - start ) / 1000.0 + " seconds" );

		return hands;
	}
	
	private static void testSpeed() throws IOException {

		final File dir = new File( "W:\\bdwyer\\mp3\\Soundtracks" );
		final List< File > files = FileListing.getFileListing( dir );

		final Polynomial p = Polynomial.createIrreducible( 53 );
		FingerFactory factory = new FingerFactory( p, FingerFactory.WINDOW_SIZE );
		
		final Stats statsThumb = new Stats();
		final Stats statsHand = new Stats();
		for ( File file : files ) {
			if ( !file.isFile() ) continue;
			if ( file.length() < 10000 ) continue;
			
			final long size = file.length();
			final double kb = ( size / 1024 );
			final long start = System.currentTimeMillis();
			final Handprint hand = new Handprint( file, factory );
			hand.getPalm();
			final long endThumb = System.currentTimeMillis();
			statsThumb.add( kb / ( endThumb - start ) * 1000.0 );
			System.out.println( "Average Thumb Speed " + (int) ( statsThumb.average() ) + " KB/s" );
			
//			hand.getAllFingers();
//			long endHand = System.currentTimeMillis();
//			statsHand.accumulate( endHand - start );
//			System.out.println( "Average Hand Speed " + (int) ( kb / statsHand.average() * 1000.0 ) + " KB/s" );
		}
	}
	
	static final class FingerRunnable implements Runnable {
		private final Handprint hand;
		private final Stats sizeStats;
		private final Stats timeStats;
		private final CountDownLatch latch;

		public FingerRunnable( Handprint hand, Stats sizeStats, Stats timeStats, CountDownLatch latch  ) {
			this.hand = hand;
			this.sizeStats = sizeStats;
			this.timeStats = timeStats;
			this.latch = latch;
		}

		public void run() {
			final double kb = ( hand.getFile().length() / 1024 );
			final long start = System.currentTimeMillis();
			hand.getPalm();
			final long endThumb = System.currentTimeMillis();
			sizeStats.add( kb );
			timeStats.add( ( endThumb - start ) / 1000.0 );
			System.out.println( "Average Thumb Speed " + (int) ( sizeStats.sum() / timeStats.sum() ) + " KB/s" );
			System.out.flush();
			latch.countDown();
		}
	}
	
	private static void testConcurrency() throws IOException {

		final File dir = new File( "W:\\bdwyer\\mp3\\Soundtracks\\Hackers" );
		final List< File > files = FileListing.getFileListing( dir );

		final Polynomial p = Polynomial.createIrreducible( 53 );
		final FingerFactory factory = new FingerFactory( p, FingerFactory.WINDOW_SIZE );
		
		final List< Handprint > hands = new ArrayList<Handprint>();
		for ( File file : files ) {
			if ( !file.isFile() ) continue;
			if ( file.length() < 10000 ) continue;
			hands.add(  new Handprint( file, factory ) );
		}

		final CountDownLatch latch = new CountDownLatch( hands.size() );
		final ExecutorService executor = Executors.newFixedThreadPool( 2 );
		final Stats sizeStats = new Stats();
		final Stats timeStats = new Stats();
		final long start = System.currentTimeMillis();
		for ( Handprint hand : hands ) {
			executor.submit( new FingerRunnable( hand, sizeStats, timeStats, latch ) );
		}
		
		try {
			latch.await(); // wait for all to finish
		} catch ( InterruptedException ie ) {
		} finally {
			executor.shutdown();
			
			final long end = System.currentTimeMillis();
			System.out.println( "Overall Thumb Speed " + (int) ( sizeStats.sum() / ( end - start ) * 1000.0 ) + " KB/s" );
			System.out.flush();
		}
	}
}
