package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Database.TERADATA;
import static org.usf.jquery.core.Database.currentDatabase;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class AllColumns implements NamedColumn {
	
	private final DBView[] views;
	
	@Override
	public void build(QueryBuilder query) {
		query.append(currentDatabase() == TERADATA && nonNull(views) 
				? stream(views)
						.map(query::viewAlias)
						.map(v-> v+".*")
						.collect(joining(SCOMA))
				: "*");
	}
	
	@Override
	public JDBCType getType() {
		return null;
	}
	
	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> cons) {
		if(nonNull(views)) {
			query.declare(views); //declare views
		}
		return -1;
	}
	
	@Override
	public String getTag() {
		return null;
	}
}
