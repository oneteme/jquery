package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
public enum Keyword {

	COLUMN, DISTINCT, JOIN, OFFSET, LIMIT, ORDER;
	
	public String getValue() {
		return name().toLowerCase();
	}
}
