package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CaseExpressionBuilder<T> {
	
	private final List<Entry<String, T[]>> cases;
	private final Class<T> type;
	private String defaultValue;
	
	@SuppressWarnings("unchecked")
	public CaseExpressionBuilder<T> append(String title, T... values) {
		cases.add(entry(requireNonNull(title), requireNonEmpty(values)));
		return this;
	}
	
	public CaseExpressionBuilder<T> orElse(String orElse){
		this.defaultValue = orElse;
		return this;
	}
	
	public static CaseExpressionBuilder<Integer> when(String title, Integer... values) {
		return new CaseExpressionBuilder<>(new LinkedList<Entry<String, Integer[]>>(), Integer.class)
				.append(title, values);
	}
	
	public static CaseExpressionBuilder<String> when(String title, String... values) {
		return new CaseExpressionBuilder<>(new LinkedList<Entry<String, String[]>>(), String.class)
				.append(title, values);
	}
}