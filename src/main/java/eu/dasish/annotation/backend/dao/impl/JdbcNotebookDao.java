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

import eu.dasish.annotation.backend.dao.AnnotationDao;
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
public class JdbcNotebookDao extends JdbcResourceDao implements NotebookDao {

    @Autowired
    private AnnotationDao jdbcAnnotationDao;
    final static private String notebookTableName = "notebook";
    final static private String notebooksAnnotationsTableName = "notebooks_annotations";
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
    public NotebookIdentifier addNotebook(UserIdentifier userID, String title) {
        try {
            final NotebookIdentifier notebookIdentifier = new NotebookIdentifier();
            String sql = "INSERT INTO notebook (external_id, title, owner_id) VALUES (:notebookId, :title, (SELECT principal_id FROM principal WHERE principal.external_id = :userID))";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("notebookId", notebookIdentifier.getUUID().toString());
            params.put("userID", userID.toString());
            params.put("title", title);
            final int updatedRowCount = getSimpleJdbcTemplate().update(sql, params);
            return notebookIdentifier;
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
            notebook.setAnnotations(jdbcAnnotationDao.getAnnotations(rs.getInt("notebook_id")));
            return notebook;
        }
    };

    // returns the number of affected annotations
    @Override
    public int deleteNotebook(NotebookIdentifier notebookId) {
        String sql1 = "DELETE FROM " + notebooksAnnotationsTableName + " where notebook_id = (SELECT notebook_id FROM notebook WHERE external_id = ?)";
        String sql2 = "DELETE FROM notebook where external_id = ?";
        int affectedAnnotations = getSimpleJdbcTemplate().update(sql1, notebookId.getUUID().toString());
        int affectedNotebooks = getSimpleJdbcTemplate().update(sql2, notebookId.getUUID().toString());
        return affectedAnnotations;
    }

    @Override
    public int addAnnotation(NotebookIdentifier notebookId, AnnotationIdentifier annotationId) {
        try {
            SimpleJdbcInsert notebookInsert = new SimpleJdbcInsert(getDataSource()).withTableName(notebooksAnnotationsTableName);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("notebook_id", notebookId);
            params.put("annotation_id", annotationId);
            int rowsAffected = notebookInsert.execute(params);
            return rowsAffected;
        } catch (DataAccessException exception) {
            throw exception;
        }
    }
}
