package org.usf.jquery.web;

import org.usf.jquery.core.DBView;
import org.usf.jquery.core.ViewQuery;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class CompletableViewQuery implements DBView {
	
	@Delegate
	private final ViewQuery query;

	@Override
	public String toString() {
		return query.toString();
	}
}
