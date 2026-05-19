package org.usf.jquery.core;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ColumnProxy implements Column {

	//do not @Delegate
	private final Column column;
	private final JDBCType type; //optional
	private final String tag; //optional
	
	@Override
	public int prepare(QueryManifest manifest) {
		return manifest.prepareNestedOrElse(asList(column), this);
	}

	@Override
	public void build(QueryBuilder builder) {
		builder.append(column);
	}
	
	@Override
	public JDBCType getType() {
		return nonNull(type) ? type : column.getType();
	}

	@Override
	public String getTag() {
		return tag;
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
