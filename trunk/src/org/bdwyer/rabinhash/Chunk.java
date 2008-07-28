package org.bdwyer.rabinhash;

import java.util.Comparator;

public class Chunk implements Comparable< Chunk > {
	private int start;
	private int end;
	private byte[] digest;
	private String digestString;

	
	private static final String[] HEX = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
	static String toHex( byte b ) {
		return HEX[0xF & ( b >> 8 )] + HEX[0xF & b];
	}

	Chunk( int start, int end, byte[] digest ) {
		this.start = start;
		this.end = end;
		this.digest = digest;
		StringBuffer str = new StringBuffer();
		for ( byte b : digest ) {
			str.append( toHex( b ) );
		}
		this.digestString = str.toString();
	}

	public int compareTo( Chunk o ) {
		DigestComparator comparator = new DigestComparator();
		return comparator.compare( this.digest, o.digest );
	}
	
	@Override
	public String toString() {
		return "0x" + digestString;
	}
	
	public static class DigestComparator implements Comparator< byte[] > {

		public int compare( byte[] o1, byte[] o2 ) {
			for ( int i = 0; i < o1.length && i < o2.length; i++ ) {
				Byte b1 = o1[i];
				Byte b2 = o2[i];
				int cmp = b1.compareTo( b2 );
				if ( cmp != 0 ) return cmp;
			}
			return o1.length - o2.length;
		}
	}

	public static void main( String[] args ) {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1,
				2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
				0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7,
				8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5,
				6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3,
				4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1,
				2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
		long f0 = RabinFingerprinter.hash( bytes, 0, 7, 0 );
		long f1 = RabinFingerprinter.hash( bytes, 0, 32, 0 );
		long f2 = RabinFingerprinter.hash( bytes, 1, 32, 0 );
		System.out.println( "f0: 0x" + Long.toHexString( f0 ).toUpperCase() );
		System.out.println( "f1: 0x" + Long.toHexString( f1 ).toUpperCase() );
		System.out.println( "f2: 0x" + Long.toHexString( f2 ).toUpperCase() );
	}

}