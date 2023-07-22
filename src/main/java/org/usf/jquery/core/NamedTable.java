package org.usf.jquery.core;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@EqualsAndHashCode(of = "reference")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class NamedTable implements TaggableTable {

	@Delegate
	private final DBTable table;
	private final String reference;

	@Override
	public String reference() {
		return reference;
	}
	
	@Override
	public NamedTable as(String name) { // map
		return new NamedTable(table, name);
	}
}
