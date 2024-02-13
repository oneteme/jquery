package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.web.JQueryContext.context;
import static org.usf.jquery.web.NoSuchResourceException.throwNoSuchColumnException;
import static org.usf.jquery.web.NoSuchResourceException.throwNoSuchTableException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.usf.jquery.core.DBView;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestContext {
	
	private static final ThreadLocal<RequestContext> local = new ThreadLocal<>();
	
	private final Map<String, TableDecorator> tdMap;
	private final Map<String, ColumnDecorator> columns;
	private final Map<String, DBView> views = new LinkedHashMap<>(); //work views

	public boolean isDeclaredView(String id) {
		return tdMap.containsKey(id);
	}
	
	public TableDecorator getViewDecorator(String id) {
		return ofNullable(tdMap.get(id)).
				orElseThrow(()-> throwNoSuchTableException(id));
	}

	public void putViewDecorator(TableDecorator td) {
		if(tdMap.containsKey(td.identity())) {
			throw new IllegalArgumentException(td.identity() + "already exist");
		}
		tdMap.put(td.identity(), td);
	}

	public boolean isDeclaredColumn(String id) {
		return columns.containsKey(id);
	}

	public ColumnDecorator getColumnDecorator(String value) {
		return ofNullable(columns.get(value))
				.orElseThrow(()-> throwNoSuchColumnException(value));
	}

	public void putColumnDecorator(ColumnDecorator cd) {
		if(columns.containsKey(cd.identity())) {
			throw new IllegalArgumentException(cd.identity() + "already exist");
		}
		columns.put(cd.identity(), cd);
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
			var jc = context();
			rc = new RequestContext(new HashMap<>(jc.getTables()), new HashMap<>(jc.getColumns()));
			local.set(rc);
		}
		return rc;
	}

	public static final void clearContext() {
		local.remove();
	}
}
