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
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * Created on : Jun 14, 2013, 3:27:04 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */

// TODO: not updated fully yet. 

public class JdbcNotebookDao extends JdbcResourceDao implements NotebookDao {

//    @Autowired
//    private AnnotationDao jdbcAnnotationDao;

    public JdbcNotebookDao(DataSource dataSource) {
        setDataSource(dataSource);        
        internalIdName = notebook_id;
        resourceTableName = notebookTableName;
    }

    ////////////////////////////////////////////////
    @Override
    public List<NotebookInfo> getNotebookInfos(UUID  userID) {
        String sql = "SELECT " + notebookTitle + ", " + notebookExternal_id + " FROM " + notebookTableName + ", " + principalTableName + " where " + principalPrincipal_id + " = " + notebookOwner_id + " and " + principalExternal_id + " = ?";
        return getSimpleJdbcTemplate().query(sql, notebookInfoRowMapper, userID.toString());
    }
    
    private final RowMapper<NotebookInfo> notebookInfoRowMapper = new RowMapper<NotebookInfo>() {
        @Override
        public NotebookInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            NotebookInfo notebookInfo = new NotebookInfo();
            notebookInfo.setRef(externalIDtoURI(rs.getString(external_id))); 
            notebookInfo.setTitle(rs.getString(title));
            return notebookInfo;
        }
    };
    
    ////////////////////////////////////////////////

    @Override
    public List<Notebook> getUsersNotebooks(UUID userID) {
        String sql = "SELECT " + notebookStar + " FROM " + notebookTableName + ", " + principalTableName + " where " + principal_id + " = " + notebookOwner_id + " and " + principalExternal_id + " = ?";
        return getSimpleJdbcTemplate().query(sql, notebookRowMapper, userID.toString());
    }
    
       private final RowMapper<Notebook> notebookRowMapper = new RowMapper<Notebook>() {
        @Override
        public Notebook mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Notebook notebook = new Notebook();
//	    notebook.setId(rs.getInt("notebook_id"));
            notebook.setTitle(rs.getString(title));
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(rs.getTimestamp(last_modified));
            try {
                XMLGregorianCalendar gregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
                notebook.setLastModified(gregorianCalendar);
            } catch (DatatypeConfigurationException exception) {
                throw new SQLException(exception);
            }
            notebook.setURI(externalIDtoURI(rs.getString("external_id")));
            notebook.setAnnotations(getAnnotations(rs.getInt(notebook_id)));
            return notebook;
        }
    };

    @Override
    public UUID addNotebook(UUID userID, String title) {
        try {
            final UUID externalIdentifier = UUID.randomUUID();
            String sql = "INSERT INTO " + notebookTableName + " (" + external_id + ", " + this.title + "," + notebookOwner_id + ") VALUES (:notebookId, :title, (SELECT " + principal_id + " FROM " + principalTableName + " WHERE " + principalExternal_id + " = :userID))";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("notebookId", externalIdentifier.toString());
            params.put("userID", userID.toString());
            params.put("title", title);
            final int updatedRowCount = getSimpleJdbcTemplate().update(sql, params);
            return externalIdentifier;
        } catch (DataAccessException exception) {
            throw exception;
        }
    }
    
 

    // returns the number of affected annotations
    @Override
    public int deleteNotebook(UUID notebookId) {
        String sql1 = "DELETE FROM " + notebooksAnnotationsTableName + " where " + notebook_id + "= (SELECT " + notebook_id + " FROM " + notebookTableName + " WHERE " + external_id + " = ?)";
        String sql2 = "DELETE FROM notebook where external_id = ?";
        int affectedAnnotations = getSimpleJdbcTemplate().update(sql1, notebookId.toString());
        int affectedNotebooks = getSimpleJdbcTemplate().update(sql2, notebookId.toString());
        return affectedAnnotations;
    }

    @Override
    public int addAnnotation(UUID notebookId, UUID annotationId) {
        try {
            SimpleJdbcInsert notebookInsert = new SimpleJdbcInsert(getDataSource()).withTableName(notebooksAnnotationsTableName);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(notebook_id, notebookId);
            params.put(annotation_id, annotationId);
            int rowsAffected = notebookInsert.execute(params);
            return rowsAffected;
        } catch (DataAccessException exception) {
            throw exception;
        }
    }

    ////////////////////////////////////////////////////////////////////////
    /**
     *
     * @param notebookID
     * @return the list of annotation-ids belonging to the notebook with
     * notebookId returns null if notebookId is null or is not in the DB TODO:
     * do we need to return null here? using an additional check.
     */
    @Override
    public List<Number> getAnnotationIDs(Number notebookID) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
        sql.append(notebooksAnnotationsTableNameAnnotation_id).append("  FROM ").append(notebooksAnnotationsTableName).append(" where ").append(notebook_id).append(" = ?");
        return getSimpleJdbcTemplate().query(sql.toString(), annotationIDRowMapper, notebookID);
    }
  
    //////////////////////////////////////////////////
    /**
     *
     * @param notebookID
     * @return the list of annotation-infos of the annotations from notebookID;
     * if notebook not in the DB or null returns null if the notebook contains
     * no annotations returns an empty list
     *
     *
     * @Override public List<AnnotationInfo> getAnnotationInfosOfNotebook(Number
     * notebookID) { return
     * jdbcAnnotationDao.getAnnotationInfos(getAnnotationIDs(notebookID)); }
     */
    //////////////////////////////////////////////
    /**
     *
     * @param notebookID
     * @return the list of annotation References from the notebookID returns
     * null if notebookID == null or it does not exists in the DB
     */
    @Override
    public List<String> getAnnotationREFsOfNotebook(Number notebookID) {
        return null; //jdbcAnnotationDao.getAnnotationREFs(getAnnotationIDs(notebookID));
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     *
     * @param notebookID
     * @return the Annotations (as a list of references) from the notebookID *
     * returns null if notebookID == null, or it does not exists in th DB, or
     * the list of annotations is empty, or something wrong happened when
     * extracting annotations from the notebook (according to dasish.xsd if an
     * Annotation is created then its list of annotations must contain at least
     * one element!)
     *
     */
    @Override
    public ReferenceList getAnnotations(Number notebookID) {
        if (notebookID == null) {
            return null;
        }
        ReferenceList result = new ReferenceList();
        result.getRef().addAll(getAnnotationREFsOfNotebook(notebookID));
        return result;

    }

    ///////////////////////////////////////////////////
    // REUSES notebookInfoRowMapper
    @Override
    public NotebookInfo getNotebookInfo(Number notebookID) {
        if (notebookID == null) {
            return null;
        }
        String sql = "SELECT  " + notebookExternal_id + "," + notebookTitle + " FROM " + notebookTableName + " where " + notebook_id + " = ? LIMIT 1";
        List<NotebookInfo> result = getSimpleJdbcTemplate().query(sql, notebookInfoRowMapper, notebookID.toString());
        return (!result.isEmpty() ? result.get(0) : null);
    }

    
    //////////////////////////////////////////////////////////////////
    @Override
    public List<UUID> getAnnotationExternalIDs(UUID notebookId) {
        List<Number> internalIds = getAnnotationIDs(getInternalID(notebookId));
        if (internalIds == null) {
            return null;
        }
        List<UUID> annotationIds = new ArrayList<UUID>();
//        for (Number internalId : internalIds) {
//            annotationIds.add(jdbcAnnotationDao.getExternalID(internalId));
//        }
        return annotationIds;
    }

    ////////////////////////////////////////////////////////////
    @Override
    public int removeAnnotation(Number annotationID) {
        String sqlNotebooks = "DELETE FROM " + notebooksAnnotationsTableName + " where " + annotation_id + " = ?";
        int affectedNotebooks = getSimpleJdbcTemplate().update(sqlNotebooks, annotationID);
        return affectedNotebooks;
    }
}