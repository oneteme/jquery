package org.usf.jquery.web;

import org.usf.jquery.core.Partition;
import org.usf.jquery.core.QueryContext;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface PartitionBuilder {

	Partition build(QueryContext ctx);
	
}
