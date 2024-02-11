package org.usf.jquery.web;

import static java.util.Objects.isNull;

import java.util.LinkedHashMap;
import java.util.Map;

import org.usf.jquery.core.DBView;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestContext {
	
	private static final ThreadLocal<RequestContext> local = new ThreadLocal<>();
	
	private final Map<String, DBView> views = new LinkedHashMap<>();
	
	public DBView getViews(String name) {
		return views.get(name);
	}
	
	public void setViews(String name, DBView v) {
		views.put(name, v);
	}
	
	public DBView[] views() {
		return views.values().stream()
				.map(v-> v instanceof CompletableViewQuery ? ((CompletableViewQuery)v).getQuery() : v)
				.toArray(DBView[]::new);
	}

	public static final RequestContext requestContext() {
		var rc = local.get();
		if(isNull(rc)) {
			rc = new RequestContext();
			local.set(rc);
		}
		return rc;
	}

	public static final void clearContext() {
		local.remove();
	}
}
