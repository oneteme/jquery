package fr.enedis.teme.jquery.web;

import static java.util.Arrays.binarySearch;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor 
final class TableMetadata {
	
	private final int[] revisions; //nullable, sorted
	private final Integer currentRevision; //nullable
	private final Map<String, ColumnMetadata> columns;

	public TableMetadata(Map<String, ColumnMetadata> columns) {
		this.columns = columns;
		this.revisions = null;
		this.currentRevision = null;
	}

	public boolean exists(int year) {
		return binarySearch(revisions, year) > -1;
	}
	
	public YearMonth currentRevision() {
		return revisions != null && currentRevision != null 
				? YearMonth.of(revisions[revisions.length - 1], currentRevision)
				: null;
	}
	
	@Override
	public String toString() {
		return "{revisions:" + Arrays.toString(revisions) + ", columns:" + columns + "}";
	}
	
}