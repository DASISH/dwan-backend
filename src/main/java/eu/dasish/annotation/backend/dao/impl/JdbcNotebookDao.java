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
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.NotebookIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
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
    public List<NotebookInfo> getNotebookInfos(UserIdentifier userID) {
        String sql = "SELECT notebook.title, notebook.external_id FROM notebook, principal where principal.principal_id = notebook.owner_id and principal.external_id = ?";
        return getSimpleJdbcTemplate().query(sql, notebookInfoRowMapper, userID.toString());
    }

    @Override
    public List<Notebook> getUsersNotebooks(UserIdentifier userID) {
        String sql = "SELECT notebook.* FROM notebook, principal where principal_id = owner_id and principal.external_id = ?";
        return getSimpleJdbcTemplate().query(sql, notebookRowMapper, userID.toString());
    }

    @Override
    public NotebookIdentifier addNotebook(UserIdentifier userID, NotebookIdentifier notebookId, String title) {
        try {
            String sql = "INSERT INTO notebook (external_id, title, owner_id) VALUES (:notebookId, :title, (SELECT principal_id FROM principal WHERE principal.external_id = :userID))";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("notebookId", notebookId.toString());
            params.put("userID", userID.toString());
            params.put("title", title);
            final int updatedRowCount = getSimpleJdbcTemplate().update(sql, params);
            return notebookId;
        } catch (DataAccessException exception) {
            throw exception;
        }
    }
    private final RowMapper<NotebookInfo> notebookInfoRowMapper = new RowMapper<NotebookInfo>() {
        @Override
        public NotebookInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            NotebookInfo notebookInfo = new NotebookInfo();
            notebookInfo.setRef(rs.getString("external_id")); // todo: what is ref? should it be the external id?
            notebookInfo.setTitle(rs.getString("title"));
//            notebookInfo.setRef(rs.getString("URI"));
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
//            notebook.setURI(rs.getString("URI_ID"));
            notebook.setAnnotations(new JdbcAnnotationDao().getAnnotations(rs.getInt("notebook_id")));
            return notebook;
        }
    };

    @Override
    public int deleteNotebook(NotebookIdentifier notebookId) {
        String sql = "DELETE FROM notebook where external_id = ?";
        // todo: also delete from the join table
        return getSimpleJdbcTemplate().update(sql, notebookId.toString());
    }

    @Override
    public int addAnnotation(NotebookIdentifier notebookId, AnnotationIdentifier annotationId) {
        try {
            //SimpleJdbcInsert notebookInsert = new SimpleJdbcInsert(getDataSource()).withTableName(notebookTableName).usingGeneratedKeyColumns(notebook_id);
            //Map<String, Object> params = new HashMap<String, Object>();
//            params.put("URI_ID", notebookUri.toString());
////            params.put("time_stamp", System.);
//            params.put("title", title);
//            params.put("owner_id", userID);

            //int rowsAffected = notebookInsert.execute(params);
//            txManager.commit(transaction);
            //return rowsAffected;
        } catch (DataAccessException exception) {
//            txManager.rollback(transaction);
            throw exception;
        }
    }
}
