package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.Utils.isEmpty;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.Map;

import fr.enedis.teme.jquery.DBTable;
import fr.enedis.teme.jquery.web.RequestQueryParam.RevisionMode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@RequiredArgsConstructor
public final class DatabaseMetadata {
	
	private final Map<String, TableMetadata> tables;
	
	public TableMetadata table(DBTable table) {
		return tables.get(table.dbName());
	}

	public YearMonth latestRevision(DBTable table) {
		var meta = tables.get(table.dbName());
		return meta == null ? null : meta.latestRevision();
	}
	
	public YearMonth[] filterExistingRevision(DBTable table, RevisionMode mode, YearMonth[] revs) {
		var tm = tables.get(table.dbName());
		if(tm == null) {
			log.error("table metadata not found : " + table.dbName());
			return revs;
		}
		YearMonth[] revisions;
		switch (mode) {//switch lambda in java14
		case STRICT  : revisions = tm.findStrictRevision(revs); break;
		case CLOSEST : revisions = tm.findClosestRevision(revs); break;
		default : throw new UnsupportedOperationException("Unsupported mode : " + mode);
		}
		if(isEmpty(revisions)) {
			log.warn("no revision found for : " + Arrays.toString(revs));
		}
		return revisions;
	}

}