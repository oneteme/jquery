package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class NamedColumn implements TaggableColumn {

	@Delegate
	private final DBColumn column;
	private final String reference;

	@Override
	public String tagname() {
		return reference;
	}
	
	@Override
	public NamedColumn as(String name) { // map
		return new NamedColumn(unwrap(), name);
	}
	
	public DBColumn unwrap() {
		return column;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue()) + " AS " + reference;
	}
}
