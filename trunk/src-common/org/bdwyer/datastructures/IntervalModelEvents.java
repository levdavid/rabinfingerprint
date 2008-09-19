package org.bdwyer.datastructures;

import java.util.EventListener;

import org.bdwyer.datastructures.WeakListener.Eventor;

public class IntervalModelEvents {

	/**
	 * Marker interface for IntervalModelListener eventors
	 */
	public static interface IntervalModelEventor< T > extends Eventor {
		public void addIntervalModelListener( IntervalModelListener< T > l );
		public void removeIntervalModelListener( IntervalModelListener< T > l );
	}
	
	public static interface IntervalModelListener< T > extends EventListener {
		public void intervalAdded( IntervalModel< T > model, T value, Interval interval );
		public void intervalRemoved( IntervalModel< T > model, T value, Interval interval );
		public void intervalChanged( IntervalModel< T > model, T value, Interval oldInterval, Interval newInterval );
	}

	public static class WeakIntervalModelListener< T > extends WeakListener< IntervalModelEventor< T >, IntervalModelListener< T > >
		implements IntervalModelListener< T > {

		/**
		 * Users of this factory method should store a hard reference to the
		 * listener that is passed in and out of this method. This will ensure
		 * that the life-cycle of the listener is the same as its one hard
		 * reference.
		 */
		public static < T > IntervalModelListener< T > create( IntervalModelEventor< T > eventor, IntervalModelListener< T > listener ) {
			WeakIntervalModelListener< T > weakListener = new WeakIntervalModelListener< T >( eventor, listener );
			weakListener.addListener();
			return listener;
		}

		protected WeakIntervalModelListener( IntervalModelEventor< T > eventor, IntervalModelListener< T > listener ) {
			super( eventor, listener );
		}

		@Override
		protected void addListener() {
			getEventor().addIntervalModelListener( this );
		}

		@Override
		protected void removeListener() {
			getEventor().removeIntervalModelListener( this );
		}

		public void intervalAdded( IntervalModel< T > model, T value, Interval interval ) {
			IntervalModelListener< T > listener = getListener();
			if ( listener != null ) listener.intervalAdded( model, value, interval );
		}

		public void intervalRemoved( IntervalModel< T > model, T value, Interval interval ) {
			IntervalModelListener< T > listener = getListener();
			if ( listener != null ) listener.intervalRemoved( model, value, interval );
		}

		public void intervalChanged( IntervalModel< T > model, T value, Interval oldInterval, Interval newInterval ) {
			IntervalModelListener< T > listener = getListener();
			if ( listener != null ) listener.intervalChanged( model, value, oldInterval, newInterval );
		}

	}
}
