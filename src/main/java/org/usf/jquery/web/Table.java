package org.usf.jquery.web;

import static java.nio.file.Files.readString;
import static java.sql.Types.BIGINT;
import static java.sql.Types.BIT;
import static java.sql.Types.BOOLEAN;
import static java.sql.Types.DATE;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.REAL;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.TINYINT;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.SqlStringBuilder.quote;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.usf.jquery.core.ResultMapper;
import org.usf.jquery.core.SqlStringBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class Table implements ResultMapper<Void> {
	
	private static final String DATA = "$data";
	private static final String COLUMN = "$columns";

    private final Writer writer;
	
    public Void map(ResultSet rs) throws SQLException {
		log.debug("mapping results...");
		var sb1 = new StringBuilder();
		var types = new WebTypes[rs.getMetaData().getColumnCount()];
		for(var i=0; i<rs.getMetaData().getColumnCount(); i++) {
			var name = rs.getMetaData().getColumnLabel(i+1);
			types[i] = typeOf(rs.getMetaData().getColumnType(i+1));
			sb1.append("[").append(quote(types[i].name().toLowerCase())).append(",").append(quote(name)).append("],");
		}
		sb1.deleteCharAt(sb1.length()-1); //last coma : dirty but less code
		var sb2 = new StringBuilder();
		while(rs.next()) {
			sb2.append("[")
			.append(types[0].getFormater().format(rs.getObject(1)));
			for(int i=1; i<types.length; i++) {
				sb2.append(",")
				.append(types[i].getFormater().format(rs.getObject(i+1)));
			}
			sb2.append("],");
		}
		sb2.deleteCharAt(sb2.length()-1); //last coma : dirty but less code

		try {
			var content = readString(Paths.get(getClass().getResource("../chart/table.google.html").toURI()))
					.replace(COLUMN, sb1.toString())
					.replace(DATA, sb2.toString());
			writer.write(content);
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return null;
    }
    

	private static WebTypes typeOf(int type) {
		switch (type) {
		case BIT:
		case TINYINT:
		case SMALLINT:
		case INTEGER:
		case BIGINT:
		case REAL:
		case FLOAT:
		case DOUBLE:
		case NUMERIC:
		case DECIMAL: return WebTypes.NUMBER;
		case DATE:
		case TIMESTAMP: return WebTypes.DATE;
		case BOOLEAN: return WebTypes.BOOLEAN;
		default: return WebTypes.STRING;
		}
	}
	
	@Getter
	@RequiredArgsConstructor
	enum WebTypes {
		
		DATE(o-> ofNullable((java.util.Date) o) //Timestamp | Date
				.map(java.util.Date::toInstant)
				.map(t-> "new Date('" + t + "')") //ISO
				.orElse(null)), 
		
		NUMBER(o-> ofNullable(o)
				.map(Object::toString)
				.orElse(null)), 
		
		STRING(o-> ofNullable(o)
				.map(Object::toString)
				.map(SqlStringBuilder::quote)
				.orElse(null)),
		
		BOOLEAN(o-> ofNullable(o)
				.map(Object::toString)
				.orElse(null));
		
		private final Formatter formater;
	}
	
	@FunctionalInterface
	interface Formatter {
		
		String format(Object rs) throws SQLException;	
	}
}
