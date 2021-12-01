package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

import lombok.Getter;

@Getter
public final class ConstantColumn<T> implements Column {
		
	private final T value;
	private final String mappedName;
	
	public ConstantColumn(T value, String mappedName) {
		this.value = value;
		this.mappedName = requireNonNull(mappedName);
	}
	
	@Override
	public String toSql(Table table) {
		
		return value instanceof Number 
				? value.toString()
				: "'" + value.toString() + "'";
	}

}
