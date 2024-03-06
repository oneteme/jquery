package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.web.JQueryContext.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.usf.jquery.core.DBQuery;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestContext {
	
	private static final ThreadLocal<RequestContext> local = new ThreadLocal<>();
	
	private final Map<String, TableDecorator> viewDecorators;
	private final Map<String, ColumnDecorator> columnDecorators;
	private final Map<String, DBQuery> workQueries = new LinkedHashMap<>(); //work queries

	public Optional<TableDecorator> lookupViewDecorator(String id) {
		log.trace("lookup view decorator : {}", id);
		return ofNullable(viewDecorators.get(id));
	}

	public Optional<ColumnDecorator> lookupColumnDecorator(String id) {
		log.trace("lookup column decorator : {}", id);
		return ofNullable(columnDecorators.get(id));
	}

	public Optional<DBQuery> lookupView(String id) {
		log.trace("lookup view : {}", id);
		return ofNullable(workQueries.get(id));
	}
	
	public void putViewDecorator(TableDecorator v) {
		if(!viewDecorators.containsKey(v.identity())) {
			viewDecorators.put(v.identity(), v);
		}
		else {
			throw new IllegalArgumentException(v.identity() + " already exist");
		}
	}
	
	public void putWorkQuery(DBQuery v) {
		workQueries.put(v.id(), v);
	}
	
	public Collection<DBQuery> popQueries() {
		var q = new ArrayList<>(workQueries.values());
		workQueries.clear();
		return q;
	}
	
	public static final RequestContext currentContext() {
		var rc = local.get();
		if(isNull(rc)) {
			var jc = context(); //can filter view & column 
			rc = new RequestContext(new HashMap<>(jc.getTables()), new HashMap<>(jc.getColumns()));
			local.set(rc);
		}
		return rc;
	}
	
	public void clearWorkQueries() {
		workQueries.clear();
	}

	public static final void clearContext() {
		local.remove();
	}
}
