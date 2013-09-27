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

import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.ResourceREF;
import java.lang.String;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import org.springframework.jdbc.core.RowMapper;

/**
 * Created on : Jun 27, 2013, 10:30:52 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class JdbcAnnotationDao extends JdbcResourceDao implements AnnotationDao {

   
    public JdbcAnnotationDao(DataSource dataSource) {
        setDataSource(dataSource);
        internalIdName = annotation_id;
        resourceTableName = annotationTableName;
        
    }
    
    
    ///////////// GETTERS /////////////
  
    @Override
    public List<Number> retrieveSourceIDs(Number annotationID) {
        if (annotationID == null ) {
            return null;
        }
        StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
        sql.append(source_id).append(" FROM ").append(annotationsSourcesTableName).append(" WHERE ").append(annotation_id).append("= ?");
        return getSimpleJdbcTemplate().query(sql.toString(), sourceIDRowMapper, annotationID);
    }
    
    ///////////////////////////////////////////////////////////////////
    @Override
    public List<Map<Number, String>> retrievePermissions(Number annotationId) {
        if (annotationId == null) {
            return null;
        }
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(principal_id).append(",").append(permission).append(" FROM ").append(permissionsTableName).append(" WHERE ").append(annotation_id).append("  = ?");
        return getSimpleJdbcTemplate().query(sql.toString(), principalsPermissionsRowMapper, annotationId.toString());
    }
    private final RowMapper<Map<Number, String>> principalsPermissionsRowMapper = new RowMapper<Map<Number, String>>() {
        @Override
        public Map<Number, String> mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Map<Number,String> result = new HashMap<Number, String>();
            result.put(rs.getInt(principal_id),rs.getString(permission));
            return result;
        }
    };
    
    ////////////////////////////////////////////////////////////////////////
    @Override
    public List<Number> getFilteredAnnotationIDs(List<Number> annotationIDs, String text, String access, String namespace, Number ownerID, Timestamp after, Timestamp before) {

        StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
        sql.append(annotation_id).append(" FROM ").append(annotationTableName).append(" WHERE TRUE ");
        Map<String, Object> params = new HashMap<String, Object>();

        if (annotationIDs == null) {
            return null;
        } else {
            if (annotationIDs.isEmpty()) {
                return new ArrayList<Number>();
            }
        }

        String values = makeListOfValues(annotationIDs);
        sql.append(" AND ").append(annotation_id).append(" IN ").append(values);


        if (ownerID != null) {
            sql.append(" AND ").append(owner_id).append(" = :owner ");
            params.put("owner", ownerID);
        }

        if (after != null) {
            sql.append(" AND ").append(time_stamp).append("  > :after");
            params.put("after", after);
        }

        if (before != null) {
            sql.append(" AND ").append(time_stamp).append("  < :before");
            params.put("before", before);
        }

        if (text != null) {
            sql.append(" AND ").append(body_text).append("  LIKE '%").append(text).append("%'");
        }

        return getSimpleJdbcTemplate().query(sql.toString(), internalIDRowMapper, params);
    }

    //////////////////////////////
    @Override
    public List<Number> retrieveAnnotationList(List<Number> sourceIDs) {
        if (sourceIDs == null) {
            return null;
        }
        if (sourceIDs.isEmpty()) {
            return new ArrayList<Number>();
        }
        String values = makeListOfValues(sourceIDs);
        StringBuilder query = new StringBuilder("SELECT DISTINCT ");
        query.append(annotation_id).append(" FROM ").append(annotationsSourcesTableName).append(" WHERE ").append(source_id).append(" IN ");
        query.append(values);
        return getSimpleJdbcTemplate().query(query.toString(), internalIDRowMapper);
    }
    
    //////////////////////////////////////////////////////////////////////////

    @Override
    public List<AnnotationInfo> getAnnotationInfos(List<Number> annotationIDs) {

        if (annotationIDs == null) {
            return null;
        }

        if (annotationIDs.isEmpty()) {
            return (new ArrayList<AnnotationInfo>());
        }

        String values = makeListOfValues(annotationIDs);
        StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
        sql.append(annotationStar).append(" FROM ").append(annotationTableName ).append(" WHERE ").append(annotationAnnotation_id).append("  IN ").append(values);
        return getSimpleJdbcTemplate().query(sql.toString(), annotationInfoRowMapper);
    }
    private final RowMapper<AnnotationInfo> annotationInfoRowMapper = new RowMapper<AnnotationInfo>() {
        @Override
        public AnnotationInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            AnnotationInfo annotationInfo = new AnnotationInfo();
            annotationInfo.setRef(externalIDtoURI(rs.getString(external_id)));
            annotationInfo.setOwner(getResourceREF(Integer.toString(rs.getInt(owner_id))));
            annotationInfo.setHeadline(rs.getString(headline));
            return annotationInfo;
        }
    };

    /////////////////////////////////////////////////
    /**
     *
     * @param annotationIDs
     * @return list of annotation references corresponding to the annotation-ids
     * from the input list if the input list is null or empty (zero elements)
     * returns an empty list there may be annotationIDs which are not in the DB
     * (so that's why we need this method).
     */
    @Override
    public List<ResourceREF> getAnnotationREFs(List<Number> annotationIDs) {
        if (annotationIDs == null) {
            return null;
        }
        if (annotationIDs.isEmpty()) {
            return (new ArrayList<ResourceREF>());
        }

        String values = makeListOfValues(annotationIDs);
        StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
        sql.append(external_id).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotationAnnotation_id).append("  IN ").append(values);
        return getSimpleJdbcTemplate().query(sql.toString(), annotationREFRowMapper);
    }
    private final RowMapper<ResourceREF> annotationREFRowMapper = new RowMapper<ResourceREF>() {
        @Override
        public ResourceREF mapRow(ResultSet rs, int rowNumber) throws SQLException {
            ResourceREF annotationREF = new ResourceREF();
            annotationREF.setRef(externalIDtoURI(rs.getString(external_id)));
            return annotationREF;
        }
    };

    //////////////////////////////////////////////////////////////////////////
    @Override    
    public Annotation getAnnotationWithoutSourcesAndPermissions(Number annotationID) throws SQLException {
        if (annotationID == null) {
            return null;        }
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(annotationStar).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotationAnnotation_id).append("= ? LIMIT  1");
        List<Annotation> respond = getSimpleJdbcTemplate().query(sql.toString(), annotationRowMapper, annotationID);
        return (respond.isEmpty() ? null : respond.get(0));
    }
    
    private final RowMapper<Annotation> annotationRowMapper = new RowMapper<Annotation>() {
        @Override
        public Annotation mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Annotation annotation = new Annotation();

            ResourceREF ownerREF = new ResourceREF();
            ownerREF.setRef(String.valueOf(rs.getInt(owner_id)));
            annotation.setOwner(ownerREF);

            annotation.setHeadline(rs.getString(headline));
            AnnotationBody body = new AnnotationBody();
            body.setMimeType(rs.getString(body_mimetype));
            body.setValue(rs.getString(body_text));
            annotation.setBody(body);
            
            annotation.setTargetSources(null);
            
            annotation.setURI(externalIDtoURI(rs.getString(external_id)));

            try {
                annotation.setTimeStamp(Helpers.setXMLGregorianCalendar(rs.getTimestamp(time_stamp)));
                return annotation;
            } catch (DatatypeConfigurationException e) {
                System.out.println(e);
                return annotation; // no date-time is set 
            }
        }
    };
    
   /////////////////////////////
    @Override
    public boolean annotationIsInUse(Number annotationID) {
        StringBuilder sqlNotebooks = new StringBuilder("SELECT ");
        sqlNotebooks.append(notebook_id).append(" FROM ").append(notebooksAnnotationsTableName).append(" WHERE ").append(annotation_id).append("= ? LIMIT 1");
        List<Number> resultNotebooks = getSimpleJdbcTemplate().query(sqlNotebooks.toString(), notebookIDRowMapper, annotationID);
        if (resultNotebooks.size() > 0) {
            return true;
        }
        
        StringBuilder sqlSources = new StringBuilder("SELECT ");
        sqlSources.append(source_id).append(" FROM ").append(annotationsSourcesTableName).append(" WHERE ").append(annotation_id).append("= ? LIMIT 1");
        List<Number> resultSources = getSimpleJdbcTemplate().query(sqlSources.toString(), sourceIDRowMapper, annotationID);
        if (resultSources.size() > 0) {
            return true;
        }
        
        StringBuilder sqlPermissions = new StringBuilder("SELECT ");
        sqlPermissions.append(principal_id).append(" FROM ").append(permissionsTableName).append(" WHERE ").append(annotation_id).append("= ? LIMIT 1");
        List<Number> resultPermissions = getSimpleJdbcTemplate().query(sqlPermissions.toString(), principalIDRowMapper, annotationID);
        return (resultPermissions.size() > 0);
    }
    
    //////////// UPDATERS /////////////
    
    
    @Override
    public int updateBodyText(Number annotationID, String newBodyText) {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(annotationTableName).append(" SET ").append(body_text).append("= '").append(newBodyText).append("' WHERE ").append(annotation_id).append("= ?");
        return getSimpleJdbcTemplate().update(sql.toString(), annotationID);
    }

    @Override
    public int updateBodyMimeType(Number annotationID, String newMimeType){
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(annotationTableName).append(" SET ").append(body_mimetype).append("= '").append(newMimeType).append("' WHERE ").append(annotation_id).append("= ?");
        return getSimpleJdbcTemplate().update(sql.toString(), annotationID);
    }
    
  
    // TODO Unit test
    @Override
    public Number updateAnnotation(Annotation annotation, Number ownerID) throws SQLException {
       
        String externalID = stringURItoExternalID(annotation.getURI());
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(annotationTableName).append(" SET ").
                append(body_text).append("= '").append(annotation.getBody().getValue()).append("',").
                append(body_mimetype).append("= '").append(annotation.getBody().getMimeType()).append("',").
                append(headline).append("= '").append(annotation.getHeadline()).append("',").
                append(owner_id).append("= '").append(annotation.getOwner()).append("',").
                append(time_stamp).append("= '").append(annotation.getTimeStamp().toString()).
                append("' WHERE ").append(external_id).append("= ?");
        int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), externalID);
        return ((affectedRows > 0) ? getInternalID(UUID.fromString(externalID)) : null);
    }
    
    
    //////////// ADDERS ////////////////////////
    
   
    @Override
    public Number addAnnotation(Annotation annotation, Number ownerID) throws SQLException {
        // generate a new annotation ID 
        UUID externalID = UUID.randomUUID();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalID.toString());
        params.put("ownerId", ownerID);
        params.put("headline", annotation.getHeadline());       
        params.put("bodyText", annotation.getBody().getValue());
        params.put("bodyMimeType", annotation.getBody().getMimeType());
        
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(annotationTableName).append("(").append(external_id).append(",").append(owner_id);
        sql.append(",").append(headline).append(",").append(body_text).append(",").append(body_mimetype).append(" ) VALUES (:externalId, :ownerId, :headline, :bodyText, :bodyMimeType)");
        int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), params);
        return ((affectedRows > 0) ? getInternalID(externalID) : null);
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////
    @Override
    public int addAnnotationSource(Number annotationID, Number sourceID) throws SQLException {
        Map<String, Object> paramsAnnotationsSources = new HashMap<String, Object>();
        paramsAnnotationsSources.put("annotationId", annotationID);
        paramsAnnotationsSources.put("sourceId", sourceID);
        StringBuilder  sqlAnnotationsSources = new StringBuilder("INSERT INTO ");
        sqlAnnotationsSources.append(annotationsSourcesTableName ).append("(").append(annotation_id).append(",").append(source_id).append(" ) VALUES (:annotationId, :sourceId)");
        return getSimpleJdbcTemplate().update(sqlAnnotationsSources.toString(), paramsAnnotationsSources);
    }
    
    
         /////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int addAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission) throws SQLException {
        Map<String, Object> paramsPermissions = new HashMap<String, Object>();
        paramsPermissions.put("annotationId", annotationID);
        paramsPermissions.put("principalId", userID);
        paramsPermissions.put("status", permission.value());
        StringBuilder sqlUpdatePermissionTable = new StringBuilder("INSERT INTO ");
        sqlUpdatePermissionTable.append(permissionsTableName).append(" (").append(annotation_id).append(",").append(principal_id).append(",").append(this.permission ).append(") VALUES (:annotationId, :principalId, :status)");
        final int affectedPermissions = getSimpleJdbcTemplate().update(sqlUpdatePermissionTable.toString(), paramsPermissions);
        return affectedPermissions;
    }
    
    
 
    //////////////////////////////////////////////////////////////////////////////////

   
    /////////////////// DELETERS //////////////////////////
    @Override
    public int deleteAnnotation(Number annotationID) throws SQLException {
        if (annotationIsInUse(annotationID)) {
            return 0;
        }
        StringBuilder sqlAnnotation = new StringBuilder("DELETE FROM ");
        sqlAnnotation.append(annotationTableName).append(" where ").append(annotation_id).append(" = ?");
        return (getSimpleJdbcTemplate().update(sqlAnnotation.toString(), annotationID)); 
    }
    
    
    @Override
    public int deleteAllAnnotationSource(Number annotationID) throws SQLException {
        StringBuilder sqlTargetSources = new StringBuilder("DELETE FROM ");
        sqlTargetSources.append(annotationsSourcesTableName).append(" WHERE ").append(annotation_id).append(" = ?");
        return getSimpleJdbcTemplate().update(sqlTargetSources.toString(), annotationID); // # removed "annotations_target_sources" rows
       
    }
    

    //////////////////////////////////////////////////////
    @Override
    public int deleteAnnotationPrincipalPermissions(Number annotationID) throws SQLException {
        StringBuilder sqlPermissions = new StringBuilder("DELETE FROM ");
        sqlPermissions.append(permissionsTableName).append(" WHERE ").append(annotation_id).append(" = ?");
        return getSimpleJdbcTemplate().update(sqlPermissions.toString(), annotationID); // removed "permission" rows 
        
    }
    

    
    /////////////// helpers //////////////////
    
    @Override
    public String[] splitBody(AnnotationBody body) {
        String[] result= new String[2];
        result[0] = body.getMimeType();
        result[1] = body.getValue();
        return result;
    }

    @Override
    public AnnotationBody makeBody(String text, String mimeType) {
        AnnotationBody result = new AnnotationBody();
        result.setMimeType(mimeType);
        result.setValue(text);
        return result;
    }
    
    ///////////////////////////////////////////////////////////
    private ResourceREF getResourceREF(String resourceID) {
        ResourceREF result = new ResourceREF();
        result.setRef(resourceID);
        return result;
    }
    
  
    
}