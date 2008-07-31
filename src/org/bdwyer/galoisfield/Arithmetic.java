package org.bdwyer.galoisfield;

public interface Arithmetic<T>{
	public T add( T o );
	public T subtract( T o );
	public T multiply( T o );
	public T and( T o );
	public T or( T o );
	public T xor( T o );
	public T mod( T o );
	public T gcd( T o );
}