package org.bdwyer.datastructures;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An implementation of {@link Iterable} for {@link Interval}s. This is backed
 * by an {@link IntervalTree}.
 */
public class IntervalSet implements Iterable< Interval > {

	// Dummy value to associate with an Object in the backing Map
	private static final Object PRESENT = new Object();

	// Delegate tree
	private final IntervalTree< Object > tree;

	public IntervalSet() {
		tree = new IntervalTree< Object >();
	}

	/**
	 * Inserts the Interval into the IntervalSet
	 * @return true if the Interval was already present
	 */
	public boolean insert( Interval interval ) {
		Object val = tree.insert( interval, PRESENT );
		return ( val == PRESENT );
	}

	/**
	 * Inserts each Interval into the IntervalSet
	 * @return true if any of the Intervals were already present
	 */
	public boolean insertAll( Iterable< Interval > intervals ) {
		boolean b = false;
		for ( Interval interval : intervals ) {
			b |= insert( interval );
		}
		return b;
	}

	/**
	 * Deletes the Interval from the IntervalSet
	 * @return true if the Interval was present prior to deletion
	 */
	public boolean delete( Interval interval ) {
		Object val = tree.delete( interval );
		return ( val == PRESENT );
	}

	/**
	 * Deletes each Interval from the IntervalSet
	 * @return true if any of the Interval were present prior to deletion
	 */
	public boolean deleteAll( Iterable< Interval > intervals ) {
		boolean b = false;
		for ( Interval interval : intervals ) {
			b |= delete( interval );
		}
		return b;
	}
	
	public Integer getIndex( Interval interval ) {
		return tree.getIndex( interval );
	}

	public Interval getIntervalAt( Integer index ) {
		return tree.getKeyAt( index );
	}
	
	public boolean contains( Interval interval ) {
		return tree.containsKey( interval );
	}

	public void clear() {
		tree.clear();
	}
	
	public boolean isEmpty() {
		return tree.isEmpty();
	}

	public int getSize() {
		return tree.getSize();
	}

	public Interval getOverlappingInterval( Interval interval ) {
		return tree.getOverlappingInterval( interval );
	}

	public List< Interval > getOverlappingIntervals( Interval interval ) {
		return tree.getOverlappingIntervals( interval );
	}
	
	public Set< Interval > keySet() {
		return tree.keySet();
	}

	public Iterator< Interval > iterator() {
		return tree.new KeyIterator();
	}
}
