package org.bdwyer.fingerprint;

/**
 * A fast, but dangerous circular byte queue. There is no enforcement that the
 * indices are valid, and it is easily possible to overflow when adding or
 * polling.
 * 
 * Nevertheless, this is faster than Queue<Byte> by a factor of 5 or so.
 */
public class CircularByteQueue {

	private int size = 0;
	private int head = 0;
	private int tail = 0;
	private final int capacity;
	private final byte[] bytes;
	
	public CircularByteQueue( int capacity ) {
		this.capacity = capacity;
		this.bytes = new byte[ capacity ];
	}
	
	/**
	 * Adds the byte to the queue
	 */
	public synchronized void add( byte b ) {
		bytes[head] = b;
		head++;
		head %= capacity;
		size++;
	}

	/**
	 * Removes and returns the next byte in the queue
	 */
	public synchronized byte poll() {
		byte b = bytes[tail];
		tail++;
		tail %= capacity;
		size--;
		return b;
	}
	
	/**
	 * Resets the queue to its original state but DOES NOT clear the array of
	 * bytes.
	 */
	public synchronized void clear() {
		head = 0;
		tail = 0;
		size = 0;
	}

	/**
	 * Returns the number of elements that have been added to this queue minus
	 * those that have been removed.
	 */
	public synchronized int size() {
		return size;
	}
	
	/**
	 * Returns the capacity of this queue -- i.e. the total number of bytes that
	 * can be stored without overflowing.
	 */
	public int apacity() {
		return capacity;
	}
}
