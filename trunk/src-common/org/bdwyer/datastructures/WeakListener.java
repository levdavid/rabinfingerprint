package org.bdwyer.datastructures;

import java.lang.ref.WeakReference;

/**
 * Abstract class for a weak reference listener wrapper.
 * 
 * Example concrete implementation:
 * @see WeakPropertyChangeListener
 */
public abstract class WeakListener< EventorType extends  WeakListener.Eventor, ListenerType > {

	public static interface Eventor { /* Marker interface for eventor classes */ }
	
	protected final EventorType eventor;
	protected final WeakReference< ListenerType > weakListener;
	
	protected WeakListener( EventorType eventor, ListenerType listener ) {
		this.eventor = eventor;
		this.weakListener = new WeakReference< ListenerType >( listener );
	}

	protected abstract void addListener();
	protected abstract void removeListener();
	
	public EventorType getEventor() {
		return eventor;
	}

	protected ListenerType getListener() {
		ListenerType listener = weakListener.get();
		if ( listener == null ) removeListener();
		return listener;
	}
	
	/**
	 * Direct equals to weak reference so a normal remove using strong reference
	 * will work. We must access the weakListener directly as opposed to the
	 * modified getter because removal will cause an infinite .equals() loop
	 */
	@Override
	public boolean equals( Object obj ) {
		ListenerType listener = weakListener.get();
		if ( listener != null ) return listener.equals( obj );
		return super.equals( obj );
	}

	@Override
	public int hashCode() {
		ListenerType listener = weakListener.get();
		if ( listener != null ) return listener.hashCode();
		return super.hashCode();
	}
}












