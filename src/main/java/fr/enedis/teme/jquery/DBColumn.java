package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public interface DBColumn extends Column {

	default String toSql(Table table) {
		var name = requireNonNull(table).getColumnName(this);
		return ofNullable(name)
			.orElseThrow(()-> new IllegalArgumentException(this + " not in " + table));
	}

}
