package org.usf.jquery.web;

import org.usf.jquery.core.DBColumn;

@FunctionalInterface
public interface ColumnBuilder {

	DBColumn column(TableDecorator table);
}