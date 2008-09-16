package org.bdwyer.handprint;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bdwyer.fingerprint.RabinFingerprintLong;
import org.bdwyer.fingerprint.RabinFingerprintLongWindowed;
import org.bdwyer.polynomial.Polynomial;

public class HandPrint {

	private static final int FINGERS = 10;

	protected final File file;
	protected final RabinFingerprintLong finger;
	protected final RabinFingerprintLongWindowed fingerWindow;

	protected Long thumbprint;
	protected List<Long> offsets;
	protected List<Long> chunks;
	protected List<Long> hand;

	public HandPrint( File file, RabinFingerprintLong finger, RabinFingerprintLongWindowed fingerWindow ) {
		this.file = file;
		this.finger = finger;
		this.fingerWindow = fingerWindow;
	}
	
	public void buildAll(){
		getThumb();
		getOffsets();
		getAllFingers();
		getHandFingers();
	}

	public Long getThumb() {
		if ( thumbprint != null ) return thumbprint;
		thumbprint = HandprintUtils.getThumbprint( file, finger );
		return thumbprint;
	}

	public List<Long> getOffsets() {
		if ( offsets != null ) return offsets;
		offsets = HandprintUtils.getOffsets( file, fingerWindow );
		return offsets;
	}

	public List<Long> getAllFingers() {
		if ( chunks != null ) return chunks;
		chunks = HandprintUtils.getChunks( file, finger, getOffsets() );
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
