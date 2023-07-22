package org.usf.jquery.core;

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
	public String reference() {
		return reference;
	}
	
	@Override
	public NamedColumn as(String name) { // map
		return new NamedColumn(column, name);
	}
}
