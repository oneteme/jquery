package org.usf.jquery.core;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.sql.Types.BIGINT;
import static java.sql.Types.BIT;
import static java.sql.Types.BOOLEAN;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.REAL;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TINYINT;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor
public final class AsciiResultMapper implements ResultSetMapper<Void> {
	
	private static final int MAX_LENGTH = 50;

    private final DataWriter writer;
    private final Map<String, String> columns;
    
	public AsciiResultMapper(DataWriter writer) {
		this(writer, emptyMap());
	}

	@Override
	public Void map(ResultSet rs) throws SQLException {
		log.debug("mapping results...");
		var bg = currentTimeMillis();
        var rw = 0;
		var names = new String[rs.getMetaData().getColumnCount()];
		var sb = new StringBuilder(MAX_LENGTH * names.length).append("|");
		for(var i=0; i<names.length; i++) {
			names[i] = rs.getMetaData().getColumnLabel(i+1);
			if(columns.isEmpty() || columns.containsKey(names[i])) {
				var type = rs.getMetaData().getColumnType(i+1);
				var disp = rs.getMetaData().getColumnDisplaySize(i+1);
				int size = min(MAX_LENGTH, disp)+1;
				var sign = isNumer(type) ? 1 : -1;
				if(columns.containsKey(names[i])) {
					names[i] = columns.get(names[i]); //map names
				}
				sb.append("%").append(sign * max(size, names[i].length())).append("s|");
			}
		}
		var pattern = sb.toString();
		var div = format(pattern, array(names.length, "")).replace("|", "+").replace(" ", "-"); 
		try {
			writer.writeLine(div);
			writer.writeLine(format(pattern, (Object[])names));
			writer.writeLine(div);
			var data = new Object[names.length];
			while(rs.next()) {
				for(int i=0; i<names.length; i++) {
					data[i] = rs.getObject(names[i]);
					if(nonNull(data[i])) {
						var s = data[i].toString();
						if(s.length() > MAX_LENGTH) {
							data[i] = s.substring(0, MAX_LENGTH-2) + "..";
						}
					}
				}
				writer.writeLine(format(pattern, data));
                rw++;
			}
			writer.writeLine(div);
		} catch (IOException e) {
            throw new MappingException("error writing results", e);
		}
		log.info("{} rows mapped in {} ms", rw, currentTimeMillis() - bg);
		return null;
	}
	
	private static boolean isNumer(int type) {
		return switch (type) {
		case BOOLEAN, BIT, TINYINT, SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, NUMERIC, DECIMAL: yield true;
		default: yield false;
		};
	}
	
	private static Object[] array(int size, String v) {
		return range(0, size)
				.mapToObj(i-> v)
				.toArray(String[]::new);
	}	
}
