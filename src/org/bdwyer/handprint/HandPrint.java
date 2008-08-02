package org.bdwyer.handprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;



public class HandPrint {

	private static final int FINGERS = 20;

	protected final List< Chunk > chunks;
	protected final List< Chunk > hand;

	public HandPrint( List< Chunk > chunks ) {
		this.chunks = chunks;
		Collections.sort( chunks );
		Collections.reverse( chunks ); // 0's are common, so we reverse sort
		this.hand = new ArrayList< Chunk >( FINGERS );
		for ( int i = 0; i < FINGERS && i < chunks.size(); i++ ) {
			this.hand.add( this.chunks.get( i ) );
		}
	}

	@Override
	public String toString() {
		return this.hand.toString();
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
