package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.sqlString;
import static fr.enedis.teme.jquery.Validation.requireNonBlank;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class CaseExpression implements DBExpression {
	
	protected abstract String caseExpression(String columnName);

	@Override
	public final String toSql(String columnName) {
		return "CASE " + caseExpression(requireNonBlank(columnName)) + " END";
	}

	@Override
	public String getFunctionName() {
		return "case";
	}
	
	protected static String whenThen(String expression, String label) {
		return "WHEN " + expression + " THEN " + sqlString(label);
	}
}
