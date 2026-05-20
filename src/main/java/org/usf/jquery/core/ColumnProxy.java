package org.usf.jquery.core;

import static org.usf.jquery.core.Utils.toList;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ColumnProxy implements Column {

	private final Column column;
	private final JDBCType type; //optional
	private final String tag; //optional
	
	@Override
	public int prepare(QueryAnalyzer manifest) {
		return manifest.analyzeNested(toList(column), this);
	}

	@Override
	public void build(SqlBuilder builder) {
		builder.append(column);
	}
	
	@Override
	public JDBCType getType() {
		return type;
	}

	@Override
	public String getTag() {
		return tag;
	}
	
	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
}
