package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.QueryContext.addWithValue;

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
public final class ColumnProxy implements NamedColumn {

	@Delegate
	private final DBColumn column;
	private final JDBCType type; //optional
	private final String tag; //optional
	
	@Override
	public String getTag() {
		return tag;
	}
	
	@Override
	public JDBCType getType() {
		return nonNull(type) ? type : column.getType();
	}
	
	@Override // do not delegate this
	public ColumnProxy as(String name) { 
		return NamedColumn.super.as(name);
	}
	
	@Override
	public ColumnProxy as(String name, JDBCType type) { // map
		return Objects.equals(this.tag, name) && Objects.equals(this.type, type) 
				? this 
				: new ColumnProxy(column, type, name);
	}
	
	@Override
	public String toString() {
		return this.sql(addWithValue());
	}
}
