package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlStringBuilder.DOT;
import static org.usf.jquery.core.Validation.requireLegalVariable;

import java.util.function.Consumer;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public class ViewColumn implements NamedColumn, Driven<ViewColumn, String> {

	private final String name;
	private final DBView view; //optional
	private final JDBCType type; //optional
	private final String tag;  //optional
	private final Adjuster<String> adjsuter;
	
	ViewColumn(String name, DBView view, JDBCType type, String tag, Adjuster<String> adjsuter) {
		this.name = requireLegalVariable(name);
		this.view = view;
		this.type = type;
		this.tag = tag;
		this.adjsuter = adjsuter;
	}
	
	public ViewColumn(String name, DBView view, JDBCType type, String tag) {
		this(name, view, type, tag, null);
	}

	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		if(nonNull(view)) {
			query.declare(view);
		}
		groupKeys.accept(this);
		return 0;
	}
	
	@Override
	public void build(QueryBuilder query) {
		if(nonNull(view)) {
			query.appendViewAlias(view, DOT);
		}
		query.append(name, adjsuter);
	}
	
	@Override
	public ViewColumn adjuster(Adjuster<String> adjsuter) {
		return new ViewColumn(name, view, type, tag, adjsuter);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
