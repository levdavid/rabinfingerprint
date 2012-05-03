package org.bdwyer.handprint;

import java.io.File;

import org.bdwyer.datastructures.BidiSortedMap;
import org.bdwyer.datastructures.Interval;

public class HandPrint {
	
	private static final int FINGERS = 10;

	protected final File file;
	protected final FingerFactory factory;

	protected Long palm;
	protected BidiSortedMap<Long, Interval> fingers;
	protected BidiSortedMap<Long, Interval> hand;

	public HandPrint( File file, FingerFactory factory ) {
		this.file = file;
		this.factory = factory;
	}
	
	public void buildAll(){
		getPalm();
		getAllFingers();
		getHandFingers();
	}

	public Long getPalm() {
		if ( palm != null ) return palm;
		palm = factory.getPalm( file );
		return palm;
	}

	public BidiSortedMap< Long, Interval > getAllFingers() {
		if ( fingers != null ) return fingers;
		fingers = factory.getAllFingers( file );
		return fingers;
	}
	
	public BidiSortedMap< Long, Interval > getHandFingers() {
		if ( hand != null ) return hand;
		hand = new BidiSortedMap< Long, Interval >();
		int i = 0;
		for ( java.util.Map.Entry< Long, Interval > entry : getAllFingers().entrySet() ) {
			if ( i >= FINGERS ) break;
			hand.put( entry.getKey(), entry.getValue() );
			i++;
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