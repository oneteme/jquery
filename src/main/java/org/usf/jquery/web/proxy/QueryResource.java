package org.usf.jquery.web.proxy;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;

import org.usf.jquery.core.DBView;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.QueryView;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor
public final class QueryResource implements DatasetResource {

	private final QueryView query;

	@Override
	public DBView getView() {
		return query;
	}

	@Override
	public <T> T invokeResource(String id, Class<T> type, Entry[] args, RequestContext ctx) {
		if(type.isAssignableFrom(NamedColumn.class) && nonNull(query.getColumns())) {
			return stream(query.getColumns())
					.filter(c-> id.equals(c.getTag()))
					.findFirst().map(type::cast)
					.orElse(null);
		}
		throw new IllegalArgumentException("no exposed method with id '" + id + "' found in query resource");
	}
	
	@Override
	public boolean exposes(String id, Class<?> type) {
		return type.isAssignableFrom(NamedColumn.class) 
				&& nonNull(query.getColumns())
				&& stream(query.getColumns()).anyMatch(c-> id.equals(c.getTag()));
	}
}
