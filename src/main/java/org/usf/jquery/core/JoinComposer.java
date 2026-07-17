package org.usf.jquery.core;

import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Collection;

import lombok.NonNull;

/**
 * 
 * @author u$f
 * 
 */
public class JoinComposer implements Composer<Join> {
	
	private final JoinType type;
	private final View view;
	private Collection<Criteria> criterias; //optional for cross join

	public JoinComposer(JoinType type, View view) {
		this.type = type;
		this.view = view;
	}

	public JoinComposer criteria(@NonNull Criteria criteria){
		getCriterias().add(criteria);
		return this;
	}

	public JoinComposer criterias(@NonNull Criteria... criterias){
		addAll(getCriterias(), criterias);
		return this;
	}
	
	public JoinComposer criterias(@NonNull Collection<Criteria> criterias){
		getCriterias().addAll(criterias);
		return this;
	}
	
	private Collection<Criteria> getCriterias(){
		if(isNull(criterias)) {
			criterias = new ArrayList<>();
		}
		return criterias;
	}

	public Join compose() {
		return compose(null);
	}
	
	@Override
	public Join compose(Store store) {
		return new Join(type, view, 
				nonNull(criterias) ? unmodifiableCollection(criterias) : null);
	}
}
