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
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.PermissionsDao;
import eu.dasish.annotation.backend.dao.SourceDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfos;
import eu.dasish.annotation.schema.NewSourceInfo;
import eu.dasish.annotation.schema.ResourceREF;
import eu.dasish.annotation.schema.SourceInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

/**
 * Created on : Jun 27, 2013, 10:30:52 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class JdbcAnnotationDao extends JdbcResourceDao implements AnnotationDao {

    @Autowired
    PermissionsDao jdbcPermissionsDao;
    @Autowired
    UserDao jdbcUserDao;
    @Autowired
    NotebookDao jdbcNotebookDao;
    @Autowired
    SourceDao jdbcSourceDao;

    public JdbcAnnotationDao(DataSource dataSource) {
        setDataSource(dataSource);
        internalIdName = annotation_id;
        resourceTableName = annotationTableName;
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     *
     * @param annotationIDs is a list of internal annotation identifiers
     * @return the list of the corresponding annotation-infos for the annotation
     * identifiers from the list; if the input list is null return null if the
     * input list is empty (zero elements) returns an empty list
     */
    @Override
    public List<AnnotationInfo> getAnnotationInfos(List<Number> annotationIDs) {

        if (annotationIDs == null) {
            return null;
        }

        if (annotationIDs.isEmpty()) {
            return (new ArrayList<AnnotationInfo>());
        }

        String values = makeListOfValues(annotationIDs);
        String sql = "SELECT " + annotationStar + " FROM " + annotationTableName + " WHERE " + annotationAnnotation_id + "  IN " + values;
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
     * returns an empty list
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
        String sql = "SELECT " + annotationAnnotation_id + " FROM " + annotationTableName + " WHERE " + annotationAnnotation_id + "  IN " + values;
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
    public Annotation getAnnotation(Number annotationID) throws SQLException {
        if (annotationID == null) {
            return null;
        }
        String sql = "SELECT " + annotationStar + " FROM " + annotationTableName + " WHERE " + annotationAnnotation_id + "= ?";
        List<Annotation> result = getSimpleJdbcTemplate().query(sql, annotationRowMapper, annotationID);

        if (result == null) {
            return null;
        }
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }
    private final RowMapper<Annotation> annotationRowMapper = new RowMapper<Annotation>() {
        @Override
        public Annotation mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Annotation result = new Annotation();

            ResourceREF ownerREF = new ResourceREF();
            ownerREF.setRef(String.valueOf(rs.getInt(owner_id)));
            result.setOwner(ownerREF);

            result.setHeadline(rs.getString(headline));

            result.setBody(convertToAnnotationBody(rs.getString(body_xml)));

            List<SourceInfo> sourceInfoList = jdbcSourceDao.getSourceInfos(rs.getInt(annotation_id));
            NewOrExistingSourceInfos noeSourceInfos = jdbcSourceDao.contructNewOrExistingSourceInfo(sourceInfoList);
            result.setTargetSources(noeSourceInfos);

            // TODO: fix: rpelace URI in the schema with external id, or make here the conversion:
            // from external ID in the DB to the URI for the class
            result.setURI(rs.getString(external_id));

            try {
                result.setTimeStamp(Helpers.setXMLGregorianCalendar(rs.getTimestamp(time_stamp)));
                return result;
            } catch (DatatypeConfigurationException e) {
                System.out.println(e);
                return result; // no date-time is set 
            }
        }
    };

    private AnnotationBody convertToAnnotationBody(String input) {
        if (input == null) {
            return null;
        }

        AnnotationBody result = new AnnotationBody();
        List<Object> element = result.getAny();
        element.add(input);
        return result;
    }

    @Override
    public int deleteAnnotation(Number annotationId) throws SQLException {

        //TODO: why  does it not work via calling notebook and permissions "remove" methods??

        String sqlNotebooks = "DELETE FROM " + notebooksAnnotationsTableName + " where " + annotation_id + " = ?";
        int affectedNotebooks = getSimpleJdbcTemplate().update(sqlNotebooks, annotationId);

        String sqlPermissions = "DELETE FROM " + permissionsTableName + " where " + annotation_id + " = ?";
        int affectedPermissions = getSimpleJdbcTemplate().update(sqlPermissions, annotationId);

        // TODO make a separate methods in sources DAO so that it handles removal of sources which are not referered by annotations
        String sqlTargetSources = "DELETE FROM " + annotationsSourcesTableName + " where " + annotation_id + " = ?";
        int affectedSources = getSimpleJdbcTemplate().update(sqlTargetSources, annotationId);

        String sqlAnnotation = "DELETE FROM " + annotationTableName + " where " + annotation_id + " = ?";
        int affectedAnnotations = getSimpleJdbcTemplate().update(sqlAnnotation, annotationId);
        if (affectedAnnotations > 1) {
            throw new SQLException("There was more than one annotation (" + affectedAnnotations + ") with the same ID " + annotationId);
        }
        return affectedAnnotations;
        //TODO implement deleting sources (see the specification document and the interfaces' javadoc
    }

    // TODO: so far URI in the xml is the same as the external_id in the DB!!
    // Change it when the decision is taken!!!
    @Override
    public Annotation addAnnotation(Annotation annotation, Number ownerID) throws SQLException {

        Annotation result = makeDeepCopy(annotation);

        ResourceREF ownerRef = new ResourceREF();
        ownerRef.setRef(String.valueOf(ownerID));
        result.setOwner(ownerRef);

        // generate a new annotation ID 
        AnnotationIdentifier annotationIdentifier = new AnnotationIdentifier();
        result.setURI(annotationIdentifier.toString());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", annotationIdentifier.toString());
        //params.put("timeStamp", annotation.getTimeStamp()); is generated while adding the annotation in the DB as "now"
        params.put("ownerId", ownerID);
        params.put("headline", annotation.getHeadline());
        params.put("bodyXml", annotation.getBody().getAny().get(0).toString());

        try {

            String sql = "INSERT INTO " + annotationTableName + "(" + external_id + "," + owner_id + "," + headline + "," + body_xml + " ) VALUES (:externalId, :ownerId, :headline, :bodyXml)";
            final int affectedRows = getSimpleJdbcTemplate().update(sql, params);

            if (affectedRows != 1) {
                throw (new SQLException("Cannot add the annotation properly"));
            }

            Number internalID = getInternalID(annotationIdentifier);

            //retrieve taime stamp for the just added annotation
            result.setTimeStamp(retrieveTimeStamp(internalID));

            // place new target sources in the DB, when necessary, update the corresponding target source info for the result
            // the joint annotations_target_sources" tabel is updated.
            List<NewOrExistingSourceInfo> sources = result.getTargetSources().getTarget();
            Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo> sourcePairs = jdbcSourceDao.addTargetSources(internalID, sources);
            sources.clear();
            sources.addAll(sourcePairs.values());

            //replace the temporary sourceId-references in the body with the persistent externalId
            String body = annotation.getBody().getAny().get(0).toString();
            String newBody = updateTargetRefsInBody(body, sourcePairs);
            List<Object> bodyXML = result.getBody().getAny();
            bodyXML.clear();
            bodyXML.add(newBody);
            String sqlUpdate = "UPDATE " + annotationTableName + " SET " + body_xml + "= ? WHERE " + annotation_id + "= " + internalID;
            int affectedRowsBodyUpd = getSimpleJdbcTemplate().update(sqlUpdate, newBody);
            if (affectedRows != 1) {
                throw (new SQLException("Cannot update the body with persistent reference ID"));
            }

            return result;
        } catch (DataAccessException exception) {
            throw exception;
        }
    }

    //////////////////////////////////////////////////
    @Override
    public AnnotationIdentifier getExternalID(Number internalID) {
        return new AnnotationIdentifier(super.getExternalIdentifier(internalID));
    }

    //////////// helpers /////////////////////// 
    /////////////////////////////////////////////////
    private String updateTargetRefsInBody(String body, Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo> sourcePairs) {
        String result = body;
        for (NewOrExistingSourceInfo tempSource : sourcePairs.keySet()) {
            NewSourceInfo newSource = tempSource.getNewSource();
            if (newSource != null) {
                result = result.replaceAll(newSource.getId(), sourcePairs.get(tempSource).getSource().getRef());
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////
    private ResourceREF getResourceREF(String resourceID) {
        ResourceREF result = new ResourceREF();
        result.setRef(resourceID);
        return result;
    }

    //////////////////////////////////////////  
    private Annotation makeDeepCopy(Annotation annotation) {

        if (annotation == null) {
            return null;
        }

        Annotation result = new Annotation();

        AnnotationBody body = new AnnotationBody();
        String bodyString = annotation.getBody().getAny().get(0).toString();
        body.getAny().add(bodyString);
        result.setBody(body);

        result.setHeadline(annotation.getHeadline());

        ResourceREF owner = new ResourceREF();
        owner.setRef(annotation.getOwner().getRef());
        result.setOwner(owner);

//        ResourceREF permissions = new ResourceREF();
//        permissions.setRef(annotation.getPermissions().getRef());
//        result.setPermissions(permissions);

        result.setPermissions(null); //we do not have permissions there

        NewOrExistingSourceInfos noesi = new NewOrExistingSourceInfos();
        noesi.getTarget().addAll(annotation.getTargetSources().getTarget());
        result.setTargetSources(noesi);

        result.setTimeStamp(annotation.getTimeStamp());
        result.setURI(annotation.getURI());

        return result;
    }
}