package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.web.ParameterInvalidValueException.invalidParameterValueException;
import static java.sql.Types.BIGINT;
import static java.sql.Types.CHAR;
import static java.sql.Types.DATE;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.INTEGER;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.TINYINT;
import static java.sql.Types.VARCHAR;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor 
public final class ColumnMetadata {

	private final String name;
	private final int type;
	private final int length;

	public Object parseArg(String value) {
		return parser().apply(value);
	}
	
	public Object parseArgs(String... values) {

		List<Object> list = new ArrayList<>(values.length);
		var parser = parser();
		for(String value : values) {
			try {
				list.add(parser.apply(value));
			}
			catch(Exception e) {
				throw invalidParameterValueException(value, e);
			}
		}
		return list.toArray();
	}

	Function<String, Object> parser(){

		switch(type) {
		case VARCHAR  : return v-> v;
		case INTEGER  : return Integer::parseInt;
		case BIGINT   : return Long::parseLong;
		case DECIMAL  : return Double::parseDouble;
		case SMALLINT : return Short::parseShort;
		case TINYINT  : return Byte::parseByte;
		case CHAR  	  : return v-> v.charAt(0); //check length==1
		case DATE     : return v-> Date.valueOf(LocalDate.parse(v)); //TD check
		case TIMESTAMP: return v-> Timestamp.from(Instant.parse(v)); //TD check
		default       : throw new UnsupportedOperationException("Unsupported dbType " + type);
		}
	}
}