package org.bdwyer.scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import org.bdwyer.fingerprint.RabinFingerprintLong;
import org.bdwyer.fingerprint.RabinFingerprintLongWindowed;
import org.bdwyer.handprint.HandPrint;
import org.bdwyer.handprint.HandprintUtils;
import org.bdwyer.polynomial.Polynomial;

public class ScannerTest {
	public static void main( String[] args ) throws Exception {
		MatchModel matcher = new MatchModel();
		matcher.getMatches( "samples/one", "samples/two" );
	}

	private static Collection< HandPrint > fingerprintPath( final Polynomial p, final String path ) throws FileNotFoundException {
		final File dir = new File( path );
		final List< File > files = FileListing.getFileListing( dir );
		final List< HandPrint > hands = new ArrayList< HandPrint >();

		final RabinFingerprintLong finger = new RabinFingerprintLong( p );
		final RabinFingerprintLongWindowed fingerWindow = new RabinFingerprintLongWindowed( p, HandprintUtils.WINDOW_SIZE );
		
		long start = System.currentTimeMillis();

		for ( File file : files ) {
			if ( !file.isFile() ) continue;
			HandPrint hand = new HandPrint( file, finger, fingerWindow );
			hands.add( hand );
			System.out.println( "Thumbprinting " + file.toString() );
			System.out.println( Long.toHexString( hand.getThumb() ).toUpperCase() );
		}

		long end = System.currentTimeMillis();
		System.out.println( "That took " + ( end - start ) / 1000.0 + " seconds" );

		return hands;
	}
}
