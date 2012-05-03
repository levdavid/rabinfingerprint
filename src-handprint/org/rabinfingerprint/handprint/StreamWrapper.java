package org.rabinfingerprint.handprint;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.rabinfingerprint.handprint.Handprints.HandprintException;

public class StreamWrapper {

	private final BufferedInputStream stream;

	public StreamWrapper(File file) {
		this.stream = createStream(file);
	}

	private BufferedInputStream createStream(File file) {
		BufferedInputStream stream;
		try {
			stream = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new HandprintException("File not found", e);
		}
		return stream;
	}

	public int getBytes(byte[] buffer) {
		int bytesRead;
		try {
			bytesRead = stream.read(buffer);
		} catch (IOException e) {
			throw new HandprintException("IO Exception while reading file", e);
		}
		return bytesRead;
	}

	public void close() {
		try {
			stream.close();
		} catch (IOException e) {
			throw new HandprintException("IO Exception while closing file", e);
		}
	}

}