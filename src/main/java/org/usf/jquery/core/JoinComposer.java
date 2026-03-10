package org.usf.jquery.core;

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class JoinComposer implements Composer<ViewJoin> {
	
	private final JoinType type;
	private final DBView view;
	private List<Criteria> criterias; //optional for cross join

	public JoinComposer(JoinType type, DBView view) {
		this.type = type;
		this.view = view;
	}
	
	public JoinComposer criterias(Criteria... criterias) {
		if(Objects.nonNull(criterias)) {
			this.criterias = new ArrayList<>(criterias.length);
		}
		Collections.addAll(this.criterias, criterias);
		return this;
	}
	
	@Override
	public ViewJoin compose() {
		return new ViewJoin(type, view, 
				nonNull(criterias) ? criterias.toArray(Criteria[]::new) : null);
	}
}
