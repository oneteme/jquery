package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Iterator;
import java.util.function.Consumer;
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
	
	public SqlStringBuilder(String v) { //buffer++
		this.sb = new StringBuilder(v.length() + 50).append(v);
	}

	public SqlStringBuilder appendIf(boolean condition, String s) {
		return condition ? append(s) : this;
	}
	
	public SqlStringBuilder appendIf(boolean condition, Supplier<String> sup) {
		return condition ? append(sup.get()) : this;
	}

	public SqlStringBuilder appendIf(boolean condition, Supplier<String> sup, Supplier<String> orSup) {
		return append(condition ? sup.get() : orSup.get());
	}
	
	public SqlStringBuilder appendIf(boolean condition, String sup, String orElse) {
		return append(condition ? sup : orElse);
	}

	public <T> SqlStringBuilder appendEach(T[] arr, String separator, Consumer<T> fn) {
		return appendEach(arr, separator, fn, EMPTY, EMPTY);
	}

	public <T> SqlStringBuilder appendEach(T[] arr, String separator, Consumer<T> fn, String prefix, String suffix) {
		sb.append(prefix);
		if(!isEmpty(arr)) {
			var i=0;
			fn.accept(arr[i]);
			for(++i; i<arr.length; i++) {
				sb.append(separator);
				fn.accept(arr[i]);
			}
		}
		sb.append(suffix);
		return this;
	}
	
	public <T> SqlStringBuilder forEach(Iterator<T> it, String separator, Consumer<T> cons) {
		if(it.hasNext()) {
			cons.accept(it.next());
			while(it.hasNext()) {
				append(separator);
				cons.accept(it.next());
			}
		} 
		return this;
	}
	
	public SqlStringBuilder as(String v) {
		return append(" AS ").append(v);
	}

	public SqlStringBuilder from(String v) {
		return from().append(v);
	}
	
	public SqlStringBuilder from() {
		return append(" FROM ");
	}

	public SqlStringBuilder function(String name, Runnable args) {
		return append(name).parenthesis(args);
	}

	public SqlStringBuilder parenthesis(Runnable exec) {
		openParenthesis();
		exec.run();
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

	public static String parenthese(String op) { 
		return "(" + op + ")";
	}

	public static String member(String parent, String child) { 
		return isNull(parent) ? child : parent + "." + child;
	}
}
