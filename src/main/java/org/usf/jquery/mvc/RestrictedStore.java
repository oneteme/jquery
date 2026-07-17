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
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class RestrictedStore implements StoreCatalog {

	@Delegate(types = StoreCatalog.class)
	private final StoreCatalog store;
	private final int maxCols;
	private final int maxRows;
	private final boolean aggregateOnly;
	private final Set<String> excludeResources;
	private final Set<String> excludeDialects;
	
	
	@Override
	public RequestContext createContext(String defaultDataset) {
		if(!excludeResources.contains(defaultDataset)) {
			return StoreCatalog.super.createContext(defaultDataset); //because of @Delegate, this will call the store.createContext(defaultDataset)
		}
		throw new IllegalAccessError("Dataset " + defaultDataset + " is not accessible or does not exist");
	}
	
	@Override
	public <T> ResourceInvoker<T> lookup(String resource, Class<T> type) {
		if(!excludeResources.contains(resource)) {
			return store.lookup(resource, type);
		}
		throw new ResourceAccessException("Resource " + resource + " is not allowed");
	}
	
	@Override
	public <T> ResourceInvoker<T> lookup(Catalog sub, String resource, Class<T> type) {
		if(!excludeResources.contains(resource)) {
			return store.lookup(sub, resource, type); //not sure !
		}
		throw new ResourceAccessException("Sub-resource " + resource + " is not allowed");
	}
	
	@Override
	public <T> ResourceInvoker<T> lookupDialect(String resource, Class<T> type) {
		if(!excludeDialects.contains(resource)) {
			return store.lookupDialect(resource, type);
		}
		throw new ResourceAccessException("Dialect " + resource + " is not allowed");
	}
	
	@Override
	public <T> ResourceInvoker<T> lookupDialect(String resource, Class<T> type, Object composer) {
		if(!excludeDialects.contains(resource)) {
			return store.lookupDialect(resource, type, composer);
		}
		throw new ResourceAccessException("Resource " + resource + " is not allowed");
	}
	
	@Override
	public <T> T execute(Query query, ResultSetMapper<T> mapper) {
		if(maxCols > 0 && query.getSelects().size() > maxCols) {
			throw new ResourceAccessException("Query has too many columns: " + query.getSelects().size() + " > " + maxCols);
		}
		if(aggregateOnly && !query.isAggregation()) {
			throw new ResourceAccessException("Query is not an aggregation query");
		}
		return store.execute(query, maxRows > 0 ? resultSetLimiter(mapper, maxRows) : mapper);
	}

	@Override
	public <T extends StoreCatalog> T unwrap(Class<T> type) {
		return store.unwrap(type);
	}
	
	public static StoreCatalog restrict(StoreCatalog store, int maxCols, int maxRows, boolean aggregationOnly, Set<String> excludeResources, Set<String> excludeDialects) {
		if(isEmpty(excludeResources) && isEmpty(excludeDialects) && maxCols <= 0 && maxRows <= 0) {
			return store;
		}
		return new RestrictedStore(store, maxCols, maxRows, aggregationOnly,
				requireNonNullElseGet(excludeResources, Collections::emptySet), 
				requireNonNullElseGet(excludeDialects, Collections::emptySet));
	}
}
