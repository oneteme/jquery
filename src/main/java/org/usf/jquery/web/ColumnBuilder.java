package org.usf.jquery.web;

import org.usf.jquery.core.DBColumn;

/**
 * 
 * @author u$f
 * 
 * @See ColumnDecorator
 *
 */
@FunctionalInterface
public interface ColumnBuilder {

	DBColumn column(TableDecorator table);
}