package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.web.JQueryContext.context;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.usf.jquery.core.DBView;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestContext {
	
	private static final ThreadLocal<RequestContext> local = new ThreadLocal<>();
	
	private final Map<String, TableDecorator> tdMap;
	private final Map<String, ColumnDecorator> columns;
	private final Map<String, DBView> views = new LinkedHashMap<>(); //work views

	public Optional<TableDecorator> lookupViewDecorator(String id) {
		log.trace("lookup view decorator : {}", id);
		return ofNullable(tdMap.get(id));
	}

	public Optional<ColumnDecorator> lookupColumnDecorator(String id) {
		log.trace("lookup column decorator : {}", id);
		return ofNullable(columns.get(id));
	}

	public DBView getView(String id) {
		return views.get(id);
	}
	
	public void putView(DBView v) {
		views.put(v.id(), v);
	}
	
	public DBView[] views() {
		return views.values().toArray(DBView[]::new);
	}

	public static final RequestContext requestContext() {
		var rc = local.get();
		if(isNull(rc)) {
			var jc = context(); //can filter view & column 
			rc = new RequestContext(new HashMap<>(jc.getTables()), new HashMap<>(jc.getColumns()));
			local.set(rc);
		}
		return rc;
	}

	public static final void clearContext() {
		local.remove();
	}
}
