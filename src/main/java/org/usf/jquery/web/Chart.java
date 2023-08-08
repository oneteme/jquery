package org.usf.jquery.web;

import static java.nio.file.Files.readString;
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
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.web.NoSuchResourceException.throwNoSuchColumnException;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.function.Function;

import org.usf.jquery.core.ResultMapper;
import org.usf.jquery.core.SqlStringBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class Chart implements ResultMapper<Void> {
	
	private static final String CHART_DATA = "$chart_data";
	private static final String CHART_TITLE = "$chart_title";

    private final Writer writer;
    private final String x; 
    private final String y;
	
    public Void map(ResultSet rs) throws SQLException {
		log.debug("mapping results...");
		var sb = new StringBuilder();
		var xForm = formatter(rs.getMetaData(), x);
		var yForm = formatter(rs.getMetaData(), y);
		sb.append("[").append(quote(x)).append(",").append(quote(y)).append("]");
		while(rs.next()) {
			sb.append(",[")
			.append(xForm.apply(rs.getObject(x)))
			.append(",")
			.append(yForm.apply(rs.getObject(y)))
			.append("]");
		}
		try {
			var content = readString(Paths.get(getClass().getResource("../chart/chart.google.html").toURI()))
					.replace(CHART_DATA, sb.toString());
			writer.write(content);
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return null;
    }
    
    private static Function<Object, String> formatter(ResultSetMetaData rsm, String col) throws SQLException {

		for(var i=0; i<rsm.getColumnCount(); i++) {
			if(rsm.getColumnName(i+1).equals(col)) {
				Function<Object, String> fn = String::valueOf;
				return isNumer(rsm.getColumnType(i+1)) ? fn : fn.andThen(SqlStringBuilder::quote);
			}
		}
		throw throwNoSuchColumnException(col);
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
}
