package org.bdwyer.handprint;


public class Chunk implements Comparable< Chunk > {
	private int start;
	private int end;
	private long fingerprint;
	private String string;

	public Chunk( int start, int end, long fingerprint ) {
		this.start = start;
		this.end = end;
		this.fingerprint = fingerprint;
		this.string = Long.toHexString( fingerprint ).toUpperCase();
	}

	public int compareTo( Chunk o ) {
		return this.string.compareTo( o.string );
	}
	
	@Override
	public String toString() {
		return string;
	}
}