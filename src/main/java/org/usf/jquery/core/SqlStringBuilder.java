package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 
 * @author u$f
 *
 */
public final class SqlStringBuilder {
	
	static final String EMPTY = "";
	static final String COMA  = ",";
	static final String SPACE = " ";
	static final String QUOTE = "'";
	static final String DQUOT = "\"";
	static final String SCOMA  = COMA + SPACE;
	
	private final StringBuilder sb;
	
	public SqlStringBuilder() {
		this.sb = new StringBuilder();
	}
	
	public SqlStringBuilder(int capacity) {
		this.sb = new StringBuilder(capacity);
	}
	
	public SqlStringBuilder appendIf(boolean condition, String s) {
		return condition ? append(s) : this;
	}
	
	public SqlStringBuilder appendIf(boolean condition, Supplier<String> sup) {
		return condition ? append(sup.get()) : this;
	}

	public <T> SqlStringBuilder appendIfNonNull(T o, Function<T, String> fn) {
		return nonNull(o) ? append(fn.apply(o)) : this;
	}
	
	public <T> SqlStringBuilder runIfNonNull(T o, Consumer<T> cons) {
		if(nonNull(o)) {
			cons.accept(o);
		}
		return this;
	}
	
	public <T> SqlStringBuilder runForeach(T[] arr, String delimiter, Consumer<T> fn) {
		return runForeach(arr, 0, delimiter, fn);
	}
	
	public <T> SqlStringBuilder runForeach(T[] arr, int idx, String delimiter, Consumer<T> fn) {
		return runForeach(arr, idx, delimiter, fn, EMPTY, EMPTY);
	}
	
	public <T> SqlStringBuilder runForeach(T[] arr, String delimiter, Consumer<T> fn, String prefix, String suffix) {
		return runForeach(arr, 0, delimiter, fn, prefix, suffix);
	}

	public <T> SqlStringBuilder runForeach(T[] arr, int idx, String delimiter, Consumer<T> fn, String prefix, String suffix) {
		requireNonNull(arr, "arr connot be null");
		if(idx < arr.length) {
			sb.append(prefix);
			if(!isEmpty(arr)) {
				var i=idx;
				fn.accept(arr[i]);
				for(++i; i<arr.length; i++) {
					sb.append(delimiter);
					fn.accept(arr[i]);
				}
			}
			sb.append(suffix);
		}
		else if(idx > arr.length) {
			throw new IndexOutOfBoundsException(idx);
		}// idx == arr.length 
		return this;
	}

	public <T> SqlStringBuilder runForeach(Iterator<T> it, String separator, Consumer<T> cons) {
		if(it.hasNext()) {
			cons.accept(it.next());
			while(it.hasNext()) {
				sb.append(separator);
				cons.accept(it.next());
			}
		} 
		return this;
	}

	public SqlStringBuilder as(String v) {
		return as().append(v);
	}
	
	public SqlStringBuilder as() {
		return append(" AS ");
	}

	public SqlStringBuilder function(String name, Runnable args) {
		return append(name).parenthesis(args);
	}

	public SqlStringBuilder parenthesis(Runnable exec) {
		openParenthesis();
		exec.run();
		return closeParenthesis();
	}
	
	public SqlStringBuilder parenthesis(String s) {
		openParenthesis();
		sb.append(s);
		return closeParenthesis();
	}
	
	public SqlStringBuilder openParenthesis() {
		sb.append('(');
		return this;
	}

	public SqlStringBuilder closeParenthesis() {
		sb.append(')');
		return this;
	}
	
	public SqlStringBuilder spacing(String s) {
		return space().append(s).space();
	}
	
	public SqlStringBuilder space() {
		sb.append(SPACE);
		return this;
	}
	
	public SqlStringBuilder append(String s) {
		sb.append(s);
		return this;
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}

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
