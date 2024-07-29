package org.usf.jquery.web;

import org.usf.jquery.core.DBView;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ViewBuilder {

	DBView build();
	
}
