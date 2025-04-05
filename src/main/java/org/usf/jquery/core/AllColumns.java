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
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		String s = "*";
		if(currentDatabase() == TERADATA) {
			var arr = nonNull(views) ? views : ctx.views().toArray(DBView[]::new);
			if(nonNull(arr)) {
				s = stream(arr)
					.map(ctx::viewAlias)
					.map(v-> v +".*")
					.collect(joining(SCOMA));
			}
		}
		sb.append(s);
	}
	
	@Override
	public JDBCType getType() {
		return null;
	}
	
	@Override
	public int declare(RequestComposer builder, Consumer<DBColumn> cons) {
		if(nonNull(views)) {
			builder.from(views); //declare views
		}
		return -1;
	}
	
	@Override
	public String getTag() {
		return null;
	}
}
