package org.usf.jquery.web.proxy;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static org.usf.jquery.web.proxy.Resource.Match.NONE;
import static org.usf.jquery.web.proxy.Resource.Match.VALID;

import org.usf.jquery.core.DBView;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.QueryView;
import org.usf.jquery.web.NoSuchResourceException;

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
		if(type.isAssignableFrom(NamedColumn.class) && nonNull(query.getSelects())) {
			return stream(query.getSelects())
					.filter(c-> id.equals(c.getTag()))
					.findFirst().map(type::cast)
					.orElse(null);
		}
		throw new NoSuchResourceException("no exposed method with id '" + id + "' found in query resource");
	}
	
	@Override
	public Match exposes(String id, Class<?> type) {
		return type.isAssignableFrom(NamedColumn.class) 
				&& nonNull(query.getSelects())
				&& stream(query.getSelects()).anyMatch(c-> id.equals(c.getTag())) ? NONE : VALID;
	}
}
