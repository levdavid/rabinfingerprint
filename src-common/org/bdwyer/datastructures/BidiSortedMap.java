package org.bdwyer.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A bi-directionally indexed map for quick key-value and value-key lookup.
 * Also, both values and keys can be sorted using the comparators provided to
 * the constructor.
 * 
 * @param <K>
 * @param <V>
 */
public class BidiSortedMap< K, V > implements Map< K, V > {

	private final TreeMap< K, V > keyToValue;
	private final TreeMap< V, K > valueToKey;
	
	public BidiSortedMap() {
		this( null, null );
	}

	public BidiSortedMap( Comparator< K > keySort, Comparator< V > valueSort ) {
		keyToValue = new TreeMap< K, V >( keySort );
		valueToKey = new TreeMap< V, K >( valueSort );
	}
	
	public void clear() {
		keyToValue.clear();
		valueToKey.clear();
	}

	public boolean containsKey( Object key ) {
		return keyToValue.containsKey( key );
	}

	public boolean containsValue( Object value ) {
		return valueToKey.containsKey( value );
	}

	public Set< java.util.Map.Entry< K, V >> entrySet() {
		return keyToValue.entrySet();
	}

	public V get( Object key ) {
		return keyToValue.get( key );
	}
	
	public V getValueOf( K key ) {
		return keyToValue.get( key );
	}

	public K getKeyOf( V key ) {
		return valueToKey.get( key );
	}

	public boolean isEmpty() {
		return keyToValue.isEmpty();
	}

	public Set< K > keySet() {
		return keyToValue.keySet();
	}
	
	public List< K > keyList() {
		return new ArrayList<K>( keyToValue.keySet() );
	}

	public Set< V > valueSet() {
		return valueToKey.keySet();
	}

	public List< V > valueList() {
		return new ArrayList<V>( valueToKey.keySet() );
	}

	public V put( K key, V value ) {
		V oldValue = keyToValue.put( key, value );
		K oldKey = valueToKey.put( value, key );
		return oldValue; 
	}

	public void putAll( Map< ? extends K, ? extends V > t ) {
		for ( java.util.Map.Entry< ? extends K, ? extends V > entry : t.entrySet() ) {
			put( entry.getKey(), entry.getValue() );
		}
	}

	public V remove( Object key ) {
		V oldValue = keyToValue.remove( key );
		K oldKey = valueToKey.remove( oldValue );
		return oldValue;
	}

	public int size() {
		return keyToValue.size();
	}

	public Collection< V > values() {
		return valueSet();
	}
	
	public void sortValues( Comparator< K > comp ){
		
	}
}
