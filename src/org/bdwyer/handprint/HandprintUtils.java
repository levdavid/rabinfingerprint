package org.bdwyer.handprint;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bdwyer.fingerprint.RabinFingerprintLong;
import org.bdwyer.fingerprint.RabinFingerprintLongWindowed;
import org.bdwyer.fingerprint.Fingerprint.FingerprintFactory;
import org.bdwyer.polynomial.Polynomial;

public class HandprintUtils {

	@SuppressWarnings( "serial" )
	public static class HandprintException extends RuntimeException {
		public HandprintException( String msg, Throwable wrapped ) {
			super( msg, wrapped );
		}
	}

	public static class HandprintFactory implements FingerprintFactory< Polynomial > {
		private final RabinFingerprintLong finger;
		private final RabinFingerprintLongWindowed fingerWindow;
	
		public HandprintFactory( Polynomial p, long bytesPerWindow ) {
			this.finger = new RabinFingerprintLong( p );
			this.fingerWindow = new RabinFingerprintLongWindowed( p, bytesPerWindow );
		}
	
		public RabinFingerprintLong getFingerprint() {
			return finger;
		}
	
		public RabinFingerprintLongWindowed getWindowedFingerprint() {
			return fingerWindow;
		}
	}

	/**
	 * Assumes chunk lists are reverse sorted
	 */
	public static int countOverlap( HandPrint ha, HandPrint hb ) {
		int matches = 0;

		final Iterator<Long> ia = ha.chunks.iterator();
		final Iterator<Long> ib = hb.chunks.iterator();

		if ( ia.hasNext() == false || ib.hasNext() == false ) return matches;

		Long ac = ia.next();
		Long bc = ib.next();
		int cmp = ac.compareTo( bc );

		while ( true ) {
			while ( cmp > 0 ) {
				if ( ia.hasNext() == false ) return matches;
				ac = ia.next();
				cmp = ac.compareTo( bc );
			}
			while ( cmp < 0 ) {
				if ( ib.hasNext() == false ) return matches;
				bc = ib.next();
				cmp = ac.compareTo( bc );
			}
			while ( cmp == 0 ) {
				matches++;
				if ( ia.hasNext() == false || ib.hasNext() == false ) return matches;
				ac = ia.next();
				bc = ib.next();
				cmp = ac.compareTo( bc );
			}
		}
	}
	
	public static double getSimilarity( HandPrint ha, HandPrint hb ) {
		return (double) HandprintUtils.countOverlap( ha, hb ) / (double) Math.max( ha.getFingerCount(), hb.getFingerCount() );
	}

	/** 4 KB = 1 OS Page for most platforms */
	private final static int BUFFER_SIZE = 65536;

	public static long getThumbprint( File file, RabinFingerprintLong fingerprinter ) {
		final FileInputStream stream = StreamWrapper.getStream( file );
		final byte[] buffer = new byte[BUFFER_SIZE];
		fingerprinter.reset();
		
		int bytesRead;
		while ( ( bytesRead = StreamWrapper.getBytes( stream, buffer ) ) >= 0 ) {
			fingerprinter.pushBytes( buffer, 0, bytesRead );
		}
		StreamWrapper.closeStream( stream );
		return fingerprinter.getFingerprintLong();
	}

	public final static long CHUNK_BOUNDARY = 0xFFF;
	public final static long CHUNK_PATTERN = 0xABC;
	public final static long WINDOW_SIZE = 8;

	public static List<Long> getOffsets( File file, RabinFingerprintLongWindowed fingerprinter ) {
		final FileInputStream stream = StreamWrapper.getStream( file );
		final List<Long> offsets = new ArrayList<Long>();
		final byte[] buffer = new byte[BUFFER_SIZE];
		fingerprinter.reset();

		int offset = 0;
		int bytesRead;
		while ( ( bytesRead = StreamWrapper.getBytes( stream, buffer ) ) >= 0 ) {
			// determine offsets
			for ( int i = 0; i < bytesRead; i++ ) {
				fingerprinter.pushByte( buffer[i] );
				if ( ( fingerprinter.getFingerprintLong() & CHUNK_BOUNDARY ) == CHUNK_PATTERN ) {
					offsets.add( new Long( offset + i ) );
				}
				offset++;
			}
		}
		StreamWrapper.closeStream( stream );
		return offsets;
	}

	public static List<Long> getChunks( File file, RabinFingerprintLong fingerprinter, List<Long> offsets ) {
		final FileInputStream stream = StreamWrapper.getStream( file );
		final List<Long> chunks = new ArrayList<Long>();
		fingerprinter.reset();
		
		long i0 = 0;
		for ( long i1 : offsets ) {
			long size = i1 - i0;
			if ( size <= 0 ) continue;

			byte[] buffer = new byte[(int) size];
			int bytesRead = StreamWrapper.getBytes( stream, buffer );
			if ( bytesRead < 0 ) break;

			fingerprinter.reset();
			fingerprinter.pushBytes( buffer );
			long f = fingerprinter.getFingerprintLong();
			chunks.add( f );
			i0 = i1;
		}
		StreamWrapper.closeStream( stream );
		return chunks;

	}

}
