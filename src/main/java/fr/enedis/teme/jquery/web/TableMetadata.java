package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.Utils.isEmpty;

import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import fr.enedis.teme.jquery.TableColumn;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString(includeFieldNames = false)
@RequiredArgsConstructor
public final class TableMetadata {
	
	static final YearMonth[] EMPTY_REVISION = new YearMonth[0];
	
	private final YearMonth[] revisions; //nullable
	private final Map<String, ColumnMetadata> columns;

	
	TableMetadata(Map<String, ColumnMetadata> columns) {
		this(null, columns);
	}

	public ColumnMetadata column(TableColumn c) {
		return columns.get(c.getTagname());
	}

	public YearMonth latestRevision() {
		return isEmpty(revisions) ? null : revisions[0];
	}
		
	public YearMonth[] findStrictRevision(YearMonth[] values) {
		return isEmpty(revisions) ? EMPTY_REVISION : Stream.of(values)
				.filter(v-> Stream.of(revisions).anyMatch(o-> o.equals(v)))
				.toArray(YearMonth[]::new);
	}
	
	public YearMonth[] findClosestRevision(YearMonth[] values) {
		if(isEmpty(revisions)) {
			return EMPTY_REVISION;
		}
		List<YearMonth> list = new LinkedList<>();
		for(var v : values) {
			Stream.of(revisions)
			.filter(o-> o.compareTo(v) <= 0)
			.findFirst()
			.ifPresent(list::add);
		}
		return list.isEmpty() ? EMPTY_REVISION : list.toArray(YearMonth[]::new);
	}
	
}