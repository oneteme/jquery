package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface JavaArgumentParser {
	
	Object parse(RequestEntryChain entry, TableDecorator td);
}
