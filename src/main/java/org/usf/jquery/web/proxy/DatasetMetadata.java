package org.usf.jquery.web.proxy;

import static java.util.Objects.nonNull;

import java.time.Instant;
import java.util.Map;

import org.usf.jquery.core.JDBCType;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 * 
 */
@ToString
@Slf4j
@RequiredArgsConstructor
public class DatasetMetadata {

	private final String name;
	private final DatasetType type;
	private final Map<String, ColumnMetadata> columns;
	private final Instant update;
	
	public JDBCType getColumnType(String name) {
		if(columns.containsKey(name)) {
			var meta = columns.get(name);
			return nonNull(meta) ? meta.getType() : null;
		}
		log.warn("column {} not found in view {}", name, this.name);
		return null;
	}
	
}
