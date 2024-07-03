package org.usf.jquery.core;

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
		return column.toString();
	}
}
