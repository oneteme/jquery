package org.usf.jquery.core;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResultMapper<T> {

    T map(ResultSet rs, String[] columnNames);

}