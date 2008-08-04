package org.bdwyer.handprint;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bdwyer.polynomial.Polynomial;

public class HandPrint {

	private static final int FINGERS = 20;

	protected final File file;
	protected final Polynomial p;

	protected Collection<Integer> offsets;
	protected List<Chunk> chunks;
	protected List<Chunk> hand;
	protected Long thumbprint;

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
		thumbprint = Thumbprinter.getThumbprint( file, p );
		return thumbprint;
	}

	public Collection<Integer> getOffsets() {
		if ( offsets != null ) return offsets;
		offsets = ChunkFinder.getOffsets( file, p );
		return offsets;
	}

	public List<Chunk> getChunks() {
		if ( chunks != null ) return chunks;
		chunks = Chunker.getChunks( file, p, getOffsets() );
		// 0's are common, so we reverse sort
		Collections.sort( chunks );
		Collections.reverse( chunks );
		return chunks;
	}

	public List<Chunk> getHand() {
		if ( hand != null ) return hand;
		hand = new ArrayList<Chunk>( FINGERS );
		for ( int i = 0; i < FINGERS && i < getChunks().size(); i++ ) {
			hand.add( getChunks().get( i ) );
		}
		return hand;
	}

	@Override
	public String toString() {
		return getHand().toString();
	}

}
