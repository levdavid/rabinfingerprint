package org.rabinfingerprint.handprint;

import java.io.File;
import java.util.Comparator;

import org.rabinfingerprint.datastructures.BidiSortedMap;
import org.rabinfingerprint.datastructures.Interval;
import org.rabinfingerprint.fingerprint.RabinFingerprintLong;
import org.rabinfingerprint.fingerprint.RabinFingerprintLongWindowed;
import org.rabinfingerprint.polynomial.Polynomial;

public class FingerFactory {
	// TODO make these configurable, but default
	public final static long WINDOW_SIZE = 8;
	public final static int BUFFER_SIZE = 65536;
	private final static long CHUNK_BOUNDARY = 0xFFF;
	private final static long CHUNK_PATTERN = 0xABC;

	private final static Comparator<Long> LONG_REVERSE_SORT = new Comparator<Long>() {
		public int compare(Long o1, Long o2) {
			return o2.compareTo(o1);
		}
	};

	private final static Comparator<Interval> INTERVAL_SORT = new Comparator<Interval>() {
		public int compare(Interval o1, Interval o2) {
			return o1.compareTo(o2);
		}
	};

	private final RabinFingerprintLong finger;
	private final RabinFingerprintLongWindowed fingerWindow;

	public FingerFactory(Polynomial p, long bytesPerWindow) {
		this.finger = new RabinFingerprintLong(p);
		this.fingerWindow = new RabinFingerprintLongWindowed(p, bytesPerWindow);
	}

	protected RabinFingerprintLong getFingerprint() {
		// prevent leaking state
		return new RabinFingerprintLong(finger);
	}

	protected RabinFingerprintLongWindowed getWindowedFingerprint() {
		// prevent leaking state
		return new RabinFingerprintLongWindowed(fingerWindow);
	}

	/**
	 * Fingerprint the file into chunks called "Fingers". The chunk boundaries
	 * are determined using a windowed fingerprinter such as
	 * {@link RabinFingerprintLongWindowed}. This guarantees that a long chunk
	 * of data will always contains some fingers that hash to same value. This
	 * is the KEY to the utility of the handprinting scheme for file similarity.
	 * Even if you re-arrange a file's contents or corrupt parts of it, the hand
	 * print will be able to find all the parts that are similar in a very
	 * efficient manner.
	 */
	public BidiSortedMap<Long, Interval> getAllFingers(File file) {
		// init structures
		final BidiSortedMap<Long, Interval> chunks = new BidiSortedMap<Long, Interval>(LONG_REVERSE_SORT, INTERVAL_SORT);
		final StreamWrapper stream = new StreamWrapper(file);
		final byte[] buffer = new byte[BUFFER_SIZE];

		// windowing fingerprinter for finding chunk boundaries. this is only
		// reset at the beginning of the file
		final RabinFingerprintLong window = getWindowedFingerprint();

		// fingerprinter for chunks. this is reset after each chunk
		final RabinFingerprintLong finger = getFingerprint();

		// init counters
		long chunkStart = 0;
		long chunkEnd = 0;
		int bytesRead;

		/*
		 * buffer-read through file one byte at a time. we have to use this
		 * granularity to ensure that, for example, a one byte offset at the
		 * beginning of the file won't effect the chunk boundaries
		 */
		while ((bytesRead = stream.getBytes(buffer)) >= 0) {
			for (int i = 0; i < bytesRead; i++) {
				// push byte into fingerprints
				window.pushByte(buffer[i]);
				finger.pushByte(buffer[i]);
				chunkEnd++;

				/*
				 * if we've reached a boundary (which we will at some
				 * probability based on the boundary pattern and the size of the
				 * fingerprint window), we store the current chunk fingerprint
				 * and reset the chunk fingerprinter.
				 */
				if ((window.getFingerprintLong() & CHUNK_BOUNDARY) == CHUNK_PATTERN) {
					Interval interval = new Interval(new Long(chunkStart), new Long(chunkEnd));
					chunks.put(finger.getFingerprintLong(), interval);
					finger.reset();
					// store last chunk offset
					chunkStart = chunkEnd;
				}
			}
		}

		// close stream
		stream.close();

		// final chunk
		Interval interval = new Interval(new Long(chunkStart), new Long(chunkEnd));
		chunks.put(finger.getFingerprintLong(), interval);
		finger.reset();

		return chunks;
	}

	/**
	 * Rapidly fingerprint an entire file's contents.
	 * 
	 * We use the term "Palm" to describe the fingerprint of the entire file,
	 * instead of chunks, which are referred to as the file's "Fingers".
	 */
	public long getPalm(File file) {
		final StreamWrapper stream = new StreamWrapper(file);
		final byte[] buffer = new byte[BUFFER_SIZE];
		final RabinFingerprintLong finger = getFingerprint();
		int bytesRead;
		while ((bytesRead = stream.getBytes(buffer)) >= 0) {
			finger.pushBytes(buffer, 0, bytesRead);
		}
		stream.close();
		return finger.getFingerprintLong();
	}
}