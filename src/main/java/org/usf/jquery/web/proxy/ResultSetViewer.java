package org.usf.jquery.web.proxy;

import org.usf.jquery.core.QueryExecutor;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ResultSetViewer {
	
	QueryExecutor<?> view(HttpServletResponse res);
}