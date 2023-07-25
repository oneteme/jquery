package org.usf.jquery.web;

import static org.usf.jquery.web.DatabaseScanner.database;

import java.util.Map;

import org.usf.jquery.core.RequestQuery;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class RequestQueryParamResolver {
	
	public RequestQuery requestQuery(@NonNull RequestQueryParam ant, @NonNull Map<String, String[]> parameterMap) {
		return database()
				.getTable(ant.name())
				.query(ant, parameterMap);
	}
}
