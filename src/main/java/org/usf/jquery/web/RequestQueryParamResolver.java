package org.usf.jquery.web;

import java.util.Map;
import java.util.NoSuchElementException;

import org.usf.jquery.core.RequestQuery;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RequestQueryParamResolver {
	
	public RequestQuery requestQuery(@NonNull RequestQueryParam ant, @NonNull Map<String, String[]> parameterMap) {
		return DatabaseScanner.get().tableDescriptors().stream()
				.filter(e-> e.identity().equals(ant.name()))
				.findAny()
				.orElseThrow(()-> new NoSuchElementException(ant.name() + " not found"))
				.query(ant, parameterMap);
	}
}
