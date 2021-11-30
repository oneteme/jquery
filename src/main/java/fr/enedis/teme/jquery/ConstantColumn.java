package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

public final class ConstantColumn<T> implements Column {
		
	private final T value;
	private final String name;
	
	public ConstantColumn(T value, String name) {
		this.value = value;
		this.name = requireNonNull(name);
	}

	@Override
	public String getColumnName(Table table) {
		return name;
	}

	@Override
	public String getMappedName() {
		return name;
	}

	@Override
	public String toSql(Table table) {
		
		return value instanceof Number 
				? value.toString()
				: "'" + value.toString() + "'";
	}

}
