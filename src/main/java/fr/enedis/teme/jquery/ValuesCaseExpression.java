package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isEmpty;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class ValuesCaseExpression<T> extends CaseExpression {
	
	private final CaseExpressionBuilder<T> cb;

	@Override
	protected String caseExpression(String columnName) {
		
		int nv = cb.getCases().stream().mapToInt(o-> o.getValue().length).max().orElseThrow();
		Function<T, String> fn = Number.class.isAssignableFrom(cb.getType())
				? Object::toString 
				: Utils::sqlString;
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
	
	public static <T> ValuesCaseExpression<T> values(CaseExpressionBuilder<T> choice) {
		return new ValuesCaseExpression<>(requireNonNull(choice));
	}
	
}
