package org.usf.jquery.web;

import static org.usf.jquery.core.Utils.isEmpty;

import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.usf.jquery.web.RequestQueryParam.RevisionMode;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author u$f
 *
 */
@Getter
@ToString(includeFieldNames = false)
public final class YearTableMetadata extends TableMetadata {
	
	static final YearMonth[] EMPTY_REVISION = new YearMonth[0];
	
	private final YearMonth[] revisions; //nullable

	YearTableMetadata(Map<ColumnDecorator, ColumnMetadata> columns, YearMonth[] revisions) {
		super(columns);
		this.revisions = revisions;
	}

	public YearMonth latestRevision() {
		return isEmpty(revisions) ? null : revisions[0];
	}
	
	public YearMonth[] filterExistingRevision(RevisionMode mode, YearMonth[] revs) {
		switch (mode) {//switch lambda in java14
		case STRICT  : return findStrictRevision(revs);
		case CLOSEST : return findClosestRevision(revs);
		default : throw new UnsupportedOperationException(mode.toString());
		}
	}
		
	private YearMonth[] findStrictRevision(YearMonth[] values) {
		return isEmpty(revisions) || isEmpty(values) 
				? EMPTY_REVISION 
				: Stream.of(values)
				.filter(v-> Stream.of(revisions).anyMatch(o-> o.equals(v)))
				.toArray(YearMonth[]::new);
	}
	
	private YearMonth[] findClosestRevision(YearMonth[] values) {
		if(isEmpty(revisions)) {
			return EMPTY_REVISION;
		}
		if(isEmpty(values)) {
			return new YearMonth[]{ latestRevision() };
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