package org.usf.jquery.core;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class JoinsClause implements DBObject { //TODO : rename to JoinsClause or something else

	private final ViewJoin[] joins;

	@Override
	public int compose(QueryComposer composer, Consumer<DBColumn> groupKeys) {
		return 0; //TODO
	}

	@Override
	public void build(QueryBuilder query, Object... args) {
		//TODO
	}
	
	public static JoinsClause of(ViewJoin... joins) {
		return new JoinsClause(joins);
	}
	
}
