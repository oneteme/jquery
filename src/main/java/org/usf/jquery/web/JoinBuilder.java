package org.usf.jquery.web;

import org.usf.jquery.core.ViewJoin;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface JoinBuilder {

	ViewJoin[] build();
}
