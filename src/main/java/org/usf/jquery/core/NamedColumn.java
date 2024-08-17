package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import java.util.Objects;

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
	private final String tag; //nullable 
	//+ type

	@Override
	public String tagname() {
		return tag;
	}
	
	@Override
	public NamedColumn as(String name) { // map
		return Objects.equals(name, tag) ? this : new NamedColumn(column, name);
	}
	
	@Override
	public String toString() {
		return this.sqlWithTag(addWithValue());
	}
}
