package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.QueryVariables.addWithValue;

import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ColumnProxy implements NamedColumn {

	private final DBColumn column;
	private final JDBCType type; //optional
	private final String tag; //optional
	
	@Override
	public JDBCType getType() {
		return nonNull(type) ? type : column.getType();
	}

	@Override
	public String sql(QueryVariables builder) {
		return column.sql(builder);
	}
	
	@Override
	public ColumnProxy as(String name, JDBCType type) { // map
		return Objects.equals(this.tag, name) && Objects.equals(this.type, type) 
				? this 
				: new ColumnProxy(column, type, name);
	}
	
	@Override
	public String toString() {
		return this.sqlWithTag(addWithValue());
	}
}
