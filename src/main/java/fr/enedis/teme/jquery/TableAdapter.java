package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isBlank;

import fr.enedis.teme.jquery.web.ColumnDescriptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class TableAdapter implements DBTable {
	
	private final DBTable table;
	private final String suffix;
	
	@Override
	public String sql(QueryParameterBuilder ph) {
		
		return new SqlStringBuilder(20)
				.append(dbName())
				.appendIf(!isBlank(suffix), ()-> "_" + suffix)
				.toString();
	}
	
	@Override
	public String dbColumnName(ColumnDescriptor desc) {
		return table.dbColumnName(desc);
	}

	@Override
	public String dbName() {
		return table.dbName();
	}
	
	@Override
	public TableAdapter suffix(String suffix) {
		return new TableAdapter(table, suffix);
	}

}
