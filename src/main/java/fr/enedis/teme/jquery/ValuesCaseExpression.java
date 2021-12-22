package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isEmpty;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public final class ValuesCaseExpression<T> extends CaseExpressionColumn {
	
	private final CaseExpressionBuilder<T> cb;
	
	private ValuesCaseExpression(DBColumn column, CaseExpressionBuilder<T> choice) {
		super(column);
		this.cb = choice;
	}

	@Override
	protected String toSql(String columnName) {
		
		int nv = cb.getCases().stream().mapToInt(o-> o.getValue().length).max().orElseThrow();
		Function<T, String> fn = Number.class.isAssignableFrom(cb.getType())
				? Object::toString 
				: o-> "'" + o.toString() + "'";
		StringBuilder sb;
		if(nv == 1) {//one value
			sb = new StringBuilder(columnName + " ")
					.append(cb.getCases().stream()
						.map(c-> whenThen(fn.apply(c.getValue()[0]), c.getKey()))
						.collect(joining(" ")));
		}
		else {
			sb = new StringBuilder(cb.getCases().stream()
				.map(e-> whenThen(columnName + " IN(" + Stream.of(e.getValue()).map(fn).collect(joining(", ")) + ")", e.getKey()))
				.collect(joining(" ")));
		}
		if(!isEmpty(cb.getDefaultValue())) {
			sb.append(" ELSE " + cb.getDefaultValue());
		}
		return sb.toString();
	}
	
	public static <T> ValuesCaseExpression<T> values(DBColumn column, CaseExpressionBuilder<T> choice) {
		return new ValuesCaseExpression<>(requireNonNull(column), requireNonNull(choice));
	}
	
}
