package org.usf.jquery.mvc;

import org.usf.jquery.core.QueryExecutor;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ResultSetViewer {
	
	QueryExecutor<?> whith(HttpServletResponse res);
}