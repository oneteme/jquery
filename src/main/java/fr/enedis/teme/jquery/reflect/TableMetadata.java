package fr.enedis.teme.jquery.reflect;

import java.util.Arrays;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor 
class TableMetadata {
	
	private final int[] revisions;
	private final Map<String, ColumnMetadata> columns;
	
	@Override
	public String toString() {
		return "{revisions:" + Arrays.toString(revisions) + ", columns:" + columns + "}";
	}

}