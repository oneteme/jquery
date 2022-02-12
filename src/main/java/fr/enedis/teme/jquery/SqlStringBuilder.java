package fr.enedis.teme.jquery;

import static java.util.function.Function.identity;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class SqlStringBuilder {
	
	static final String EMPTY_STRING = "";
	static final String COMA_SEPARATOR = ", ";
	static final String SPACE_SEPARATOR = " ";
	static final String POINT_SEPARATOR = ".";
	private final StringBuilder sb;

	public SqlStringBuilder(int capacity) {
		this.sb = new StringBuilder(capacity);
	}
	
	public SqlStringBuilder(String v) { //buffer++
		this.sb = new StringBuilder(v.length() + 50).append(v);
	}
	
	public SqlStringBuilder appendIf(boolean condition, Supplier<String> sup) {
		return condition ? append(sup.get()) : this;
	}

	public SqlStringBuilder appendIf(boolean condition, Supplier<String> sup, Supplier<String> orSup) {
		return append(condition ? sup.get() : orSup.get());
	}

	public SqlStringBuilder appendEach(Collection<String> list, String separator) {
		return appendEach(list, separator, EMPTY_STRING, identity());
	}

	public <T> SqlStringBuilder appendEach(Collection<T> list, String separator, Function<T, String> fn) {
		return appendEach(list, separator, EMPTY_STRING, fn);
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

	public <T> SqlStringBuilder forEach(Collection<T> list, String separator, Consumer<T> cons) {
		if(!list.isEmpty()) {
			var it = list.iterator();
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
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
}
