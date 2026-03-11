package org.usf.jquery.core;

import java.util.function.Consumer;

import lombok.Getter;

@Getter
public final class JoinsClause implements DBObject {

	private final ViewJoin[] joins;

	public JoinsClause(ViewJoin... joins) {
		super();
		this.joins = joins;
	}
	
	@Override
	public int compose(QueryComposer composer, Consumer<Column> groupKeys) {
		return DBObject.composeNested(composer, groupKeys, joins);
	}

	@Override
	public void build(QueryBuilder query, Object... args) {
		if(joins.length > 0) {
			joins[0].build(query, args);
			for(var j : joins) {
				query.append(" ");
				j.build(query, args);
			}
		}
	}
	
	public static JoinsClause joins(ViewJoin... joins) {
		return new JoinsClause(joins);
	}
	
	@Override
	public String toString() {
		var sb = new StringBuilder();
		for(var j : joins) {
			sb.append(j.toString()).append(" ");
		}
		return sb.toString();
	}
}
