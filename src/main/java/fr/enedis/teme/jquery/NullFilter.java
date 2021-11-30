package fr.enedis.teme.jquery;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.util.Collection;

public final class NullFilter implements Filter {

	private final Column column;
	private final boolean invert;

	public NullFilter(Column column, boolean invert) {
		this.column = requireNonNull(column);
		this.invert = invert;
	}

	public NullFilter(Column column) {
		this(column, false);
	}

	@Override
	public String toSql(Table table) {
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
