package org.usf.jquery.core;

import java.util.stream.Stream;

/**
 * 
 * @author u$f
 *
 */
public interface Groupable extends Nested {
	
	Stream<DBColumn> groupKeys();
}
