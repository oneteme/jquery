package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.requireNonBlank;
import static java.util.Objects.requireNonNull;

public interface TableColumn extends DBColumn {

	default String toSql(DBTable table) {
		return requireNonBlank(requireNonNull(table).getColumnName(this));
	}
}