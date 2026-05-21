package org.usf.jquery.web.mvc;

import static java.util.Collections.synchronizedMap;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static java.util.function.Predicate.not;
import static org.usf.jquery.core.Mappers.keyValueMapper;
import static org.usf.jquery.core.QueryExecutor.defaultExecutor;
import static org.usf.jquery.web.mvc.ResponseMappers.asciiResponseWriter;
import static org.usf.jquery.web.mvc.ResponseMappers.csvResponseWriter;
import static org.usf.jquery.web.mvc.ResponseMappers.mvcModelMapper;
import static org.usf.jquery.web.mvc.ResponseMappers.mvcViewBinder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.usf.jquery.core.DynamicModel;
import org.usf.jquery.core.QueryExecutor;
import org.usf.jquery.core.SqlQuery;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 * 
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MvcExecutors {
	
	private static final Map<String, SqlQuery> queryQueue = synchronizedMap(new LinkedHashMap<>()); //timeout !?
	
	public static QueryExecutor<?> executor(HttpServletResponse res, Optional<String> view) {
		return switch (view.filter(not(String::isEmpty)).orElse("json")) {
		case "json" -> defaultExecutor(keyValueMapper());
		case "csv" -> defaultExecutor(csvResponseWriter(res));
		case "ascii" -> defaultExecutor(asciiResponseWriter(res));
		case "google.v1"-> deferredExecutor(mvcViewBinder(res, "static/google.v1.html"));
		default -> throw new UnsupportedOperationException("view="+view);
		};
	}

	public static <T> QueryExecutor<T> deferredExecutor(ViewBinder<T> binder) {
		return qry->{
			var id = randomUUID().toString();
			var res = binder.bind(id);
			queryQueue.put(id, qry);
			return res;
		};
	}
	
	public static List<DynamicModel> callback(String id, HttpServletResponse res){
		var qry = queryQueue.remove(id);
		if(nonNull(qry)) {
			return qry.execute(mvcModelMapper(res));
		}
		throw new NoSuchElementException(id + " not found");
	}
}
