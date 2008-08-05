package org.bdwyer.scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import org.bdwyer.handprint.HandPrint;
import org.bdwyer.handprint.HandprintUtils;
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
		for ( HandPrint hand : handsA ) {
			thumbMapA.put( hand.getThumb(), hand );
		}

		TreeMap< Long, HandPrint > thumbMapB = new TreeMap< Long, HandPrint >();
		for ( HandPrint hand : handsB ) {
			thumbMapB.put( hand.getThumb(), hand );
		}
		
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

	private void findPartialMatches( Collection< HandPrint > handsA, Collection< HandPrint > handsB ) {
		System.out.println("fingerprinting " + (handsA.size() + handsB.size()) + " files");

		// build all fingers
		TreeMap< Long, HandPrint > handMapA = new TreeMap< Long, HandPrint >();
		for ( HandPrint hand : handsA ) {
			for ( Long finger : hand.getHandFingers() ) {
				handMapA.put( finger, hand );
			}
		}

		TreeMap< Long, HandPrint > handMapB = new TreeMap< Long, HandPrint >();
		for ( HandPrint hand : handsB ) {
			for ( Long finger : hand.getHandFingers() ) {
				handMapB.put( finger, hand );
			}
		}

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

		for ( File file : files ) {
			if ( !file.isFile() ) continue;
			HandPrint hand = new HandPrint( file, p );
			hands.add( hand );
		}

		return hands;
	}
	
	
	
}
