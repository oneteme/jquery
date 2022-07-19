package org.usf.jdbc.jquery.web;

import static org.usf.jdbc.jquery.web.ResourceNotFoundException.tableNotFoundException;

import java.util.Map;

import org.usf.jdbc.jquery.RequestQuery;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RequestQueryParamResolver {
	
	public RequestQuery requestQuery(@NonNull RequestQueryParam ant, @NonNull Map<String, String[]> parameterMap) {
		return DatabaseScanner.get().tableDescriptors().stream()
				.filter(e-> e.name().equals(ant.name()))
				.findAny()
				.orElseThrow(()-> tableNotFoundException(ant.name()))
				.query(ant, parameterMap);
	}
}
