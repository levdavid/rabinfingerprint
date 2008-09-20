package org.bdwyer.handprint;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import org.bdwyer.datastructures.BidiSortedMap;
import org.bdwyer.datastructures.Interval;
import org.bdwyer.fingerprint.RabinFingerprintLong;

public class HandprintUtils {

	@SuppressWarnings( "serial" )
	public static class HandprintException extends RuntimeException {
		public HandprintException( String msg, Throwable wrapped ) {
			super( msg, wrapped );
		}
	}

	/**
	 * Assumes chunk lists are reverse sorted
	 */
	public static int countOverlap( HandPrint ha, HandPrint hb ) {
		int matches = 0;

		final Iterator<Long> ia = ha.fingers.keyList().iterator();
		final Iterator<Long> ib = hb.fingers.keyList().iterator();

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


}
