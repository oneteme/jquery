package org.usf.jquery.web;

import java.util.Map;

import org.usf.jquery.core.QueryComposer;

/**
 * 
 * @author u$f
 *
 */
public interface RequestParser {
	
	QueryComposer parse(Environment env, String defaultView, Map<String, String[]> parameterMap);

}