package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.function.Function.identity;

import java.util.Collection;
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
	
	final StringBuilder sb;

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

	public SqlStringBuilder appendEach(Collection<String> list, String separator) {
		return appendEach(list, separator, EMPTY, identity());
	}

	public <T> SqlStringBuilder appendEach(Collection<T> list, String separator, Function<T, String> fn) {
		return appendEach(list, separator, EMPTY, fn);
	}

	public <T> SqlStringBuilder appendEach(Collection<T> list, String separator, String prefix, Function<T, String> fn) {
		if(!list.isEmpty()) {
			var it = list.iterator();
			this.sb.append(prefix).append(fn.apply(it.next()));
			var before = separator + prefix;
			while(it.hasNext()) {
				this.sb.append(before).append(fn.apply(it.next()));
			}
		}
		return this;
	}

	public <T> SqlStringBuilder forEach(Iterator<T> it, String separator, Consumer<T> cons) {
		if(it.hasNext()) {
			cons.accept(it.next());
			while(it.hasNext()) {
				this.sb.append(separator);
				cons.accept(it.next());
			}
		} 
		return this;
	}
	
	public SqlStringBuilder append(String s) {
		sb.append(s);
		return this;
	}
	
	public int length(){
		return sb.length();
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
