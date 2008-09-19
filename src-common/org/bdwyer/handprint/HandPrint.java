package org.bdwyer.handprint;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bdwyer.handprint.HandprintUtils.HandprintFactory;

public class HandPrint {
	
	private static final int FINGERS = 10;

	protected final File file;
	protected final HandprintFactory factory;

	protected Long thumbprint;
	protected List< Long > offsets;
	protected List< Long > chunks;
	protected List< Long > hand;

	public HandPrint( File file, HandprintFactory factory ) {
		this.file = file;
		this.factory = factory;
	}
	
	public void buildAll(){
		getThumb();
		getOffsets();
		getAllFingers();
		getHandFingers();
	}

	public Long getThumb() {
		if ( thumbprint != null ) return thumbprint;
		thumbprint = HandprintUtils.getThumbprint( file, factory.getFingerprint() );
		return thumbprint;
	}

	public List<Long> getOffsets() {
		if ( offsets != null ) return offsets;
		offsets = HandprintUtils.getOffsets( file, factory.getWindowedFingerprint() );
		return offsets;
	}

	public List<Long> getAllFingers() {
		if ( chunks != null ) return chunks;
		chunks = HandprintUtils.getChunks( file, factory.getFingerprint(), getOffsets() );
		Collections.sort( chunks, Collections.reverseOrder() );
		return chunks;
	}
	
	public List<Long> getHandFingers() {
		if ( hand != null ) return hand;
		hand = new ArrayList<Long>( FINGERS );
		for ( int i = 0; i < FINGERS && i < getAllFingers().size(); i++ ) {
			hand.add( getAllFingers().get( i ) );
		}
		return hand;
	}

	public int getFingerCount(){
		return getAllFingers().size();
	}
	
	public File getFile() {
		return file;
	}
	
	@Override
	public String toString() {
		return getHandFingers().toString();
	}

	
	
}
