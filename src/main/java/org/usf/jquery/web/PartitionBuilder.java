package org.usf.jquery.web;

import org.usf.jquery.core.Partition;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface PartitionBuilder {

	Partition build();
}
