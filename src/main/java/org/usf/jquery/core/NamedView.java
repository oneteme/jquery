package org.usf.jquery.core;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class NamedView implements TaggableView {

	@Delegate
	private final DBView view;
	private final String tag;
	
	@Override
	public String tagname() {
		return tag;
	}
	
	@Override
	public NamedView as(String name) { // map
		return new NamedView(unwrap(), name);
	}
	
	public DBView unwrap() {
		return view;
	}
	
	@Override 
	public String toString() {
		return view.toString();
	}
}
