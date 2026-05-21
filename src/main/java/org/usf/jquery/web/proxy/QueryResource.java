package org.usf.jquery.web.proxy;

import static java.util.Objects.nonNull;
import static org.usf.jquery.web.proxy.Resource.Match.NONE;
import static org.usf.jquery.web.proxy.Resource.Match.VALID;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.Query;
import org.usf.jquery.core.View;
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

	private final Query query;

	@Override
	public View getView() {
		return query;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T invokeResource(String id, Class<T> type, Entry[] args, RequestContext ctx) {
		if(type.isAssignableFrom(Column.class) && nonNull(query.getSelects())) {
			return query.getSelects().stream()
					.filter(c-> id.equals(c.getTag()))
					.findFirst().map(Column.class::cast)
					.map(c-> (T)query.column(ctx.getStore().dialect().suroundColumnAlias(id), c.getType(), id))
					.orElse(null);
		}
		throw new NoSuchResourceException("no exposed method with id '" + id + "' found in query resource");
	}
	
	@Override
	public Match exposes(String id, Class<?> type) {
		return type.isAssignableFrom(Column.class) 
				&& nonNull(query.getSelects())
				&& query.getSelects().stream().anyMatch(c-> id.equals(c.getTag())) ? VALID : NONE;
	}
}
