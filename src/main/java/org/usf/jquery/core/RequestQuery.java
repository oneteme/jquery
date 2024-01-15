package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static org.usf.jquery.core.Utils.isEmpty;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public final class RequestQuery {
	
	@NonNull
	private final String query;
	private final Object[] args;
	private final int[] argTypes;
	
	public List<DynamicModel> execute(DataSource ds) {
		return execute(ds, new KeyValueMapper());
	}
	
	public <T> T execute(DataSource ds, ResultSetMapper<T> mapper) { // overload with sql types
		try(var cn = ds.getConnection()){
			log.debug("preparing statement : {}", query);
			try(var ps = cn.prepareStatement(query)){
				if(!isEmpty(args)) {
					for(var i=0; i<args.length; i++) {
						if(isNull(args[i])) {
							ps.setNull(i+1, argTypes[i]);
						}
						else {
							ps.setObject(i+1, args[i], argTypes[i]);
						}
					}						
				}
		        log.debug("with parameters : {}", Arrays.toString(args));
		        log.debug("executing SQL query...");
		        var bg = currentTimeMillis();
				try(var rs = ps.executeQuery()){
			        log.debug("query executed in {} ms", currentTimeMillis() - bg);
					return mapper.map(rs);
				}
			}
		}
		catch(SQLException e) { // re-throw SQLException
			throw new MappingException("error while mapping results", e);
		}
	}
}
