package org.usf.jquery.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
final class SubView implements DBView {
	
	private final QueryView query;

	@Override
	public void build(QueryBuilder builder) {
		query.build(builder);
	}
}
