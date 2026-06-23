package org.usf.jquery.mvc;

import static java.util.Objects.requireNonNullElseGet;
import static org.usf.jquery.core.Mappers.resultSetLimiter;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Collections;
import java.util.Set;

import org.usf.jquery.core.Query;
import org.usf.jquery.core.ResultSetMapper;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class RestrictedStore implements StoreResource {

	@Delegate
	private final StoreResource store;
	private final int maxCols;
	private final int maxRows;
	private final boolean aggregate;
	private final Set<String> excludeResources;
	private final Set<String> excludeDialects;
	
	@Override
	public RequestContext createContext(String defaultDataset) {
		if(!excludeResources.contains(defaultDataset)) {
			return StoreResource.super.createContext(defaultDataset);
		}
		throw new IllegalAccessError("Dataset " + defaultDataset + " is not accessible or does not exist");
	}
	
	@Override
	public <T> ResourceInvoker<T> lookup(String resource, Class<T> type) {
		if(excludeResources.contains(resource)) {
			throw new ResourceAccessException("Resource " + resource + " is not allowed");
		}
		return store.lookup(resource, type);
	}
	
	@Override
	public <T> ResourceInvoker<T> lookupDialect(String resource, Class<T> type) {
		return store.lookupDialect(resource, type);
	}
	
	@Override
	public <T> ResourceInvoker<T> lookupDialect(String resource, Class<T> type, Object composer) {
		if(excludeDialects.contains(resource)) {
			throw new ResourceAccessException("Resource " + resource + " is not allowed");
		}
		return store.lookupDialect(resource, type, composer);
	}
	
	@Override
	public <T> T execute(Query query, ResultSetMapper<T> mapper) {
		if(maxCols > 0 && query.getSelects().size() > maxCols) {
			throw new ResourceAccessException("Query has too many columns: " + query.getSelects().size() + " > " + maxCols);
		}
		if(aggregate && !query.isAggregation()) {
			throw new ResourceAccessException("Query is not an aggregation query");
		}
		return store.execute(query, maxRows > 0 ? resultSetLimiter(mapper, maxRows) : mapper);
	}
	
	public static StoreResource restrict(StoreResource store, int maxCols, int maxRows, boolean aggregation, Set<String> excludeResources, Set<String> excludeDialects) {
		if(isEmpty(excludeResources) && isEmpty(excludeDialects) && maxCols <= 0 && maxRows <= 0) {
			return store;
		}
		return new RestrictedStore(store, maxCols, maxRows, aggregation,
				requireNonNullElseGet(excludeResources, Collections::emptySet), 
				requireNonNullElseGet(excludeDialects, Collections::emptySet));
	}
}
