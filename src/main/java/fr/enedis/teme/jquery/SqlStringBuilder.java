package fr.enedis.teme.jquery;

import java.util.function.Supplier;

final class SqlStringBuilder {
	
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

	public SqlStringBuilder append(String s) {
		sb.append(s);
		return this;
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
}
