package org.usf.jquery.web.proxy;

import org.usf.jquery.core.QueryComposer;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 * 
 */
@Getter
@RequiredArgsConstructor
public final class MvcRequest {
	
	private final StoreResource store;
	private final QueryComposer composer;
	private final ResultSetViewer viewer;

	public Object execute() {
		return execute(null);
	}
	
	public Object execute(HttpServletResponse res) {
		return viewer.view(res).execute(composer, store);
	}
}
