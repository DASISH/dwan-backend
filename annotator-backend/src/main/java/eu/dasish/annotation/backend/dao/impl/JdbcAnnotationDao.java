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
import eu.dasish.annotation.schema.AnnotationBody.TextBody;
import eu.dasish.annotation.schema.AnnotationBody.XmlBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.Permission;
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

    @Override
    public void setServiceURI(String serviceURI) {
        _serviceURI = serviceURI;
    }

    ///////////// GETTERS /////////////
    @Override
    public List<Number> retrieveTargetIDs(Number annotationID) {
        if (annotationID != null) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
        sql.append(target_id).append(" FROM ").append(annotationsTargetsTableName).append(" WHERE ").append(annotation_id).append("= ?");
        return getSimpleJdbcTemplate().query(sql.toString(), TargetIDRowMapper, annotationID);
        }
        else {
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    @Override
    public List<Map<Number, String>> getPermissions(Number annotationID) {
        if (annotationID == null) {
            return null;
        }
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(principal_id).append(",").append(permission).append(" FROM ").append(permissionsTableName).append(" WHERE ").append(annotation_id).append("  = ?");
        return getSimpleJdbcTemplate().query(sql.toString(), principalsPermissionsRowMapper, annotationID);
    }
    private final RowMapper<Map<Number, String>> principalsPermissionsRowMapper = new RowMapper<Map<Number, String>>() {
        @Override
        public Map<Number, String> mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Map<Number, String> result = new HashMap<Number, String>();
            result.put(rs.getInt(principal_id), rs.getString(permission));
            return result;
        }
    };

    @Override
    public Permission getPermission(Number annotationID, Number userID) {
        if (annotationID == null || userID == null) {
            return null;
        }
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(permission).append(" FROM ").append(permissionsTableName).append(" WHERE ").
                append(annotation_id).append("  = ").append(annotationID.toString()).
                append(principal_id).append("  = ").append(userID.toString()).append(" LIMIT 1");
        List<Permission> result = getSimpleJdbcTemplate().query(sql.toString(), permissionRowMapper);
        if (result == null) {
            return null;
        }
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }
    private final RowMapper<Permission> permissionRowMapper = new RowMapper<Permission>() {
        @Override
        public Permission mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return Permission.fromValue(rs.getString(permission));
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
    public List<Number> retrieveAnnotationList(List<Number> TargetIDs) {
        if (TargetIDs == null) {
            return null;
        }
        if (TargetIDs.isEmpty()) {
            return new ArrayList<Number>();
        }
        String values = makeListOfValues(TargetIDs);
        StringBuilder query = new StringBuilder("SELECT DISTINCT ");
        query.append(annotation_id).append(" FROM ").append(annotationsTargetsTableName).append(" WHERE ").append(target_id).append(" IN ");
        query.append(values);
        return getSimpleJdbcTemplate().query(query.toString(), internalIDRowMapper);
    }

    @Override
    public Map<AnnotationInfo, Number> getAnnotationInfoWithoutTargets(Number annotationID) {
        if (annotationID == null) {
            return null;
        }
        StringBuilder sql = new StringBuilder("SELECT  ");
        sql.append(annotationStar).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotation_id).append("  = ? ");
        List<Map<AnnotationInfo, Number>> result = getSimpleJdbcTemplate().query(sql.toString(), annotationInfoRowMapper, annotationID);
        if (result == null) {
            return null;
        }
        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }
    private final RowMapper<Map<AnnotationInfo, Number>> annotationInfoRowMapper = new RowMapper<Map<AnnotationInfo, Number>>() {
        @Override
        public Map<AnnotationInfo, Number> mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Map<AnnotationInfo, Number> result = new HashMap<AnnotationInfo, Number>();
            AnnotationInfo annotationInfo = new AnnotationInfo();
            annotationInfo.setRef(externalIDtoURI(rs.getString(external_id)));
            annotationInfo.setHeadline(rs.getString(headline));
            try {
                annotationInfo.setTimeStamp(Helpers.setXMLGregorianCalendar(rs.getTimestamp(time_stamp)));
            } catch (DatatypeConfigurationException e) {
                System.out.println(e);
            }
            result.put(annotationInfo, rs.getInt(owner_id));
            return result;
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
    public List<String> getAnnotationREFs(List<Number> annotationIDs) {
        if (annotationIDs == null) {
            return null;
        }
        if (annotationIDs.isEmpty()) {
            return (new ArrayList<String>());
        }

        String values = makeListOfValues(annotationIDs);
        StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
        sql.append(external_id).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotationAnnotation_id).append("  IN ").append(values);
        return getSimpleJdbcTemplate().query(sql.toString(), annotationREFRowMapper);
    }
    private final RowMapper<String> annotationREFRowMapper = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return externalIDtoURI(rs.getString(external_id));
        }
    };

    //////////////////////////////////////////////////////////////////////////
    @Override
    public Map<Annotation, Number> getAnnotationWithoutTargetsAndPermissions(Number annotationID) throws SQLException {
        if (annotationID == null) {
            return null;
        }
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(annotationStar).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotationAnnotation_id).append("= ? LIMIT  1");
        List<Map<Annotation, Number>> respond = getSimpleJdbcTemplate().query(sql.toString(), annotationRowMapper, annotationID);
        return (respond.isEmpty() ? null : respond.get(0));
    }
    private final RowMapper<Map<Annotation, Number>> annotationRowMapper = new RowMapper<Map<Annotation, Number>>() {
        @Override
        public Map<Annotation, Number> mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Map<Annotation, Number> result = new HashMap<Annotation, Number>();

            Annotation annotation = new Annotation();
            result.put(annotation, rs.getInt(owner_id));

            annotation.setHeadline(rs.getString(headline));

            AnnotationBody body = new AnnotationBody();
            if (rs.getBoolean(is_xml)) {
                body.setTextBody(null);
                XmlBody xmlBody = new XmlBody();
                xmlBody.setMimeType(rs.getString(body_mimetype));
                xmlBody.setAny(Helpers.stringToElement(rs.getString(body_text)));
                body.setXmlBody(xmlBody);
            } else {
                body.setXmlBody(null);
                TextBody textBody = new TextBody();
                textBody.setMimeType(rs.getString(body_mimetype));
                textBody.setValue(rs.getString(body_text));
                body.setTextBody(textBody);
            }
            annotation.setBody(body);

            annotation.setTargets(null);
            annotation.setURI(externalIDtoURI(rs.getString(external_id)));

            try {
                annotation.setTimeStamp(Helpers.setXMLGregorianCalendar(rs.getTimestamp(time_stamp)));
                return result;
            } catch (DatatypeConfigurationException e) {
                System.out.println(e);
            }
            return result;
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

        StringBuilder sqlTargets = new StringBuilder("SELECT ");
        sqlTargets.append(target_id).append(" FROM ").append(annotationsTargetsTableName).append(" WHERE ").append(annotation_id).append("= ? LIMIT 1");
        List<Number> resultTargets = getSimpleJdbcTemplate().query(sqlTargets.toString(), TargetIDRowMapper, annotationID);
        if (resultTargets.size() > 0) {
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
    public int updateBodyMimeType(Number annotationID, String newMimeType) {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(annotationTableName).append(" SET ").append(body_mimetype).append("= '").append(newMimeType).append("' WHERE ").append(annotation_id).append("= ?");
        return getSimpleJdbcTemplate().update(sql.toString(), annotationID);
    }

    // TODO Unit test
    @Override
    public int updateAnnotation(Annotation annotation, Number ownerID) throws SQLException, Exception {

        String[] body = retrieveBodyComponents(annotation);

        String externalID = stringURItoExternalID(annotation.getURI());
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(annotationTableName).append(" SET ").
                append(body_text).append("= '").append(body[0]).append("',").
                append(body_mimetype).append("= '").append(body[1]).append("',").
                append(headline).append("= '").append(annotation.getHeadline()).append("',").
                append(time_stamp).append("= '").append(annotation.getTimeStamp().toString()).append("',").
                append(is_xml).append("= '").append(annotation.getBody().getXmlBody() != null).
                append("' WHERE ").append(external_id).append("= ?");
        int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), externalID);
        return affectedRows;
    }

    @Override
    public int updateAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission) throws SQLException {

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(permissionsTableName).append(" SET ").
                append(this.permission).append("= '").append(permission.value()).
                append("' WHERE ").append(annotation_id).append("= ").append(annotationID).
                append(" AND ").append(principal_id).append("= ").append(userID);
        return getSimpleJdbcTemplate().update(sql.toString());
    }

    //////////// ADDERS ////////////////////////
    @Override
    public Number addAnnotation(Annotation annotation, Number ownerID) throws SQLException, Exception {

        String[] body = retrieveBodyComponents(annotation);

        // generate a new annotation ID 
        UUID externalID = UUID.randomUUID();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalID.toString());
        params.put("ownerId", ownerID);
        params.put("headline", annotation.getHeadline());
        params.put("bodyText", body[0]);
        params.put("bodyMimeType", body[1]);
        params.put("isXml", annotation.getBody().getXmlBody() != null);

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(annotationTableName).append("(").append(external_id).append(",").append(owner_id);
        sql.append(",").append(headline).append(",").append(body_text).append(",").append(body_mimetype).append(",").append(is_xml).append(" ) VALUES (:externalId, :ownerId, :headline, :bodyText, :bodyMimeType, :isXml)");
        int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), params);
        return ((affectedRows > 0) ? getInternalID(externalID) : null);
    }

    //////////////////////////////////////////////////////////////////////////////////
    @Override
    public int addAnnotationTarget(Number annotationID, Number targetID) throws SQLException {
        Map<String, Object> paramsAnnotationsTargets = new HashMap<String, Object>();
        paramsAnnotationsTargets.put("annotationId", annotationID);
        paramsAnnotationsTargets.put("targetId", targetID);
        StringBuilder sqlAnnotationsTargets = new StringBuilder("INSERT INTO ");
        sqlAnnotationsTargets.append(annotationsTargetsTableName).append("(").append(annotation_id).append(",").append(target_id).append(" ) VALUES (:annotationId, :targetId)");
        return getSimpleJdbcTemplate().update(sqlAnnotationsTargets.toString(), paramsAnnotationsTargets);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int addAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission) throws SQLException {
        Map<String, Object> paramsPermissions = new HashMap<String, Object>();
        paramsPermissions.put("annotationId", annotationID);
        paramsPermissions.put("principalId", userID);
        paramsPermissions.put("status", permission.value());
        StringBuilder sqlUpdatePermissionTable = new StringBuilder("INSERT INTO ");
        sqlUpdatePermissionTable.append(permissionsTableName).append(" (").append(annotation_id).append(",").append(principal_id).append(",").append(this.permission).append(") VALUES (:annotationId, :principalId, :status)");
        final int affectedPermissions = getSimpleJdbcTemplate().update(sqlUpdatePermissionTable.toString(), paramsPermissions);
        return affectedPermissions;
    }

    //////////////////////////////////////////////////////////////////////////////////
    /////////////////// DELETERS //////////////////////////
    @Override
    public int deleteAnnotation(Number annotationID) throws SQLException {
        if (annotationID != null) {
            if (annotationIsInUse(annotationID)) {
                return 0;
            }
            StringBuilder sqlAnnotation = new StringBuilder("DELETE FROM ");
            sqlAnnotation.append(annotationTableName).append(" where ").append(annotation_id).append(" = ?");
            return (getSimpleJdbcTemplate().update(sqlAnnotation.toString(), annotationID));
        } else {
            return 0;
        }
    }

    @Override
    public int deleteAllAnnotationTarget(Number annotationID) throws SQLException {
        if (annotationID != null) {
            StringBuilder sqlTargetTargets = new StringBuilder("DELETE FROM ");
            sqlTargetTargets.append(annotationsTargetsTableName).append(" WHERE ").append(annotation_id).append(" = ?");
            return getSimpleJdbcTemplate().update(sqlTargetTargets.toString(), annotationID); // # removed "annotations_target_Targets" rows
        } else {
            return 0;
        }
    }

    //////////////////////////////////////////////////////
    @Override
    public int deleteAnnotationPrincipalPermissions(Number annotationID) throws SQLException {
        if (annotationID != null) {
            StringBuilder sqlPermissions = new StringBuilder("DELETE FROM ");
            sqlPermissions.append(permissionsTableName).append(" WHERE ").append(annotation_id).append(" = ?");
            return getSimpleJdbcTemplate().update(sqlPermissions.toString(), annotationID); // removed "permission" rows 
        } else {
            return 0;
        }

    }

    /////////////// helpers //////////////////
    private String[] retrieveBodyComponents(Annotation annotation) throws Exception {
        boolean body_is_xml = annotation.getBody().getXmlBody() != null;
        String[] result = new String[2];
        if (body_is_xml) {
            result[0] = Helpers.elementToString(annotation.getBody().getXmlBody().getAny());
            result[1] = annotation.getBody().getXmlBody().getMimeType();
        } else {
            TextBody textBody = annotation.getBody().getTextBody();
            if (textBody != null) {
                result[0] = textBody.getValue();
                result[1] = textBody.getMimeType();
            } else {
                throw (new Exception());
            }
        }
        return result;
    }
}