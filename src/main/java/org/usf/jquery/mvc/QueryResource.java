package org.usf.jquery.mvc;

import static java.util.Objects.nonNull;
import static org.usf.jquery.mvc.ResourceInvoker.ofObject;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.Query;
import org.usf.jquery.core.View;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor
public final class QueryResource implements DatasetCatalogue {

	@NonNull private final Query query;

	@Override
	public View getView() {
		return query;
	}	
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> ResourceInvoker<T> lookup(String id, Class<T> type) {
		if(Column.class.isAssignableFrom(type) && nonNull(query.getSelects())) {
			return query.getSelects().stream()
					.filter(c-> id.equals(c.getTag()))
					.findFirst().map(Column.class::cast)
					.map(c-> query.column(query.getStore().dialect().suroundColumnAlias(id), c.getType(), id)) //proxy ?
					.map(v-> ofObject(true, (T)v, type)) //no arguments
					.orElse(null);
		}
		return null;
	}
}
