package org.usf.jquery.web.mvc;

import static java.util.Collections.synchronizedMap;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static java.util.function.Predicate.not;
import static org.usf.jquery.core.Mappers.keyValueMapper;
import static org.usf.jquery.web.mvc.ResponseMappers.asciiResponseWriter;
import static org.usf.jquery.web.mvc.ResponseMappers.csvResponseWriter;
import static org.usf.jquery.web.mvc.ResponseMappers.mvcModelMapper;
import static org.usf.jquery.web.mvc.ResponseMappers.mvcViewBinder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.sql.DataSource;

import org.usf.jquery.core.DynamicModel;
import org.usf.jquery.core.Query;
import org.usf.jquery.core.QueryExecutor;
import org.usf.jquery.core.ResultSetMapper;
import org.usf.jquery.core.SimpleQueryExecutor;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 * 
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MvcExecutors {
	
	private static final Map<String, ResponseEntity> queryQueue = synchronizedMap(new LinkedHashMap<>()); //timeout !?
	
	public static QueryExecutor<?> executor(HttpServletResponse res, Optional<String> view) {
		return switch (view.filter(not(String::isEmpty)).orElse("json")) {
		case "json" -> new SimpleQueryExecutor<>(keyValueMapper());
		case "csv" -> new SimpleQueryExecutor<>(csvResponseWriter(res));
		case "ascii" -> new SimpleQueryExecutor<>(asciiResponseWriter(res));
		case "google.v1"-> deferredExecutor(mvcViewBinder(res, "static/google.v1.html")); 
		default -> throw new UnsupportedOperationException("view="+view);
		};
	}
	
	public static <T> QueryExecutor<T> deferredExecutor(ViewBinder<T> binder) {
		return (qry, ds)->{
			var id = randomUUID().toString();
			var res = binder.bind(id);
			queryQueue.put(id, new ResponseEntity(qry, ds));
			return res;
		};
	}
	
	public static List<DynamicModel> callback(String id, HttpServletResponse res){
		return callback(id, mvcModelMapper(res));
	}
	
	public static <T> T callback(String id, ResultSetMapper<T> mapper){
		var o = queryQueue.remove(id);
		if(nonNull(o)) {
			return new SimpleQueryExecutor<>(mapper).execute(o.getQuery(), o.getDataSource());
		}
		throw new NoSuchElementException(id + " not found");
	}
	
	@Getter
	@RequiredArgsConstructor
	static final class ResponseEntity {

		private final Query query;
		private final DataSource dataSource;
	}
}
