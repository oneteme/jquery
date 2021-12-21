package fr.enedis.teme.jquery;

import java.time.YearMonth;
import java.util.function.Function;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class QueryParam {
	
	private final DBColumn[] columns;
	private final DBFilter[] filters;
	private final Function<DBTable, YearMonth[]> partitionFn;
	
}