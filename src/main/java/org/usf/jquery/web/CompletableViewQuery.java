package org.usf.jquery.web;

import org.usf.jquery.core.DBView;
import org.usf.jquery.core.ViewQuery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@Getter
@RequiredArgsConstructor
final class CompletableViewQuery implements DBView {
	
	@Delegate
	private final ViewQuery query;

}
