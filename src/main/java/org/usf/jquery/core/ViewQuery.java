package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
//@EqualsAndHashCode(callSuper = true) //doesn't works
public class ViewQuery extends DBTable {  //! important extends DBTable

	private final RequestQueryBuilder query; //named operation column
	
	public ViewQuery(String tag, @NonNull TaggableColumn... columns) {
		super(null, tag);
		this.query = new RequestQueryBuilder().columns(columns);
	}

	@Override
	public String sql(QueryParameterBuilder builder) {
		var s = new SqlStringBuilder(100).append("(");
		query.build(s, builder.subQuery());
		return s.append(")").toString();
	}
	
	public ViewQuery columns(TaggableColumn... columns) {
		query.columns(columns);
		return this;
	}

	public ViewQuery filters(DBFilter... filters){
		query.filters(filters);
		return this;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue()); 
	}
}
