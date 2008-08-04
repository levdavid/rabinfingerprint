package org.bdwyer.handprint;

import java.util.Iterator;

public class HandprintUtils {

	@SuppressWarnings("serial")
	public static class HandprintException extends RuntimeException{ 
		public HandprintException( String msg, Throwable wrapped ) {
			super( msg, wrapped );
		}
	}
	
	/**
	 * Assumes chunk lists are reverse sorted
	 */
	public static int countOverlap( HandPrint ha, HandPrint hb ) {
		int matches = 0;

		final Iterator< Chunk > ia = ha.chunks.iterator();
		final Iterator< Chunk > ib = hb.chunks.iterator();

		if ( ia.hasNext() == false || ib.hasNext() == false ) return matches;

		Chunk ac = ia.next();
		Chunk bc = ib.next();
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
}
