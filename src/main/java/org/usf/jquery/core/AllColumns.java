package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.DatabaseVendor.TERADATA;
import static org.usf.jquery.core.SqlStringBuilder.DOT;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class AllColumns implements NamedColumn {
	
	private static final String ASTR = "*";
	private final DBView[] views;
	
	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> cons) {
		if(nonNull(views)) {
			query.declare(views); //declare views
		}
		return -1;
	}
	
	@Override
	public void build(QueryBuilder query) {
		if(isEmpty(views) || query.getEnvironment().getProduct() != TERADATA) {
			query.append(ASTR);
		}
		else {
			query.appendEach(SCOMA, views, v-> query.appendViewAlias(v, DOT).append(ASTR));
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
	public String toString() {
		return DBObject.toSQL(this);
	}
}
