package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.web.ParameterInvalidValueException.invalidParameterValueException;
import static java.lang.Byte.parseByte;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Short.parseShort;
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

@Getter
@RequiredArgsConstructor 
public final class ColumnMetadata {
	
	private final String name;
	private final int type;
	private final int length;

	public Object parseArgs(String... values) {
		List<Object> list = new ArrayList<>(values.length);
		Function<String, Object> parser = this::parseArg;
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

	public Object parseArg(String v){

		switch(type) {
		case VARCHAR  : return v;
		case INTEGER  : return parseInt(v);
		case BIGINT   : return parseLong(v);
		case DECIMAL  : return parseDouble(v);
		case SMALLINT : return parseShort(v);
		case TINYINT  : return parseByte(v);
		case CHAR  	  : return v.charAt(0); //check length==1
		case DATE     : return Date.valueOf(LocalDate.parse(v)); //TD check
		case TIMESTAMP: return Timestamp.from(Instant.parse(v)); //TD check
		default       : throw new UnsupportedOperationException("Unsupported dbType " + type);
		}
	}
	
	@Override
	public String toString() {
		return "(name="+ name + ", type=" + type + ", length=" + length + ")";
	}
	
	static final ColumnMetadata defaultColumnMetadata() {
		return new ColumnMetadata(null, VARCHAR, 0);
	}
}