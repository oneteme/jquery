package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface JavaArgumentParser {
	
	Object parseEntry(EntryChain entry, QueryContext context);
}
