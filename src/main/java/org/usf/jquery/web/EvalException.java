package org.usf.jquery.web;

import static org.usf.jquery.core.SqlStringBuilder.quote;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class EvalException extends WebException {

	public EvalException(String message) {
		super(message);
	}

	static EvalException cannotEvaluateException(String type, String expression) {
		return new EvalException("cannot evaluate " + type +  " " + quote(expression));
	}
}
