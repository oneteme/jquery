package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.SqlStringBuilder.POINT_SEPARATOR;
import static fr.enedis.teme.jquery.Utils.isBlank;

import java.util.Collection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class TableAdapter implements DBTable {
	
	private final DBTable table;
	private final String suffix;
	
	@Override
	public String sql(String schema, QueryParameterBuilder ph) {
		
		return new SqlStringBuilder(20)
				.appendIf(!isBlank(schema), ()-> schema + POINT_SEPARATOR)
				.append(physicalName())
				.appendIf(!isBlank(suffix), ()-> "_" + suffix)
				.toString();
	}

	@Override
	public String physicalName() {
		return table.physicalName();
	}

	@Override
	public String physicalColumnName(TableColumn column) {
		return table.physicalColumnName(column);
	}

	@Override
	public TableColumn[] columns() {
		return table.columns();
	}
	
	@Override
	public Collection<ColumnTemplate> columnTemplates() {
		return table.columnTemplates();
	}
	
	@Override
	public TableAdapter suffix(String suffix) {
		return new TableAdapter(table, suffix);
	}

}
