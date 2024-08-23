package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface JavaArgumentParser {
	
	Object parseEntry(RequestEntryChain entry, ViewDecorator td);
}
