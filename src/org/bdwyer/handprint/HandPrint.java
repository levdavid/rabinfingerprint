package org.bdwyer.handprint;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bdwyer.polynomial.Polynomial;

public class HandPrint {

	private static final int FINGERS = 10;

	protected final File file;
	protected final Polynomial p;

	protected Long thumbprint;
	protected List<Long> offsets;
	protected List<Long> chunks;
	protected List<Long> hand;

	public HandPrint( File file, Polynomial p ) {
		this.file = file;
		this.p = p;
	}
	
	public void buildAll(){
		getThumbprint();
		getOffsets();
		getChunks();
		getHand();
	}

	public Long getThumbprint() {
		if ( thumbprint != null ) return thumbprint;
		thumbprint = HandprintUtils.getThumbprint( file, p );
		return thumbprint;
	}

	public List<Long> getOffsets() {
		if ( offsets != null ) return offsets;
		offsets = HandprintUtils.getOffsets( file, p );
		return offsets;
	}

	public List<Long> getChunks() {
		if ( chunks != null ) return chunks;
		chunks = HandprintUtils.getChunks( file, p, getOffsets() );
		Collections.sort( chunks, Collections.reverseOrder() );
		return chunks;
	}
	
	public List<Long> getHand() {
		if ( hand != null ) return hand;
		hand = new ArrayList<Long>( FINGERS );
		for ( int i = 0; i < FINGERS && i < getChunks().size(); i++ ) {
			hand.add( getChunks().get( i ) );
		}
		return hand;
	}

	public int getChunkCount(){
		return getChunks().size();
	}

	@Override
	public String toString() {
		return getHand().toString();
	}

}
