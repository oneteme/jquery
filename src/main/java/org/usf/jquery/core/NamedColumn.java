package org.usf.jquery.core;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class NamedColumn implements TaggableColumn {

	@NonNull
	@Delegate
	private final DBColumn column;
	@NonNull
	private final String reference;

	@Override
	public String reference() {
		return reference;
	}
}
