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
	
	public DBView getView(DBView v) {
		return views.get(v.id());
	}
	
	public void setViews(DBView v) {
		views.put(v.id(), v);
	}
	
	public DBView[] views() {
		return views.values().toArray(DBView[]::new);
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
