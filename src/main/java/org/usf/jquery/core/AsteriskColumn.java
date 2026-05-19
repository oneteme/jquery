package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlStringBuilder.DOT;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
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
	private final Collection<DBView> views;
	
	@Override
	public int prepare(QueryManifest manifest) {
		if(nonNull(views)) {
			for(var v : views) {
				manifest.from(v); //declare views
			}
		}
		return SCALAR;
	}
	
	@Override
	public void build(QueryBuilder builder) {
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
		return DBObject.toSQL(this);
	}
}
