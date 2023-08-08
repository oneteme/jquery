package org.usf.jquery.core;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.sql.Types.*;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor
public final class ResultAsciiExport implements ResultMapper<Void> {
	
	private static final int MAX_LENGTH = 20;

    private final RowWriter writer;
    private final Map<String, String> columns;
    
	public ResultAsciiExport(RowWriter printer) {
		this(printer, emptyMap());
	}

	@Override
	public Void map(ResultSet rs) throws SQLException {
		log.debug("exporting results...");
		var bg = currentTimeMillis();
        var rw = 0;
		var names = new String[rs.getMetaData().getColumnCount()];
		var sb = new StringBuilder(MAX_LENGTH * names.length).append("|");
		for(var i=0; i<names.length; i++) {
			names[i] = rs.getMetaData().getColumnLabel(i+1);
			if(columns.isEmpty() || columns.containsKey(names[i])) {
				var type = rs.getMetaData().getColumnType(i+1);
				var disp = rs.getMetaData().getColumnDisplaySize(i+1);
				int size = min(MAX_LENGTH, disp+1);
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
			writer.write(div);
			writer.write(format(pattern, (Object[])names)); //no title align
			writer.write(div);
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
				writer.write(format(pattern, data));
                rw++;
			}
			writer.write(div);
		} catch (IOException e) {
            throw new RuntimeException("error while exporting results", e);
		}
		log.info("{} rows exported in {} ms", rw, currentTimeMillis() - bg);
		return null;
	}
	
	private static boolean isNumer(int type) {
		switch (type) {
		case BOOLEAN:
		case BIT:
		case TINYINT:
		case SMALLINT:
		case INTEGER:
		case BIGINT:
		case REAL:
		case FLOAT:
		case DOUBLE:
		case NUMERIC:
		case DECIMAL: return true;
		default: return false;
		}
	}
	
	private static Object[] array(int size, String v) {
		return IntStream.range(0, size)
				.mapToObj(i-> v)
				.toArray(String[]::new);
	}
	
	public interface RowWriter {
		
		void write(String s) throws IOException;
	}
	
}
