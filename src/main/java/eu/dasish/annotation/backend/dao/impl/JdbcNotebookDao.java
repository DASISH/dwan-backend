/*
 * Copyright (C) 2013 DASISH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.dasish.annotation.backend.dao.impl;

import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * Created on : Jun 14, 2013, 3:27:04 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class JdbcNotebookDao extends SimpleJdbcDaoSupport implements NotebookDao {

    @Autowired
//    private TransactionTemplate transactionTemplate;
    final static private String notebookTableName = "notebook";
    final static private String notebook_id = "notebook_id";

    public JdbcNotebookDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public List<NotebookInfo> getNotebookInfos(Number userID) {
        String sql = "SELECT title, \"URI\" FROM notebook where owner_id = ?";
        return getSimpleJdbcTemplate().query(sql, notebookInfoRowMapper, userID);
    }

    @Override
    public List<Notebook> getUsersNotebooks(Number userID) {
        String sql = "SELECT * FROM notebook where owner_id = ?";
        return getSimpleJdbcTemplate().query(sql, notebookRowMapper, userID);
    }

    @Override
    public Number addNotebook(Number userID, URI notebookUri, String title) {
//        TransactionStatus transaction = transactionTemplate.getTransactionManager().getTransaction(transactionTemplate)ransaction(txDefinition);
        try {
            SimpleJdbcInsert notebookInsert = new SimpleJdbcInsert(getDataSource()).withTableName(notebookTableName).usingGeneratedKeyColumns(notebook_id);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("URI", notebookUri.toString());
//            params.put("time_stamp", System.);
            params.put("title", title);
            params.put("owner_id", userID);

            Number id = notebookInsert.executeAndReturnKey(params);
//            txManager.commit(transaction);
            return id;
        } catch (DataAccessException exception) {
//            txManager.rollback(transaction);
            throw exception;
        }
    }
    private final RowMapper<NotebookInfo> notebookInfoRowMapper = new RowMapper<NotebookInfo>() {
        @Override
        public NotebookInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            NotebookInfo notebookInfo = new NotebookInfo();
//	    notebook.setId(rs.getInt("notebook_id"));
            notebookInfo.setTitle(rs.getString("title"));
            notebookInfo.setRef(rs.getString("URI"));
            return notebookInfo;
        }
    };
    private final RowMapper<Notebook> notebookRowMapper = new RowMapper<Notebook>() {
        @Override
        public Notebook mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Notebook notebook = new Notebook();
//	    notebook.setId(rs.getInt("notebook_id"));
            notebook.setTitle(rs.getString("title"));
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(rs.getTimestamp("time_stamp"));
            try {
                XMLGregorianCalendar gregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
                notebook.setTimeStamp(gregorianCalendar);
            } catch (DatatypeConfigurationException exception) {
                throw new SQLException(exception);
            }
            notebook.setURI(rs.getString("URI"));
            return notebook;
        }
    };
}
