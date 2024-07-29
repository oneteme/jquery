package org.usf.jquery.web;

import org.usf.jquery.core.ViewJoin;

/**
 * 
 * @author u$f
 *
 */
public interface JoinBuilder {

	ViewJoin[] build(ViewDecorator[] selectedViews);
	
}
