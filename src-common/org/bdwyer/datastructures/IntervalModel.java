package org.bdwyer.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.bdwyer.datastructures.BinarySearchTree.Node;
import org.bdwyer.datastructures.IntervalModelEvents.IntervalModelEventor;
import org.bdwyer.datastructures.IntervalModelEvents.IntervalModelListener;

/**
 * A Model for {@link Interval} based items. We use multi-map style semantics to
 * store multiple objects at a single interval.
 */
public class IntervalModel< T > implements IntervalModelEventor< T > {

	/** Delegate {@link IntervalTree} */
	private final IntervalTree< Collection< T > > tree;

	/**
	 * Updated upon insertion or deletion. If we use anything other than lists
	 * as the internal collection, we may have to be extra careful that
	 * insertions don't collide.
	 */
	private int size;

	public IntervalModel() {
		tree = new IntervalTree< Collection< T > >();
		size = 0;
	}

	/**
	 * Returns a new list of all the values at a given interval
	 */
	public List< T > get( Interval interval ) {
		ArrayList< T > list = new ArrayList< T >();
		Collection< T > values = tree.get( interval );
		if ( values != null ) list.addAll( values );
		return list;
	}

	/**
	 * Adds the value to the interval. If it collides, it will be appended to
	 * the list of values at that interval.
	 */
	public void put( Interval interval, T value ) {
		putImpl( interval, value );
		fireAdded( interval, value );
	}

	/**
	 * Inserts every values into the list of values at the specified interval
	 */
	public void putAll( Interval interval, Collection< T > values ) {
		for ( T value : values ) {
			put( interval, value );
		}
	}

	/**
	 * Removes the specific value from the possible values at the interval. If
	 * the value doesn't exist at that interval, we do nothing.
	 */
	public void remove( Interval interval, T value ) {
		boolean existed = removeImpl( interval, value );
		if ( existed ) fireRemoved( interval, value );
	}

	/**
	 * Removes the specific collection of values from the possible values at the
	 * interval. If the value doesn't exist at that interval, we do nothing.
	 */
	public void removeAll( Interval interval, Collection< T > values ) {
		for ( T value : values ) {
			remove( interval, value );
		}
	}

	/**
	 * Removes all values at the specified interval.
	 */
	public void remove( Interval interval ) {
		Collection< T > coll = tree.get( interval );
		if ( coll == null ) return;
		
		Iterator< T > it = coll.iterator();
		while ( it.hasNext() ) {
			T value = it.next();
			it.remove();
			size--;
			fireRemoved( interval, value );
		}
		
		tree.delete( interval );
	}

	/**
	 * Moves the value from one interval to another while firing only on event.
	 * If the interval did not exist at the old interval, we fire a added event.
	 * The change event is otherwise fired before changing the model.
	 */
	public void changeKey( Interval oldInterval, Interval newInterval, T value ) {
		boolean exists = contains( oldInterval, value );
		if ( exists ) {
			fireChanged( oldInterval, newInterval, value );
			removeImpl( oldInterval, value );
			putImpl( newInterval, value );
		} else {
			putImpl( newInterval, value );
			fireAdded( newInterval, value );
		}
	}
	
	/**
	 * Replaces the entire model with the contents of the input. Events fired
	 * for all removals and insertions.
	 */
	public void copyFrom( IntervalModel< T > otherModel ) {
		this.clearAll();
		for ( Interval key : otherModel.getKeyIterable() ) {
			this.putAll( key, otherModel.get( key ) );
		}
	}

	/**
	 * Synchronizes this model the otherModel, which fires events only for those
	 * elements that are added and removed. For example, this reduces the number
	 * of events we fire when we reload a model with a single change.
	 */
	public void rectifyTo( IntervalModel< T > otherModel ) {
		// remove all the elements not contained in othermodel
		final IntervalModel< T > toRemove = this.subtract( otherModel );
		
		// add all the elements contained othermodel but not this model
		final IntervalModel< T > toAdd = otherModel.subtract( this );

		removeAll( toRemove );
		putAll( toAdd );
	}

	private void putAll( IntervalModel< T > otherModel ) {

		final Iterator< Node< Interval, Collection< T > >> itNode = otherModel.tree.iterator();
		while ( itNode.hasNext() ) {
			final Node< Interval, Collection< T > > node = itNode.next();
			final Interval key = node.getKey();
			final Collection< T > vals = node.getValue();
			putAll( key, vals );
		}
	}
	
	private void removeAll( IntervalModel< T > otherModel ) {
		final Iterator< Node< Interval, Collection< T > >> itNode = otherModel.tree.iterator();
		while ( itNode.hasNext() ) {
			final Node< Interval, Collection< T > > node = itNode.next();
			final Interval key = node.getKey();
			final Collection< T > vals = node.getValue();
			removeAll( key, vals );
		}
	}
	
	private IntervalModel< T > subtract( IntervalModel< T > otherModel ) {
		final IntervalModel< T > diff = new IntervalModel< T >();
		
		final Iterator< Node< Interval, Collection< T > >> itNode = this.tree.iterator();
		while ( itNode.hasNext() ) {
			final Node< Interval, Collection< T > > node = itNode.next();
			final Interval key = node.getKey();
			final Collection< T > vals = node.getValue();
			
			if ( otherModel.contains( key ) == false ) {
				diff.putAll( key, vals );
			} else {
				final Collection< T > otherVals = otherModel.get( key );
				for ( T val : vals ) {
					if ( otherVals.contains( val ) == false ) {
						diff.put( key, val );
					}
				}
			}
		}
		return diff;
	}
	
	/**
	 * Tests if there exist ANY values at the given interval
	 */
	public boolean contains( Interval interval ) {
		return tree.containsKey( interval );
	}

	/**
	 * Tests of the model contains the specified value at the specified interval
	 */
	public boolean contains( Interval interval, T value ) {
		Collection< T > coll = tree.get( interval );
		if ( coll == null ) return false;
		return coll.contains( value );
	}

	private void putImpl( Interval interval, T value ) {
		Collection< T > coll;
		if ( tree.containsKey( interval ) ) {
			coll = tree.get( interval );
		} else {
			coll = new ArrayList< T >();
			tree.insert( interval, coll );
		}
		size += 1;
		coll.add( value );
	}

	private boolean removeImpl( Interval interval, T value ) {
		Collection< T > coll = tree.get( interval );
		if ( coll == null ) return false;
		int s0 = coll.size();
		coll.remove( value );
		int s1 = coll.size();
		size -= ( s0 - s1 );
		if ( coll.isEmpty() ) tree.delete( interval );
		return true;
	}

	/**
	 * Returns the index of the given interval. Note that indices apply only to
	 * keys and not to values.
	 */
	public Integer getIndex( Interval interval ) {
		return tree.getIndex( interval );
	}

	/**
	 * Returns the interval at the given index. Note that indices apply only to
	 * keys and not to values.
	 */
	public Interval getIntervalAt( Integer index ) {
		return tree.getKeyAt( index );
	}

	/**
	 * Removes all elements from the model in constant time and fires no events.
	 */
	public void clear() {
		tree.clear();
		size = 0;
	}

	/**
	 * Removes all elements from the model in O(n*log(n)) time but fires events
	 * for each removal.
	 */
	public void clearAll() {
		final Iterator< Node< Interval, Collection< T > >> it0 = tree.iterator();
		while ( it0.hasNext() ) {
			final Node< Interval, Collection< T > > node = it0.next();
			final Iterator< T > it1 = node.getValue().iterator();
			while ( it1.hasNext() ) {
				final T value = it1.next();
				it1.remove();
				// BD if we don't update the size, it will be inaccurate until
				// the clear call at the end
				fireRemoved( node.getKey(), value );
			}
			it0.remove();
		}
		clear();
	}

	/**
	 * Returns true if there are no values in the model
	 */
	public boolean isEmpty() {
		return tree.isEmpty();
	}

	/**
	 * Returns the number of unique interval keys in the model
	 */
	public int getShallowSize() {
		return tree.getSize();
	}

	/**
	 * Returns the number of values in the model
	 */
	public int getDeepSize() {
		return size;
	}

	/**
	 * Returns one interval that overlaps this interval in O(log(n)) time.
	 */
	public Interval getOverlappingInterval( Interval interval ) {
		return tree.getOverlappingInterval( interval );
	}

	/**
	 * Returns this list of all intervals in this model that overlap the input
	 * interval.
	 */
	public List< Interval > getOverlappingIntervals( Interval interval ) {
		return tree.getOverlappingIntervals( interval );
	}

	/**
	 * Returns the list of all values in this model whose key overlaps the input
	 * interval.
	 */
	public List< T > getOverlappingValues( Interval interval ) {
		List< T > list = new ArrayList< T >();
		for ( Collection< T > overlaps : tree.getOverlappingValues( interval ) ) {
			list.addAll( overlaps );
		}
		return list;
	}

	protected class ValueIterator implements Iterator< T > {
		protected final Iterator< Collection< T >> it0;
		protected Iterator< T > it1;

		protected ValueIterator() {
			it0 = tree.getValues().iterator();
		}

		public boolean hasNext() {
			if ( it1 == null || it1.hasNext() == false ) {
				if ( it0.hasNext() == false ) return false;
				it1 = it0.next().iterator();
			}
			return it1.hasNext();
		}

		public T next() {
			if ( it1 != null ) return it1.next();
			it1 = it0.next().iterator();
			return it1.next();
		}

		public void remove() {
			it1.remove();
		}
	}

	/**
	 * Returns an {@link Iterable} that iterates over all the values in the
	 * model.
	 */
	public Iterable< T > getValueIterable() {
		return new Iterable< T >() {
			public Iterator< T > iterator() {
				return new ValueIterator();
			}
		};
	}

	/**
	 * Returns an {@link Iterable} that iterates over each collection of values
	 * in the model.
	 */
	public Iterable< Collection< T > > getValueCollectionIterable() {
		return tree.getValues();
	}

	/**
	 * Returns an {@link Iterable} that iterates over all the {@link Interval}
	 * keys in the model.
	 */
	public Iterable< Interval > getKeyIterable() {
		return tree.getKeys();
	}

	private EventListenerList ell;

	private EventListenerList getEventListenerList() {
		if ( ell == null ) ell = new EventListenerList();
		return ell;
	}

	public void addIntervalModelListener( IntervalModelListener< T > l ) {
		getEventListenerList().add( IntervalModelListener.class, l );

	}

	public void removeIntervalModelListener( IntervalModelListener< T > l ) {
		getEventListenerList().remove( IntervalModelListener.class, l );
	}

	protected void fireAdded( final Interval interval, final T value ) {
//		SwingUtilities.invokeLater( new Runnable() {
//			public void run() {
				for ( IntervalModelListener< T > l : getEventListenerList().getListeners( IntervalModelListener.class ) ) {
					l.intervalAdded( IntervalModel.this, value, interval );
				}
//			}
//		} );
	}

	protected void fireRemoved( final Interval interval, final T value ) {
//		SwingUtilities.invokeLater( new Runnable() {
//			public void run() {
				for ( IntervalModelListener< T > l : getEventListenerList().getListeners( IntervalModelListener.class ) ) {
					l.intervalRemoved( IntervalModel.this, value, interval );
				}
//			}
//		} );
	}

	protected void fireChanged( final Interval oldInterval, final Interval newInterval, final T value ) {
//		SwingUtilities.invokeLater( new Runnable() {
//			public void run() {
				for ( IntervalModelListener< T > l : getEventListenerList().getListeners( IntervalModelListener.class ) ) {
					l.intervalChanged( IntervalModel.this, value, oldInterval, newInterval );
				}
//			}
//		} );
	}
}
