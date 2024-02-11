package org.usf.jquery.web;

import org.usf.jquery.core.DBView;
import org.usf.jquery.core.ViewQuery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
final class CompletableViewQuery implements DBView {
	
	@Delegate
	private final ViewQuery query;

}
