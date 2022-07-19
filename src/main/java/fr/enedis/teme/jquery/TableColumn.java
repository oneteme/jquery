package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class TableColumn implements TaggableColumn {
	
	private final String dbName;
	private final String tagname;
	//add tablename

	@Override
	public String sql(QueryParameterBuilder arg) {
		return dbName;
	}

	@Override
	public String tagname() {
		return tagname;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}

}
