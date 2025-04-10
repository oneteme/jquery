package org.usf.jquery.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlStringBuilder {
	
	static final String EMPTY = "";
	static final String COMA  = ",";
	static final String SPACE = " ";
	static final String QUOTE = "'";
	static final String DQUOT = "\"";
	static final String SCOMA  = COMA + SPACE;
	
	public static String space(String op) {
		return SPACE + op + SPACE;
	}

	public static String quote(String op) {
		return QUOTE + op + QUOTE;
	}

	public static String doubleQuote(String op) {
		return DQUOT + op + DQUOT;
	}
}
