package org.bdwyer.fingerprint;

import org.bdwyer.polynomial.Polynomial;

public abstract class AbstractFingerprint implements Fingerprint< Polynomial > {

	protected final Polynomial poly;
	protected final CircularByteQueue byteWindow;
	protected final long bytesPerWindow;

	public AbstractFingerprint( Polynomial poly, long bytesPerWindow ) {
		this.poly = poly;
		this.bytesPerWindow = bytesPerWindow;
		this.byteWindow = new CircularByteQueue( (int) bytesPerWindow + 1 );
		reset();
	}

	public void pushBytes( final byte[] bytes ) {
		for ( byte b : bytes ) {
			pushByte( b );
		}
	}

	public void pushBytes( final byte[] bytes, final int offset, final int length ) {
		final int max = offset + length;
		int i = offset;
		while ( i < max ) {
			pushByte( bytes[i++] );
		}
	}

	public abstract void pushByte( byte b );

	public abstract void popByte();

	public abstract Polynomial getFingerprint();

	public synchronized void reset() {
		this.byteWindow.clear();
	}

	public long getBytesPerWindow() {
		return bytesPerWindow;
	}

	@Override
	public String toString() {
		return getFingerprint().toHexString();
	}

}