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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 * Created on : Jun 27, 2013, 10:30:52 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class JdbcAnnotationDao extends JdbcResourceDao implements AnnotationDao {

    private final Logger loggerAnnotationDao = LoggerFactory.getLogger(JdbcAnnotationDao.class);

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
        } else {
            loggerAnnotationDao.debug(nullArgument);
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    @Override
    public List<Map<Number, String>> getPermissions(Number annotationID) {
        if (annotationID == null) {
            loggerAnnotationDao.debug(nullArgument);
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
        if (annotationID == null) {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return null;
        }

        if (userID == null) {
            loggerAnnotationDao.debug("userID: " + nullArgument);
            return null;
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(permission).append(" FROM ").append(permissionsTableName).append(" WHERE ").
                append(annotation_id).append("  = ").append(annotationID.toString()).append(" AND ").
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

    @Override
    public List<Number> getAnnotationIDsForUserWithPermission(Number userID, String[] permissionStrings) {
        if (permissionStrings == null) {
            loggerAnnotationDao.debug("premissionStrings: " + nullArgument);
            return null;
        }

        if (userID == null) {
            loggerAnnotationDao.debug("userID: " + nullArgument);
            return null;
        }

        String values = stringsToValuesString(permissionStrings);

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(annotation_id).append(" FROM ").append(permissionsTableName).append(" WHERE ").
                append(principal_id).append("  = ").append(userID.toString()).append(" AND ").
                append(permission).append("  IN ").append(values);
        return getSimpleJdbcTemplate().query(sql.toString(), internalIDRowMapper);
    }

    private String stringsToValuesString(String[] strings) {
        if (strings == null) {
            return null;
        }

        int length = strings.length;
        if (length == 0) {
            return null;
        }
        String result = "(";
        for (int i = 0; i < length - 1; i++) {
            result = result + "'" + strings[i] + "', ";
        }
        result = result + "'" + strings[length - 1] + "')";
        return result;
    }

    ////////////////////////////////////////////////////////////////////////
    @Override
    public List<Number> getFilteredAnnotationIDs(List<Number> annotationIDs, String text, String namespace, Timestamp after, Timestamp before) {

        StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
        sql.append(annotation_id).append(" FROM ").append(annotationTableName).append(" WHERE TRUE ");
        Map<String, Object> params = new HashMap<String, Object>();

        if (annotationIDs == null) {
            loggerAnnotationDao.debug("annotationIDs: " + nullArgument);
            return null;
        } else {
            if (annotationIDs.isEmpty()) {
                return new ArrayList<Number>();
            }
        }

        String values = makeListOfValues(annotationIDs);
        sql.append(" AND ").append(annotation_id).append(" IN ").append(values);


        if (after != null) {
            sql.append(" AND ").append(last_modified).append("  > :after");
            params.put("after", after);
        }

        if (before != null) {
            sql.append(" AND ").append(last_modified).append("  < :before");
            params.put("before", before);
        }

        if (text != null) {
            sql.append(" AND ").append(body_text).append("  LIKE '%").append(text).append("%'");
        }

        return getSimpleJdbcTemplate().query(sql.toString(), internalIDRowMapper, params);
    }

    /////////////////////////////////////////
    @Override
    public List<Number> getAllAnnotationIDs() {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(annotation_id).append(" , ").append(last_modified).append(" FROM ").append(annotationTableName).append(" ORDER BY ").append(last_modified).append(" DESC");
        return getSimpleJdbcTemplate().query(sql.toString(), internalIDRowMapper);
    }

    //////////////////////////////
    @Override
    public List<Number> retrieveAnnotationList(List<Number> targetIDs) {
        if (targetIDs == null) {
            loggerAnnotationDao.debug("targetIDs: " + nullArgument);
            return null;
        }
        if (targetIDs.isEmpty()) {
            return new ArrayList<Number>();
        }
        String values = makeListOfValues(targetIDs);
        StringBuilder query = new StringBuilder("SELECT DISTINCT ");
        query.append(annotation_id).append(" FROM ").append(annotationsTargetsTableName).append(" WHERE ").append(target_id).append(" IN ");
        query.append(values);
        return getSimpleJdbcTemplate().query(query.toString(), internalIDRowMapper);
    }

    @Override
    public AnnotationInfo getAnnotationInfoWithoutTargets(Number annotationID) {
        if (annotationID == null) {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return null;
        }
        StringBuilder sql = new StringBuilder("SELECT  ");
        sql.append(annotationStar).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotation_id).append("  = ? ");
        List<AnnotationInfo> result = getSimpleJdbcTemplate().query(sql.toString(), annotationInfoRowMapper, annotationID);
        if (result == null) {
            return null;
        }
        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }
    private final RowMapper<AnnotationInfo> annotationInfoRowMapper = new RowMapper<AnnotationInfo>() {
        @Override
        public AnnotationInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            AnnotationInfo annotationInfo = new AnnotationInfo();
            annotationInfo.setRef(externalIDtoURI(rs.getString(external_id)));
            annotationInfo.setHeadline(rs.getString(headline));
            annotationInfo.setLastModified(timeStampToXMLGregorianCalendar(rs));
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
    public List<String> getAnnotationREFs(List<Number> annotationIDs) {
        if (annotationIDs == null) {
            loggerAnnotationDao.debug("annotationIDs: " + nullArgument);
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
    public Annotation getAnnotationWithoutTargetsAndPermissions(Number annotationID) {
        if (annotationID == null) {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return null;
        }
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(annotationStar).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotationAnnotation_id).append("= ? LIMIT  1");
        List<Annotation> respond = getSimpleJdbcTemplate().query(sql.toString(), annotationRowMapper, annotationID);
        return (respond.isEmpty() ? null : respond.get(0));
    }
    private final RowMapper<Annotation> annotationRowMapper = new RowMapper<Annotation>() {
        @Override
        public Annotation mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Annotation annotation = new Annotation();
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
                textBody.setBody(rs.getString(body_text));
                body.setTextBody(textBody);
            }
            annotation.setBody(body);

            annotation.setTargets(null);
            annotation.setURI(externalIDtoURI(rs.getString(external_id)));
            annotation.setLastModified(timeStampToXMLGregorianCalendar(rs));
            return annotation;
        }
    };

    @Override
    public Number getOwner(Number annotationID) {
        if (annotationID == null) {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return null;
        }
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(principal_id).append(" FROM ").append(permissionsTableName).append(" WHERE ").
                append(permission).append("= 'owner' AND ").
                append(annotation_id).append("= ? LIMIT  1");
        List<Number> respond = getSimpleJdbcTemplate().query(sql.toString(), principalIDRowMapper, annotationID);
        return (respond.isEmpty() ? null : respond.get(0));
    }

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
    public int updateAnnotationBody(Number annotationID, String text, String mimeType, Boolean isXml) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("annotationID", annotationID);
        params.put("bodyText", text);
        params.put("bodyMimeType", mimeType);
        params.put("isXml", isXml);

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(annotationTableName).append(" SET ").
                append(last_modified).append("=  default,").
                append(body_text).append("= :bodyText, ").
                append(body_mimetype).append("= :bodyMimeType, ").
                append(is_xml).append("= :isXml").
                append(" WHERE ").append(annotation_id).append("= :annotationID");
        int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), params);
        return affectedRows;
    }

    // TODO Unit test
    @Override
    public int updateAnnotation(Annotation annotation) {

        String[] body = retrieveBodyComponents(annotation.getBody());
        String externalID = stringURItoExternalID(annotation.getURI());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("bodyText", body[0]);
        params.put("bodyMimeType", body[1]);
        params.put("headline", annotation.getHeadline());
        params.put("isXml", annotation.getBody().getXmlBody() != null);
        params.put("externalID", externalID);


        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(annotationTableName).append(" SET ").
                append(body_text).append("=  :bodyText ,").
                append(body_mimetype).append("= :bodyMimeType ,").
                append(headline).append("=  :headline ,").
                append(last_modified).append("=  default,").
                append(is_xml).append("= :isXml").
                append(" WHERE ").append(external_id).append("= :externalID");
        int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), params);
        return affectedRows;
    }

    @Override
    public int updateAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission) {

        if (annotationID == null) {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return 0;
        }

        if (userID == null) {
            loggerAnnotationDao.debug("userID: " + nullArgument);
            return 0;
        }

        if (permission == null) {
            loggerAnnotationDao.debug("permission: " + nullArgument);
            return 0;
        }

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("annotationID", annotationID);
        params.put("principalID", userID);

        if (permission != null) {
            params.put("permission", permission.value());
            StringBuilder sql = new StringBuilder("UPDATE ");
            sql.append(permissionsTableName).append(" SET ").
                    append(this.permission).append("= :permission").
                    append(" WHERE ").append(annotation_id).append("= :annotationID").
                    append(" AND ").append(principal_id).append("= :principalID");
            return getSimpleJdbcTemplate().update(sql.toString(), params);
        } else {
            StringBuilder sql = new StringBuilder("DELETE FROM ");
            sql.append(permissionsTableName).append(" WHERE ").append(annotation_id).append("= :annotationID").
                    append(" AND ").append(principal_id).append("= :principalID");
            return (getSimpleJdbcTemplate().update(sql.toString(), params));
        }

    }

    //////////// ADDERS ////////////////////////
    @Override
    public Number addAnnotation(Annotation annotation) {

        String[] body = retrieveBodyComponents(annotation.getBody());

        if (annotation == null) {
            loggerAnnotationDao.debug("annotation: " + nullArgument);
            return 0;
        }

        // generate a new annotation ID 
        UUID externalID = UUID.randomUUID();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalID.toString());
        params.put("headline", annotation.getHeadline());
        params.put("bodyText", body[0]);
        params.put("bodyMimeType", body[1]);
        params.put("isXml", annotation.getBody().getXmlBody() != null);

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(annotationTableName).append("(").append(external_id);
        sql.append(",").append(headline).append(",").append(body_text).append(",").append(body_mimetype).append(",").append(is_xml).
                append(" ) VALUES (:externalId, :headline, :bodyText, :bodyMimeType, :isXml)");
        int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), params);
        return ((affectedRows > 0) ? getInternalID(externalID) : null);
    }

    //////////////////////////////////////////////////////////////////////////////////
    @Override
    public int addAnnotationTarget(Number annotationID, Number targetID) {

        if (annotationID == null) {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return 0;
        }

        if (targetID == null) {
            loggerAnnotationDao.debug("targetID: " + nullArgument);
            return 0;
        }

        Map<String, Object> paramsAnnotationsTargets = new HashMap<String, Object>();
        paramsAnnotationsTargets.put("annotationId", annotationID);
        paramsAnnotationsTargets.put("targetId", targetID);
        StringBuilder sqlAnnotationsTargets = new StringBuilder("INSERT INTO ");
        sqlAnnotationsTargets.append(annotationsTargetsTableName).append("(").append(annotation_id).append(",").append(target_id).append(" ) VALUES (:annotationId, :targetId)");
        return getSimpleJdbcTemplate().update(sqlAnnotationsTargets.toString(), paramsAnnotationsTargets);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int addAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission) {

        if (annotationID == null) {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return 0;
        }

        if (userID == null) {
            loggerAnnotationDao.debug("userID: " + nullArgument);
            return 0;
        }

        if (permission == null) {
            loggerAnnotationDao.debug("permission: " + nullArgument);
            return 0;
        }


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
    public int deleteAnnotation(Number annotationID) {
        if (annotationID != null) {
            if (annotationIsInUse(annotationID)) {
                return 0;
            }
            StringBuilder sqlAnnotation = new StringBuilder("DELETE FROM ");
            sqlAnnotation.append(annotationTableName).append(" where ").append(annotation_id).append(" = ?");
            return (getSimpleJdbcTemplate().update(sqlAnnotation.toString(), annotationID));
        } else {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return 0;
        }
    }

    @Override
    public int deleteAllAnnotationTarget(Number annotationID) {
        if (annotationID != null) {
            StringBuilder sqlTargetTargets = new StringBuilder("DELETE FROM ");
            sqlTargetTargets.append(annotationsTargetsTableName).append(" WHERE ").append(annotation_id).append(" = ?");
            return getSimpleJdbcTemplate().update(sqlTargetTargets.toString(), annotationID); // # removed "annotations_target_Targets" rows
        } else {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return 0;
        }
    }

    //////////////////////////////////////////////////////
    @Override
    public int deleteAnnotationPrincipalPermissions(Number annotationID) {
        if (annotationID != null) {
            StringBuilder sqlPermissions = new StringBuilder("DELETE FROM ");
            sqlPermissions.append(permissionsTableName).append(" WHERE ").append(annotation_id).append(" = ?");
            return getSimpleJdbcTemplate().update(sqlPermissions.toString(), annotationID); // removed "permission" rows 
        } else {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return 0;
        }

    }

    //////////////////////////////////////////////////////
    @Override
    public int deleteAnnotationPrincipalPermission(Number annotationID, Number userID) {
        if (annotationID != null) {
            if (userID != null) {
                Map<String, Number> params = new HashMap();
                params.put("annotationId", annotationID);
                params.put("userId", userID);
                StringBuilder sqlPermissions = new StringBuilder("DELETE FROM ");
                sqlPermissions.append(permissionsTableName).append(" WHERE ").append(annotation_id).append(" = :annotationId AND ").
                append(principal_id).append(" = :userId");                
                return getSimpleJdbcTemplate().update(sqlPermissions.toString(), params);  
            } else {
                loggerAnnotationDao.debug("userID: " + nullArgument);
                return 0;
            }
        } else {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return 0;
        }

    }

    /////////////// helpers //////////////////
    @Override
    public String[] retrieveBodyComponents(AnnotationBody annotationBody) {
        boolean body_is_xml = annotationBody.getXmlBody() != null;
        String[] result = new String[2];
        if (body_is_xml) {
            result[0] = Helpers.elementToString(annotationBody.getXmlBody().getAny());
            result[1] = annotationBody.getXmlBody().getMimeType();
        } else {
            TextBody textBody = annotationBody.getTextBody();
            if (textBody != null) {
                result[0] = textBody.getBody();
                result[1] = textBody.getMimeType();
            } else {
                loggerAnnotationDao.error("Ill-formed body: both options, xml-body and text-body, are set to null. ");
                return null;
            }
        }
        return result;
    }
}