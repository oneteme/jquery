package org.usf.jquery.core;

import static org.usf.jquery.core.SqlBuilder.DOT;
import static org.usf.jquery.core.SqlBuilder.SCOMA;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Collection;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class AsteriskColumn implements Column {
	
	private static final String SYMBOL = "*";
	private final Collection<View> views; //optional, for example: select t1.* from table1 t1
	
	@Override
	public int prepare(QueryAnalyzer analyzer) {
		if(!isEmpty(views)) {
			for(var v : views) {
				analyzer.from(v); //declare views
			}
		}
		return SCALAR;
	}
	
	@Override
	public void build(SqlBuilder builder) {
		if(!isEmpty(views) && builder.getStore().dialect().supportWilcardPrefix()) {
			builder.appendEach(SCOMA, views, v-> builder.appendViewAlias(v, DOT).append(SYMBOL));
		}
		else {
			builder.append(SYMBOL);
		}
	}
	
	@Override
	public JDBCType getType() {
		return null;
	}
	
	@Override //!important: it does not takes tag
	public String getTag() {
		return null; 
	}
	
	@Override
	public Column as(String alias, JDBCType type) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
}
