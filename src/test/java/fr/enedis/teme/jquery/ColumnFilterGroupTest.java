package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;
import static fr.enedis.teme.jquery.ParameterHolder.parametrized;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

class ColumnFilterGroupTest implements DataProvider {

	private final ParameterHolder VAL = addWithValue();
	private final ParameterHolder PRM = parametrized();
	private final LocalDate date = LocalDate.of(2020,1,1);
	
	@Test
	void testSql() {
	}

	void testToString() {
		
	}
	
	static Stream<Arguments> randomCaseProvider() {
		var list = DataProvider.operationCaseProvider().collect(toList());
//		IntStream.range(0, 3).mapToObj(n->{
//			shuffle(list);
//			return Arguments.of(list.su)
//		});
		return null;
	}

}
