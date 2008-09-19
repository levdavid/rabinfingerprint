package org.bdwyer.scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bdwyer.fingerprint.RabinFingerprintLong;
import org.bdwyer.fingerprint.RabinFingerprintLongWindowed;
import org.bdwyer.handprint.HandPrint;
import org.bdwyer.handprint.HandprintUtils;
import org.bdwyer.handprint.HandprintUtils.HandprintFactory;
import org.bdwyer.polynomial.Polynomial;

public class MatchModel {

	public static abstract class Match {
		protected final HandPrint a, b;
		

		private Match( HandPrint a, HandPrint b ) {
			this.a = a;
			this.b = b;
		}

		public HandPrint getHandA() {
			return a;
		}

		public HandPrint getHandB() {
			return b;
		}
		
		public abstract double getSimilarity();

	}

	public static class ExactMatch extends Match {
		private ExactMatch( HandPrint a, HandPrint b ) {
			super( a, b );
		}
		
		@Override
		public double getSimilarity() {
			return 1.0;
		}
	}
	
	public static class PartialMatch extends Match {
		protected Double similarity;
		private PartialMatch( HandPrint a, HandPrint b ) {
			super( a, b );
		}

		@Override
		public double getSimilarity() {
			if( similarity == null ) similarity = HandprintUtils.getSimilarity( a, b );
			return similarity;
		}
	}
	
	public static class NonMatch extends Match {
		private NonMatch( HandPrint a, HandPrint b ) {
			super( a, b );
		}
		
		@Override
		public double getSimilarity() {
			return 0.0;
		}
	}

	protected List<Match> matches = new ArrayList<Match>();

	public void getMatches( String pathA, String pathB ) throws FileNotFoundException {
		final Polynomial p = Polynomial.createIrreducible( 53 );

		Collection< HandPrint > handsA = getHandsFromPath( p, pathA );
		Collection< HandPrint > handsB = getHandsFromPath( p, pathB );

		findExactMatches( handsA, handsB );
		findPartialMatches( handsA, handsB );
		findNonMatches( handsA, handsB );
	}

	private void findExactMatches( Collection< HandPrint > handsA, Collection< HandPrint > handsB ) {
		System.out.println("thumbprinting " + (handsA.size() + handsB.size()) + " files");

		// thumbprint files
		TreeMap< Long, HandPrint > thumbMapA = new TreeMap< Long, HandPrint >();
		TreeMap< Long, HandPrint > thumbMapB = new TreeMap< Long, HandPrint >();
		
		thumbprintTasks( handsA, handsB, thumbMapA, thumbMapB );
		System.out.print("\n");
		
		List<Long> thumbsA = new ArrayList< Long >( thumbMapA.keySet() );

		// print intersection
		for ( Long thumb : thumbsA ) {
			if ( thumbMapB.containsKey( thumb ) ) {
				HandPrint matchA = thumbMapA.get( thumb );
				HandPrint matchB = thumbMapB.get( thumb );
				
				// found exact match
				handsA.remove( matchA );
				handsB.remove( matchB );

				matches.add( new ExactMatch( matchA, matchB ) );
				
				StringBuffer str = new StringBuffer();
				str.append( "Found exact match between " );
				str.append( matchA.getFile().toString() );
				str.append( " and " );
				str.append( matchB.getFile().toString() );
				System.out.println( str.toString() );
			}
		}
	}

	private void thumbprintTasks(
			final Collection< HandPrint > handsA, final Collection< HandPrint > handsB,
			final TreeMap< Long, HandPrint > thumbMapA, final TreeMap< Long, HandPrint > thumbMapB ) {
		
		final CountDownLatch doneSignal = new CountDownLatch( 2 );
		final ExecutorService executor = Executors.newFixedThreadPool( 2 );

		final class ThumbRunnable implements Runnable {
			private final Collection< HandPrint > hands;
			private final TreeMap< Long, HandPrint > map;

			public ThumbRunnable( Collection< HandPrint > hands, TreeMap< Long, HandPrint > map ) {
				super();
				this.hands = hands;
				this.map = map;
			}

			public void run() {
				for ( HandPrint hand : hands ) {
					map.put( hand.getThumb(), hand );
					System.out.print( "." );
					System.out.flush();
				}
				doneSignal.countDown();
			}
		}
		
		executor.execute( new ThumbRunnable( handsA, thumbMapA ) );
		executor.execute( new ThumbRunnable( handsB, thumbMapB ) );
				
		try {
			doneSignal.await(); // wait for all to finish
		} catch ( InterruptedException ie ) {
		}
		executor.shutdown();

	}

	private void findPartialMatches( Collection< HandPrint > handsA, Collection< HandPrint > handsB ) {
		System.out.println("handprinting " + (handsA.size() + handsB.size()) + " files");

		// build all fingers
		TreeMap< Long, HandPrint > handMapA = new TreeMap< Long, HandPrint >();
		TreeMap< Long, HandPrint > handMapB = new TreeMap< Long, HandPrint >();
		
		handprintTasks( handsA, handsB, handMapA, handMapB );

		// print intersection
		List<Long> fingersA = new ArrayList< Long >( handMapA.keySet() );
		for ( Long finger : fingersA ) {
			if ( handMapB.containsKey( finger ) ) {
				HandPrint matchA = handMapA.get( finger );
				HandPrint matchB = handMapB.get( finger );
				
				// found partial match
				handsA.remove( matchA );
				handsB.remove( matchB );

				matches.add( new PartialMatch( matchA, matchB ) );
				
				StringBuffer str = new StringBuffer();
				str.append( "Found partial match between " );
				str.append( matchA.getFile().toString() );
				str.append( " and " );
				str.append( matchB.getFile().toString() );
				str.append( " with similarity " + ( 100.0 * HandprintUtils.getSimilarity( matchA, matchB ) ) );
				System.out.println( str.toString() );
			}
		}
	}

	private void handprintTasks(
			Collection< HandPrint > handsA, Collection< HandPrint > handsB,
			TreeMap< Long, HandPrint > handMapA, TreeMap< Long, HandPrint > handMapB ) {
		
		final CountDownLatch doneSignal = new CountDownLatch( 2 );
		final ExecutorService executor = Executors.newFixedThreadPool( 2 );

		final class HandRunnable implements Runnable {
			private final Collection< HandPrint > hands;
			private final TreeMap< Long, HandPrint > map;

			public HandRunnable( Collection< HandPrint > hands, TreeMap< Long, HandPrint > map ) {
				super();
				this.hands = hands;
				this.map = map;
			}

			public void run() {
				for ( HandPrint hand : hands ) {
					for ( Long finger : hand.getHandFingers() ) {
						map.put( finger, hand );
					}
					System.out.print( "." );
					System.out.flush();
				}
				doneSignal.countDown();
			}
		}

		executor.execute( new HandRunnable( handsA, handMapA ) );
		executor.execute( new HandRunnable( handsB, handMapB ) );

		try {
			doneSignal.await(); // wait for all to finish
		} catch ( InterruptedException ie ) {
		}
		executor.shutdown();
	}
	
	private void findNonMatches( Collection< HandPrint > handsA, Collection< HandPrint > handsB ) {
		for( HandPrint hand : handsA ){
			matches.add( new NonMatch( hand, null ) );

			StringBuffer str = new StringBuffer();
			str.append( "Found no match for " );
			str.append( hand.getFile().toString() );
			System.out.println( str.toString() );
		}
		
		for( HandPrint hand : handsB ){
			matches.add( new NonMatch( null, hand ) );
			
			StringBuffer str = new StringBuffer();
			str.append( "Found no match for " );
			str.append( hand.getFile().toString() );
			System.out.println( str.toString() );
		}
	}

	private static Collection< HandPrint > getHandsFromPath( final Polynomial p, final String path ) throws FileNotFoundException {
		final File dir = new File( path );
		final List< File > files = FileListing.getFileListing( dir );
		final List< HandPrint > hands = new ArrayList< HandPrint >();
		final HandprintUtils.HandprintFactory factory = new HandprintUtils.HandprintFactory( p, HandprintUtils.WINDOW_SIZE );

		for ( File file : files ) {
			if ( !file.isFile() ) continue;
			HandPrint hand = new HandPrint( file, factory );
			hands.add( hand );
		}

		return hands;
	}
}
