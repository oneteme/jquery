package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.SqlStringBuilder.parenthese;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class DBWindow implements TaggableView {
	
	private static final String WIN_FUNCT = "rank()"; //make it variable rank, row_number, ..
	private static final String COL_NAME  = "row_rank";
	
	private final DBTable table;
	private final List<DBColumn> partitions = new LinkedList<>();
	private final List<DBOrder> orders = new LinkedList<>();
	
	@Override
	public String sql(QueryParameterBuilder builder) {
		var sb = new SqlStringBuilder(100);
		var qp = addWithValue(); //no alias
		var tn = table.sql(builder); //build tablename
		sb.append("SELECT ").append(tn).append(".*, ");
		sb.append(WIN_FUNCT).append(" OVER(");
		if(!partitions.isEmpty()) {
			sb.append("PARTITION BY ").appendEach(partitions, COMA, o-> o.sql(qp));
		}
		if(!orders.isEmpty()) { //require orders
			sb.append(" ORDER BY ").appendEach(orders, COMA, o-> o.sql(qp));
		}
		sb.append(") AS ").append(COL_NAME)
		.append(" FROM ").append(tn);
		return parenthese(sb.toString());
	}

	public DBWindow partitions(@NonNull DBColumn... columns) {
		Stream.of(columns).forEach(this.partitions::add);
		return this;
	}

	public DBWindow orders(@NonNull DBOrder... orders) {
		Stream.of(orders).forEach(this.orders::add);
		return this;
	}
	
	public DBFilter filter() {
		return b-> b.columnFullReference(tagname(), COL_NAME) + "=1";
	}

	@Override
	public String tagname() {
		return table.tagname();
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}

}
