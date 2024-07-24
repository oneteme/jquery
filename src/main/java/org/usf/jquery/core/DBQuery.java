package org.usf.jquery.core;

import java.util.Collection;

import org.usf.jquery.core.JavaType.Typed;

/**
 * 
 * @author u$f
 *
 */
public interface DBQuery extends DBView, Typed {

	@Deprecated
	Collection<TaggableColumn> columns();

	@Override
	default JavaType getType() {
		var cols = columns();
		if(cols.size() == 1) {
			return cols.iterator().next().getType();
		}
		throw new UnsupportedOperationException((cols.isEmpty() ? "no columns" : "too many columns") + " : " + this);
	}
}
