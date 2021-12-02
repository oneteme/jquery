package fr.enedis.teme.jquery;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.util.Collection;

public final class NullFilter implements DBFilter {

	private final DBColumn column;
	private final boolean invert;

	public NullFilter(DBColumn column, boolean invert) {
		this.column = requireNonNull(column);
		this.invert = invert;
	}

	@Override
	public String toSql(DBTable table) {
		var v = column.toSql(table) + " IS";
		if (invert) {
			v += " NOT";
		}
		return v + " NULL";
	}

	@Override
	public Collection<Object> args() {
		return emptyList(); // no args
	}
	
}
