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
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfos;
import eu.dasish.annotation.schema.ResourceREF;
import eu.dasish.annotation.schema.SourceInfo;
import java.lang.String;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public List<Number> retrieveSourceIDs(Number annotationID) {
        String sql = "SELECT " + source_id + " FROM " + annotationsSourcesTableName + " WHERE " + annotation_id + "= ?";
        List<Number> result = getSimpleJdbcTemplate().query(sql, sourceIDRowMapper, annotationID);
        return result;
    }
    private final RowMapper<Number> sourceIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Number result = rs.getInt(source_id);
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
            sql.append(" AND ").append(body_xml).append("  LIKE '%").append(text).append("%'");
        }

        List<Number> result = getSimpleJdbcTemplate().query(sql.toString(), internalIDRowMapper, params);
        return result;
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
        List<Number> result = getSimpleJdbcTemplate().query(query.toString(), internalIDRowMapper);
        return result;
    }

    @Override
    public List<AnnotationInfo> getAnnotationInfos(List<Number> annotationIDs) {

        if (annotationIDs == null) {
            return null;
        }

        if (annotationIDs.isEmpty()) {
            return (new ArrayList<AnnotationInfo>());
        }

        String values = makeListOfValues(annotationIDs);
        String sql = "SELECT DISTINCT " + annotationStar + " FROM " + annotationTableName + " WHERE " + annotationAnnotation_id + "  IN " + values;
        return getSimpleJdbcTemplate().query(sql, annotationInfoRowMapper);
    }
    private final RowMapper<AnnotationInfo> annotationInfoRowMapper = new RowMapper<AnnotationInfo>() {
        @Override
        public AnnotationInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            AnnotationInfo annotationInfo = new AnnotationInfo();
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
        String sql = "SELECT DISTINCT " + annotationAnnotation_id + " FROM " + annotationTableName + " WHERE " + annotationAnnotation_id + "  IN " + values;
        return getSimpleJdbcTemplate().query(sql, annotationREFRowMapper);
    }
    private final RowMapper<ResourceREF> annotationREFRowMapper = new RowMapper<ResourceREF>() {
        @Override
        public ResourceREF mapRow(ResultSet rs, int rowNumber) throws SQLException {
            ResourceREF annotationREF = new ResourceREF();
            annotationREF.setRef(Integer.toString(rs.getInt(annotation_id)));
            return annotationREF;
        }
    };

    //////////////////////////////////////////////////////////////////////////
    @Override    
    public Annotation getAnnotationWithoutSources(Number annotationID) throws SQLException {
        if (annotationID == null) {
            return null;        }
        String sql = "SELECT " + annotationStar + " FROM " + annotationTableName + " WHERE " + annotationAnnotation_id + "= ? LIMIT  1";
        List<Annotation> respond = getSimpleJdbcTemplate().query(sql, annotationRowMapper, annotationID);

        if (respond == null) {
            return null;
        }
      
        return respond.get(0);
    }
    
    private final RowMapper<Annotation> annotationRowMapper = new RowMapper<Annotation>() {
        @Override
        public Annotation mapRow(ResultSet rs, int rowNumber) throws SQLException {
            
            Map<String, Object> result = new HashMap<String, Object>();
            
            Annotation annotation = new Annotation();

            ResourceREF ownerREF = new ResourceREF();
            ownerREF.setRef(String.valueOf(rs.getInt(owner_id)));
            annotation.setOwner(ownerREF);

            annotation.setHeadline(rs.getString(headline));

            annotation.setBody(Helpers.deserializeBody(rs.getString(body_xml)));
            annotation.setTargetSources(null);

            // TODO: fix: rpelace URI in the schema with external id, or make here the conversion:
            // from external ID in the DB to the URI for the class
            annotation.setURI(rs.getString(external_id));

            try {
                annotation.setTimeStamp(Helpers.setXMLGregorianCalendar(rs.getTimestamp(time_stamp)));
                return annotation;
            } catch (DatatypeConfigurationException e) {
                System.out.println(e);
                return annotation; // no date-time is set 
            }
        }
    };
    
    
   
    //////////////////////////////////////////////////////
    @Override
    public int[] deleteAnnotation(Number annotationID) throws SQLException {

        int[] result = new int[3];
        
        if (annotationIsInUse(annotationID)) {
            result[0] =0;
            result[1] =0;
            result[2] =0;
            return result;
        }
        
        String sqlPermissions = "DELETE FROM " + permissionsTableName + " where " + annotation_id + " = ?";
        result[0] = getSimpleJdbcTemplate().update(sqlPermissions, annotationID); // removed "permission" rows 
        
        String sqlTargetSources = "DELETE FROM " + annotationsSourcesTableName + " where " + annotation_id + " = ?";
        result[1] = getSimpleJdbcTemplate().update(sqlTargetSources, annotationID); // removed "annotations_target_sources" rows
       
        String sqlAnnotation = "DELETE FROM " + annotationTableName + " where " + annotation_id + " = ?";
        result[2] = getSimpleJdbcTemplate().update(sqlAnnotation, annotationID); // removed annotations rows

        return result;
    }

    // TODO: so far URI in the xml is the same as the external_id in the DB!!
    // Change it when the decision is taken!!!
    @Override
    public Number addAnnotation(Annotation annotation, Number ownerID) throws SQLException {
        // generate a new annotation ID 
        AnnotationIdentifier annotationIdentifier = new AnnotationIdentifier();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", annotationIdentifier.toString());
        params.put("ownerId", ownerID);
        params.put("headline", annotation.getHeadline());
        params.put("bodyXml", annotation.getBody().getAny().get(0).toString());

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(annotationTableName).append("(").append(external_id).append(",").append(owner_id);
        sql.append(",").append(headline).append(",").append(body_xml).append(" ) VALUES (:externalId, :ownerId, :headline, :bodyXml)");
        int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), params);
        if (affectedRows == 1) {
            return getInternalID(annotationIdentifier);
        } else {
            return null;
        }

    }

    //////////////////////////////////////////////////
    @Override
    public AnnotationIdentifier getExternalID(Number internalID) {
        return new AnnotationIdentifier(super.getExternalIdentifier(internalID));
    }

    ///////////////////////////////////////////////////////////////////////
    @Override
    public int updateBody(Number annotationID, String serializedNewBody) {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(annotationTableName).append(" SET ").append(body_xml).append("= ").append(serializedNewBody).append(" WHERE ").append(annotation_id).append("= ?");
        return getSimpleJdbcTemplate().update(sql.toString(), annotationID);
    }

    ///////////////////////////////////////////////////////////
    private ResourceREF getResourceREF(String resourceID) {
        ResourceREF result = new ResourceREF();
        result.setRef(resourceID);
        return result;
    }

  
    //////////////////////////////////////////////////////////////////////////////////
    @Override
    public int addAnnotationSourcePair(Number annotationID, Number sourceID) throws SQLException {
        Map<String, Object> paramsAnnotationsSources = new HashMap<String, Object>();
        paramsAnnotationsSources.put("annotationId", annotationID);
        paramsAnnotationsSources.put("sourceId", sourceID);
        String sqlAnnotationsSources = "INSERT INTO " + annotationsSourcesTableName + "(" + annotation_id + "," + source_id + " ) VALUES (:annotationId, :sourceId)";
        int affectedRows = getSimpleJdbcTemplate().update(sqlAnnotationsSources, paramsAnnotationsSources);
        return (affectedRows);
    }
    //////////////////////////////////////////////////////////////////////////////////

    /////////////// helpers //////////////////
    
        //////////////////////////////
    private boolean annotationIsInUse(Number sourceID) {
        String sql = "SELECT " + notebook_id + " FROM " + notebooksAnnotationsTableName + " WHERE " + annotation_id + "= ? LIMIT 1";
        List<Number> result = getSimpleJdbcTemplate().query(sql, notebookIDRowMapper, sourceID);
        if (result.size() > 0) {
            return true;
        }
        return false;
    }
    
      private final RowMapper<Number> notebookIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getInt(notebook_id);
        }
    };
    
}