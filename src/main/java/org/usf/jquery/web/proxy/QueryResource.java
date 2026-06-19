package org.usf.jquery.web.proxy;

import static java.util.Objects.nonNull;

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
	public <T> MethodInvoker<T> lookup(String id, Class<T> type) {
		if(type.isAssignableFrom(Column.class) && nonNull(query.getSelects())) {
			return query.getSelects().stream()
					.filter(c-> id.equals(c.getTag()))
					.findFirst().map(Column.class::cast)
					.map(c-> query.column(query.getStore().dialect().suroundColumnAlias(id), c.getType(), id)) //proxy ?
					.map(v-> new MethodInvoker<T>(true, args-> (T)v)) //no arguments
					.orElse(null);
		}
		throw new NoSuchResourceException("no exposed method with id '" + id + "' found in query resource");
	}
}
