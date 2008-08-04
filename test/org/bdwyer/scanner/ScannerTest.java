package org.bdwyer.scanner;

import java.io.File;
import java.util.List;

import org.bdwyer.handprint.HandPrint;
import org.bdwyer.polynomial.Polynomial;

public class ScannerTest {
	public static void main( String[] args ) throws Exception {

		final Polynomial p = Polynomial.createIrreducible( 53 );
		final String path = "U:/Photo/gallery/2001/Misc";
		final File dir = new File( path );
		final List<File> files = FileListing.getFileListing( dir );

		long start = System.currentTimeMillis();
		
		for ( File file : files ) {
			if ( !file.isFile() ) continue;
			HandPrint hand = new HandPrint( file, p );
			System.out.println( "Thumbprinting " + file.toString() );
			System.out.println( Long.toHexString( hand.getThumbprint() ).toUpperCase() );
		}

		long end = System.currentTimeMillis();
		System.out.println( "That took " + (end - start)/1000.0 + " seconds");
	}
}
