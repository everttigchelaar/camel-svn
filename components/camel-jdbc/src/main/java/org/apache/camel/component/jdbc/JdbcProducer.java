/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.IntrospectionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 
 */
public class JdbcProducer extends DefaultProducer {
    private static final transient Logger LOG = LoggerFactory.getLogger(JdbcProducer.class);
    private DataSource dataSource;
    private int readSize;
    private Map<String, Object> parameters;

    public JdbcProducer(JdbcEndpoint endpoint, DataSource dataSource, int readSize, Map<String, Object> parameters) throws Exception {
        super(endpoint);
        this.dataSource = dataSource;
        this.readSize = readSize;
        this.parameters = parameters;
    }

    @Override
    public JdbcEndpoint getEndpoint() {
        return (JdbcEndpoint) super.getEndpoint();
    }

    /**
     * Execute sql of exchange and set results on output
     */
    public void process(Exchange exchange) throws Exception {
        String sql = exchange.getIn().getBody(String.class);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();

            if (parameters != null && !parameters.isEmpty()) {
                IntrospectionSupport.setProperties(stmt, parameters);
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing JDBC statement: " + sql);
            }

            if (stmt.execute(sql)) {
                rs = stmt.getResultSet();
                setResultSet(exchange, rs);
            } else {
                int updateCount = stmt.getUpdateCount();
                exchange.getOut().setHeader(JdbcConstants.JDBC_UPDATE_COUNT, updateCount);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOG.warn("Error closing JDBC resource: " + e, e);
            }
        }

        // populate headers
        exchange.getOut().getHeaders().putAll(exchange.getIn().getHeaders());
    }

    /**
     * Sets the result from the ResultSet to the Exchange as its OUT body.
     */
    protected void setResultSet(Exchange exchange, ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();

        // should we use jdbc4 or jdbc3 semantics
        boolean jdbc4 = getEndpoint().isUseJDBC4ColumnNameAndLabelSemantics();

        int count = meta.getColumnCount();
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        int rowNumber = 0;
        while (rs.next() && (readSize == 0 || rowNumber < readSize)) {
            Map<String, Object> row = new HashMap<String, Object>();
            for (int i = 0; i < count; i++) {
                int columnNumber = i + 1;
                // use column label to get the name as it also handled SQL SELECT aliases
                String columnName;
                if (jdbc4) {
                    // jdbc 4 should use label to get the name
                    columnName = meta.getColumnLabel(columnNumber);
                } else {
                    // jdbc 3 uses the label or name to get the name
                    try {
                        columnName = meta.getColumnLabel(columnNumber);
                    } catch (SQLException e) {
                        columnName = meta.getColumnName(columnNumber);
                    }
                }
                // use index based which should be faster
                row.put(columnName, rs.getObject(columnNumber));
            }
            data.add(row);
            rowNumber++;
        }
        exchange.getOut().setHeader(JdbcConstants.JDBC_ROW_COUNT, rowNumber);
        exchange.getOut().setBody(data);
    }

}
