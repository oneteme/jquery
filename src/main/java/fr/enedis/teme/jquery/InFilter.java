package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Utils.nArgs;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public final class InFilter<T> implements Filter {

	private final Column column;
	private final T[] values; //all types
	private final boolean invert;

	@SafeVarargs
	public InFilter(Column column, boolean invert, T... values) {
		this.column = requireNonNull(column);
		this.invert = invert;
		this.values = values;
	}
	
	@Override
	public String toSql(Table table) {
		if(isEmpty(values)) {
			return "";
		}
		String inValues;
		if(values.length == 1) {
			inValues = (invert ? "<>" : "=") + "?";
		}
		else {
			inValues = " IN" + nArgs(values.length);
			if(invert) {
				inValues = " NOT" + inValues;
			}
		}
		return column.toSql(table) + inValues;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<Object> args() {
		return ofNullable(values)
				.map(Arrays::asList)
				.map(Collection.class::cast)
				.orElseGet(Collections::emptyList);
	}
	
	@Deprecated(forRemoval = true, since = "0.0.1") //TODO create new table
	public InFilter<String> asVarChar(){
		return new InFilter<>(column, invert, values == null ? null :
				Stream.of(values).map(Object::toString).toArray(String[]::new));
	}

}