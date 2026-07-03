package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.OperatorKind.AGGREGATE;
import static org.usf.jquery.core.OperatorKind.WINDOW;
import static org.usf.jquery.core.QueryAnalyzer.IGNORE_GROUPS;
import static org.usf.jquery.core.QueryAnalyzer.Stage.CRITERIA;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class OperationColumn implements Column {

	private final String name;
	private final OperatorKind kind;
	private final Invocable operator;
	private final List<Object> args; //optional
	private final JDBCType type; //optional
	private ViewColumn overColumn;

	@Override
	public int prepare(QueryAnalyzer analyzer) {
		if(kind == AGGREGATE || kind == WINDOW) {
			var res = MEASURE;
			if(!isEmpty(args) && analyzer.with(IGNORE_GROUPS).tryAnalyzeNested(args) == MEASURE) {
				res++; //nested measure
			}
			return res;
		}
		if("OVER".equals(name)) {
			if(analyzer.getStage() == CRITERIA) { //dialect.support window filter !?
				var col = new OperationColumn(name, kind, operator, args, type).as(name + '_' + hashCode());
				var sub = new QueryComposer().columns(Column.allColumns(), col).compose(analyzer.getStore());
				this.overColumn = sub.column(col.getTag(), col.getType());
				analyzer.cte(sub, true);
				return overColumn.prepare(analyzer);
			}
			return resolveOverColumns(analyzer);
		}
		return analyzer.tryAnalyzeNested(args, this);
	}

	@Override
	public void build(SqlBuilder builder) {
		if(nonNull(overColumn)) {
			builder.append(overColumn); //no args
		}
		else {
			operator.build(builder, args.toArray());
		}
	}
	
	@Override
	public JDBCType getType() {
		return nonNull(overColumn) ? overColumn.getType() : type;
	}
	
	private int resolveOverColumns(QueryAnalyzer analyzer) {
		var lvl = analyzer.tryAnalyzeNested(args.get(0))-1; //nested aggregate function
		return args.size() == 1
				? lvl
				: Math.max(lvl, analyzer.tryAnalyzeNested(args.get(1))); //partition
	}
		
	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
}
