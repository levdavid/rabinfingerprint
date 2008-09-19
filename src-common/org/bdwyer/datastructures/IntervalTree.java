package org.bdwyer.datastructures;

import java.util.ArrayList;
import java.util.List;


/**
 * An extension of RedBlackTree that stores generic values using Intervals as
 * keys. The augmented nodes allow us to query in O(log(n)) time for overlaps.
 * 
 * Algorithms furnished by CLRS
 * 
 * Asymptotic Characteristics:
 * Get Overlapping -- O(min(n,k*log(n))) where n is size of the tree and k is
 * the number of overlapping intervals in the tree
 */
public class IntervalTree<Value> extends RedBlackTree< Interval, Value >  {

	/**
	 * An augmented node that holds extra information about its children.
	 * 
	 * This allows us to make interval queries in O(log(n)). The trick is to
	 * update the ancestor tree on insertion and deletion (which is O(log(n)))
	 * and to update the two rotated nodes upon rotation (which is (O(1)))
	 * 
	 * @see CLRS 14.3
	 */
	protected static class AugmentedNode< Value > extends RedBlackNode< Interval, Value > {

		protected Integer maxEnd;
		protected Integer minStart;

		protected AugmentedNode( Interval key, Value value ) {
			super( key, value );
			maxEnd = key.getEnd();
			minStart = key.getStart();
		}

		protected Integer getMaxEnd() {
			return maxEnd;
		}

		protected Integer getMinStart() {
			return minStart;
		}

		/**
		 * maxEnd = max( this.maxEnd, left.maxEnd, right.maxEnd );
		 * minStart = max( this.minStart, left.minStart, right.minStart );
		 * 
		 * @see CLRS 14.3
		 */
		@Override
		public void updateDataFromChildren() {
			super.updateDataFromChildren();
			
			maxEnd = key.getEnd();
			if ( getLeft() != null && getLeft().getMaxEnd() > maxEnd ) {
				maxEnd = getLeft().getMaxEnd();
			}
			if ( getRight() != null && getRight().getMaxEnd() > maxEnd ) {
				maxEnd = getRight().getMaxEnd();
			}

			minStart = key.getStart();
			if ( getLeft() != null && getLeft().getMinStart() < minStart ) {
				minStart = getLeft().getMinStart();
			}
			if ( getRight() != null && getRight().getMinStart() < minStart ) {
				minStart = getRight().getMinStart();
			}
		}

		/** Convenience cast */
		@Override
		public AugmentedNode< Value > getLeft() {
			return (AugmentedNode< Value >) super.getLeft();
		}

		/** Convenience cast */
		@Override
		public AugmentedNode< Value > getRight() {
			return (AugmentedNode< Value >) super.getRight();
		}
		
		/** Convenience cast */
		@Override
		public AugmentedNode< Value > getParent() {
			return (AugmentedNode< Value >) super.getParent();
		}
	}

	/** Custom node factory */
	@Override
	protected AugmentedNode< Value > createNode( Interval key, Value value ) {
		return new AugmentedNode< Value >( key, value );
	}

	/** Convenience cast */
	@Override
	public AugmentedNode< Value > getRoot() {
		return (AugmentedNode< Value >) super.getRoot();
	}
	
	/**
	 * Finds one interval that overlaps the input, if one exists.
	 * 
	 * O(log(n))
	 * 
	 * @see CLRS 14.3 - Interval-Search
	 */
	public Interval getOverlappingInterval( Interval interval ) {
		AugmentedNode< Value > node = getRoot();
		while ( node != null && node.getKey().isOverlap( interval ) == false ) {
			if ( node.getLeft() != null && node.getLeft().getMaxEnd() >= interval.getStart() ) {
				node = node.getLeft();
			} else {
				node = node.getRight();
			}
		}
		return ( node == null ) ? null : node.getKey();
	}
	
	/**
	 * Finds all intervals that overlap the input. However, with the augmented
	 * node, we get major saving in pruning during the query.
	 * 
	 * O(min(n,k*log(n))) where n is size of the tree and k is the number of
	 * overlapping intervals in the tree
	 * 
	 * @see CLRS 14.3-4
	 */
	public List< Interval > getOverlappingIntervals( Interval interval ) {
		KeyVisitor< Value > visitor = new KeyVisitor< Value >();
		getOverlappingIntervalsBelow( interval, getRoot(), visitor );
		return visitor.getList();
	}

	public List< Value > getOverlappingValues( Interval interval ) {
		ValueVisitor< Value > visitor = new ValueVisitor< Value >();
		getOverlappingIntervalsBelow( interval, getRoot(), visitor );
		return visitor.getList();
	}
	
	/**
	 * Adds overlapping intervals to the list while traversing tree.
	 */
	protected void getOverlappingIntervalsBelow( Interval interval, AugmentedNode< Value > node, OverlappingVisitor<Value,?> visitor ) {
		if ( node == null ) return;
		if ( node.getKey().isOverlap( interval ) ) {
			visitor.visitOverlappingNode( node );
		}
		
		// if anything on the left side could overlap, descend
		if ( node.getLeft() != null && node.getLeft().getMaxEnd() >= interval.getStart() ) {
			getOverlappingIntervalsBelow( interval, node.getLeft(), visitor );
		}
		
		// if anything on the right side could overlap, descend
		if ( node.getRight() != null && node.getRight().getMinStart() < interval.getEnd() ) {
			getOverlappingIntervalsBelow( interval, node.getRight(), visitor );
		}
	}
	
	protected abstract static class OverlappingVisitor< Value, ListType > {
		protected final List< ListType > list;

		public OverlappingVisitor() {
			this.list = new ArrayList< ListType >();
		}

		public List< ListType > getList() {
			return list;
		}

		public abstract void visitOverlappingNode( AugmentedNode< Value > node );
	}

	protected static class KeyVisitor< Value > extends OverlappingVisitor< Value, Interval > {
		@Override
		public void visitOverlappingNode( AugmentedNode< Value > node ) {
			list.add( node.getKey() );
		}
	}

	protected static class ValueVisitor< Value > extends OverlappingVisitor< Value, Value > {
		@Override
		public void visitOverlappingNode( AugmentedNode< Value > node ) {
			list.add( node.getValue() );
		}
	}
	
}
