package org.bdwyer.fingerprint;

import java.util.Queue;

import org.bdwyer.polynomial.Polynomial;

public abstract class AbstractFingerprint implements Fingerprint< Polynomial > {

	protected final Polynomial poly;
	protected final CircularByteQueue byteWindow;
	protected final long bytesPerWindow;
	protected long byteCount;

	public AbstractFingerprint( Polynomial poly, long bytesPerWindow ) {
		this.poly = poly;
		this.bytesPerWindow = bytesPerWindow;
		this.byteWindow = new CircularByteQueue( (int) bytesPerWindow + 1 );
		reset();
	}

	public AbstractFingerprint pushBytes( byte[] bytes ) {
		for ( byte b : bytes ) {
			pushByte( b );
		}
		return this;
	}

	public AbstractFingerprint pushBytes( byte[] bytes, int offset, int length ) {
		for ( int i = offset; i < offset + length; i++ ) {
			pushByte( bytes[i] );
		}
		return this;
	}

	public abstract AbstractFingerprint pushByte( byte b );

	public abstract AbstractFingerprint popByte();

	public abstract Polynomial getFingerprint();

	public synchronized AbstractFingerprint reset() {
		this.byteCount = 0;
		this.byteWindow.clear();
		return this;
	}

	public long getBytesPerWindow() {
		return bytesPerWindow;
	}

	public synchronized long getBytesFingerprinted() {
		return byteCount;
	}

	@Override
	public String toString() {
		return getFingerprint().toHexString();
	}

}