package org.usf.jquery.web;

import org.usf.jquery.core.DBColumn;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ColumnBuilder {

	DBColumn build(ViewDecorator table);
}