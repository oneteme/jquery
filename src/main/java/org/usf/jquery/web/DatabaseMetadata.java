package org.usf.jquery.web;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.DatabaseVendor.parseName;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.usf.jquery.core.DatabaseVendor;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class DatabaseMetadata {

	private final Map<String, ViewMetadata> views; //lazy loading
	private DatabaseVendor type;
	
	public DatabaseVendor getType() {
		return type;
	}
	
	public ColumnMetadata columnMetadata(ViewDecorator view, ColumnDecorator cd) {
		var meta = viewMetadata(view);
		return nonNull(meta) ? meta.columnMetadata(cd) : null;
	}
	
	public ViewMetadata viewMetadata(ViewDecorator view) {
		return views.get(view.identity());
	}
	
	public void fetch(DatabaseMetaData metadata, String schema) throws SQLException {
		type = parseName(null);
		if(nonNull(views)) {
			for(var v : views.values()) {
				v.fetch(metadata, schema);
			}
		}
	}
}