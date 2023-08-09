package org.usf.jquery.web;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.web.ResultWebView.WebType.STRING;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;

import org.usf.jquery.core.ResultMapper;
import org.usf.jquery.core.SqlStringBuilder;

import lombok.RequiredArgsConstructor;

public interface ResultWebView extends ResultMapper<Void>  {
	
	@RequiredArgsConstructor
	enum WebType {

		BOOLEAN(o-> ofNullable(o)
				.map(Object::toString)
				.orElse(null)),
		
		NUMBER(o-> ofNullable(o)
				.map(Object::toString)
				.orElse(null)),

		DATE(o-> ofNullable((Timestamp) o)
				.map(Timestamp::toInstant)
				.map(t-> "new Date('" + t + "')") //ISO
				.orElse(null)), 
		
		STRING(o-> ofNullable(o)
				.map(Object::toString)
				.map(SqlStringBuilder::quote)
				.orElse(null));
		
		private final Formatter<Object> formatter;
		
		public String format(Object o) {
			return formatter.format(o);
		}
		
		public String typeName() {
			return name().toLowerCase();
		}
		
		static WebType typeOf(int type) {
			switch (type) {
			case Types.BOOLEAN: return BOOLEAN;
			case Types.BIT:
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			case Types.BIGINT:
			case Types.REAL:
			case Types.FLOAT:
			case Types.DOUBLE:
			case Types.NUMERIC:
			case Types.DECIMAL: return NUMBER;
			//case Types.DATE: //need explicit cast
			//case Types.TIME: //need explicit cast
			case Types.TIMESTAMP: return DATE;
			default: return STRING;
			}
		}
	}
	
	@FunctionalInterface
	interface Formatter<T> {
		
		String format(T o);
		
		public static <T> Formatter<Collection<T>> formatCollection() {
			return c-> STRING.format(c.stream().map(Object::toString).collect(joining("_")));
		}
		
		public static <T> Formatter<Collection<T>> formatFirstItem(Formatter<T> f) {
			return c-> f.format(c.iterator().next());
		}
	}
}
