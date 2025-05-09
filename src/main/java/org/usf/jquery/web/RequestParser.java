package org.usf.jquery.web;

import java.util.Map;

import org.usf.jquery.core.QueryComposer;

/**
 * 
 * @author u$f
 *
 */
public interface RequestParser {
	
	QueryComposer parse(RequestContext context, Map<String, String[]> parameterMap);

}