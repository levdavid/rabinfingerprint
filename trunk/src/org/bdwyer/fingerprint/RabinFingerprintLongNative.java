package org.bdwyer.fingerprint;

import org.bdwyer.polynomial.Polynomial;

public class RabinFingerprintLongNative extends RabinFingerprintLong {

	public RabinFingerprintLongNative( Polynomial poly ) {
		this( poly, 0 );
	}

	public RabinFingerprintLongNative( Polynomial poly, long bytesPerWindow ) {
		super( poly, bytesPerWindow );
	}

	// can we store things statically?
	private native void copyTablesNative( long[] pushTable, long[] popTable );

	private native void pushByteNative( byte b, long[] pushTable, long[] popTable );

	private native void popByteNative( byte b, long[] pushTable, long[] popTable);

	@Override
	public synchronized void pushByte( byte b ) {
		pushByteNative( b, pushTable, popTable );
	}

	@Override
	public synchronized void popByte() {
		popByteNative( (byte)0, pushTable, popTable );
	}

}
