package fr.enedis.teme.jquery.builder;

import java.util.Collection;
import java.util.LinkedList;

import fr.enedis.teme.jquery.CaseColumn;
import fr.enedis.teme.jquery.DBColumn;
import fr.enedis.teme.jquery.DBTable;
import fr.enedis.teme.jquery.ParameterHolder;
import fr.enedis.teme.jquery.WhenCase;
import fr.enedis.teme.jquery.WhenExpression;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WhenCaseBuilder implements WhenExpression {
	
	@NonNull
	private final DBColumn column;
	private final Collection<WhenExpression> whenCases = new LinkedList<>();

	WhenCaseBuilder append(WhenExpression we) {
		whenCases.add(we);
		return this;
	}

	public ColumnFilterBridge when() {
		return new ColumnFilterBridge(this, column);
	}

	@Override
	public CaseColumn orElseExp(Object def) {
		whenCases.add(new WhenCase(null, def));
		return build();
	}
	
	public CaseColumn build() {
		return new CaseColumn(whenCases.toArray(WhenExpression[]::new));
	}
	
	@Override
	public String sql(DBTable table, ParameterHolder arg) {
		throw new UnsupportedOperationException();
	}
}
