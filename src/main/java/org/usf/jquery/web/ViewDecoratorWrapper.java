package org.usf.jquery.web;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
final class ViewDecoratorWrapper implements ViewDecorator {
		
	@Delegate
	private final ViewDecorator view;
	private final String id;
		
	@Override
	public String identity() {
		return id;
	}
}