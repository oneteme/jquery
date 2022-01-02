package fr.enedis.teme.jquery.web;

import static java.sql.Types.BIGINT;
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
import java.util.function.Function;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode(of = {"dbType", "length"})
@RequiredArgsConstructor 
final class ColumnMetadata {

	private final int dbType;
	private final int length;

	Function<String, Object> parser(){

		switch(dbType) {
		case VARCHAR  : return v-> v;
		case INTEGER  : return Integer::parseInt;
		case BIGINT   : return Long::parseLong;
		case DECIMAL  : return Double::parseDouble;
		case SMALLINT : return Short::parseShort;
		case TINYINT  : return Byte::parseByte;
		case DATE     : return v-> Date.valueOf(LocalDate.parse(v)); //TD check
		case TIMESTAMP: return v-> Timestamp.from(Instant.parse(v)); //TD check
		default       : throw new UnsupportedOperationException();
		}
	}

	@Override
	public String toString() {
		return "{dbType:" + dbType + ", length:" + length +"}";
	}

}