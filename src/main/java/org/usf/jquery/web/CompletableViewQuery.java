package org.usf.jquery.web;

import org.usf.jquery.core.DBQuery;
import org.usf.jquery.core.QueryView;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class CompletableViewQuery implements DBQuery {
	
	@Delegate
	private final QueryView query;

	@Override
	public String toString() {
		return query.toString();
	}
}
