package org.usf.jquery.core;

import java.util.Optional;

/**
 * 
 * @author u$f
 *
 */
public interface QueryContext {

	Optional<NamedColumn> lookupDeclaredColumn(String name);
}