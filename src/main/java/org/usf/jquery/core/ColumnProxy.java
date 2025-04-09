package org.usf.jquery.core;

import static java.util.Objects.nonNull;

import java.util.Objects;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ColumnProxy implements NamedColumn {

	//do not @Delegate
	private final DBColumn column;
	private final JDBCType type; //optional
	private final String tag; //optional
	
	@Override
	public String getTag() {
		return tag;
	}

	@Override
	public void build(QueryBuilder query) {
		query.append(column);
	}
	
	@Override
	public JDBCType getType() {
		return nonNull(type) ? type : column.getType();
	}

	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> cons) {
		return column.compose(query, cons);
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
		return DBObject.toSQL(this);
	}
}
