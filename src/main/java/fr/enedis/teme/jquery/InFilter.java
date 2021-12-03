package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.nArgs;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public final class InFilter<T> implements DBFilter {

	private final DBColumn column;
	private final T[] values; //all types
	private final boolean invert;

	@SafeVarargs
	public InFilter(DBColumn column, boolean invert, T... values) {
		this.column = requireNonNull(column);
		this.values = requireNonEmpty(values);
		this.invert = invert;
	}
	
	@Override
	public String toSql(DBTable table) {
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
	
	@Override
	public Collection<Object> args() {
		return asList(values);
	}
	
	@Deprecated(forRemoval = true, since = "0.0.1") //TODO create new table
	public InFilter<String> asVarChar(){
		return new InFilter<>(column, invert, values == null ? null :
				Stream.of(values).map(Object::toString).toArray(String[]::new));
	}

}