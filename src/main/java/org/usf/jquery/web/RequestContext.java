package org.usf.jquery.web;

import static java.util.Optional.ofNullable;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.Parameters.VIEW;

import java.util.Map;
import java.util.Optional;

import org.usf.jquery.core.ColumnProxy;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.QueryComposer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class RequestContext {
	
	private final ContextEnvironment context;
	private final ViewDecorator defaultView;
	private final QueryComposer query;

	public  QueryComposer parseQuery(Map<String, String[]> parameterMap) {
		return context.getParser().parse(this, parameterMap);
	}
	
	Optional<NamedColumn> lookupDeclaredColumn(String name) {
		return query.getColumns().stream()
				.filter(ColumnProxy.class::isInstance) //tagged column only
				.filter(c-> name.equals(c.getTag()))
				.findAny();
	}

	public static RequestContext requestContext(ContextEnvironment context, String defaultView) {
		return new RequestContext(context, 
				context.lookupRegisteredView(defaultView).orElseThrow(()-> noSuchResourceException(VIEW, defaultView)), 
				new QueryComposer(context.getMetadata().getType()));
	}
	
	public Optional<ViewDecorator> lookupRegisteredView(String name) { //+ declared
		return ofNullable(context.getViews().get(name));
	}
	
	public Optional<ColumnDecorator> lookupRegisteredColumn(String name) {
		return ofNullable(context.getColumns().get(name));
	}
}
