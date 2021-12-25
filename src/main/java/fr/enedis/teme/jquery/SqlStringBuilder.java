package fr.enedis.teme.jquery;

import java.util.function.Supplier;

import lombok.NonNull;

final class SqlStringBuilder {
	
	private final StringBuilder sb;
	
	public SqlStringBuilder() {
		this.sb = new StringBuilder(50);
	}

	public SqlStringBuilder(int capacity) {
		this.sb = new StringBuilder(capacity);
	}
	
	public SqlStringBuilder(String v) { //buffer++
		this.sb = new StringBuilder(v.length() + 50).append(v);
	}

	public SqlStringBuilder append(String s) {
		sb.append(s);
		return this;
	}
	
	public SqlStringBuilder appendIf(boolean condition, String s) {
		return condition ? append(s) : this;
	}
	
	public SqlStringBuilder appendIf(boolean condition, String s, String orElse) {
		return append(condition ? s : orElse);
	}

	public SqlStringBuilder appendIf(boolean condition, Supplier<String> sup) {
		return condition ? append(sup.get()) : this;
	}

	public SqlStringBuilder appendIf(boolean condition, Supplier<String> sup, Supplier<String> orSup) {
		return append(condition ? sup.get() : orSup.get());
	}
	
	public static String toSqlString(Object o) {
		if(o == null) {
			return "null";
		}
		return o instanceof Number || o.getClass().isPrimitive()
				? o.toString() 
				: constantString(o);
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
	public static String constantString(@NonNull Object o) {
		return "'" + o + "'";
	}
	
}
