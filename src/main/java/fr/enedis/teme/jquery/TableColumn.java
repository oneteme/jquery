package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public interface TableColumn extends DBColumn {

	default String toSql(DBTable table) {
		var name = requireNonNull(table).getColumnName(this);
		return ofNullable(name)
			.orElseThrow(()-> new IllegalArgumentException(this + " not in " + table));
	}
}