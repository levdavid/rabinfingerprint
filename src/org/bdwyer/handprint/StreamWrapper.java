package org.bdwyer.handprint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bdwyer.handprint.HandprintUtils.HandprintException;

public abstract class StreamWrapper {

	public static FileInputStream getStream( File file ){
		FileInputStream stream;
		try {
			stream = new FileInputStream( file );
		} catch ( FileNotFoundException e ) {
			throw new HandprintException( "File not found", e );
		}
		return stream;
	}
	
	public static int getBytes( final FileInputStream stream, byte[] buffer ) {
		int bytesRead;
		try {
			bytesRead = stream.read( buffer );
		} catch ( IOException e ) {
			throw new HandprintException( "IO Exception while reading file", e );
		}
		return bytesRead;
	}
	
	public static void closeStream( final FileInputStream stream ) {
		try {
			stream.close();
		} catch ( IOException e ) {
			throw new HandprintException( "IO Exception while closing file", e );
		}
	}

}