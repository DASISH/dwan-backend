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
import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationBody.TextBody;
import eu.dasish.annotation.schema.AnnotationBody.XmlBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.PermissionList;
import java.io.IOException;
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
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.xml.sax.SAXException;

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
    public void setResourcePath(String relResourcePath) {
        _relResourcePath = relResourcePath;
    }

    ///////////// GETTERS /////////////
    ///////////////////////////////////////////////////////////////////
    @Override
    public List<Map<Number, String>> getPermissions(Number annotationID) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(principal_id).append(",").append(access).append(" FROM ").append(permissionsTableName).append(" WHERE ").append(annotation_id).append("  = ?");
        return this.loggedQuery(sql.toString(), principalsAccesssRowMapper, annotationID);
    }
    
    private List<Access> getAccessHelper(Number annotationID, Number principalID){
        Map<String, Number> params = new HashMap<String, Number>();
        params.put("annotationId", annotationID);
        params.put("principalId", principalID);

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(access).append(" FROM ").append(permissionsTableName).append(" WHERE ").
                append(annotation_id).append("  =  :annotationId ").append(" AND ").
                append(principal_id).append("  = :principalId").append(" LIMIT 1");
        return this.loggedQuery(sql.toString(), accessRowMapper, params);
    }
    
    @Override
    public boolean hasExplicitAccess(Number annotationID, Number principalID) {
        List<Access> result = this.getAccessHelper(annotationID, principalID);
        if (result == null || result.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Access getAccess(Number annotationID, Number principalID) {
     List<Access> result = this.getAccessHelper(annotationID, principalID);   
        if (result == null || result.isEmpty()) {
            return Access.NONE;
        } else {
            return result.get(0);
        }
    }
   

    /////////////////////////////////////////////////////////////////////
    @Override
    public Access getPublicAttribute(Number annotationID) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(public_).append(" FROM ").append(annotationTableName).append(" WHERE ").
                append(annotation_id).append("  =  ? ").append(" LIMIT 1");
        List<Access> result = this.loggedQuery(sql.toString(), public_RowMapper, annotationID);
        if (result == null || result.isEmpty()) {
            return Access.NONE;
        } else {
            return result.get(0);
        }

    }

    @Override
    public List<UUID> getExternalIdFromHeadline(String headline) {
        StringBuilder requestDB = new StringBuilder("SELECT ");
        requestDB.append(external_id).append(" FROM ").append(annotationTableName).append(" WHERE ").append("'").append(headline).append("'").append("= ?");
        return this.loggedQuery(requestDB.toString(), externalIDRowMapper, headline);
    }

    @Override
    public List<Number> getInternalIDsFromHeadline(String headline) {
        StringBuilder requestDB = new StringBuilder("SELECT ");
        requestDB.append(annotation_id).append(" FROM ").append(annotationTableName).append(" WHERE ").append("'").append(headline).append("'").append("= ?");
        return this.loggedQuery(requestDB.toString(), internalIDRowMapper, headline);
    }

    ////////////////////////////////////////////////////////////////////////
    @Override
    public List<Number> getFilteredAnnotationIDs(Number ownerID, String text, String namespace, String after, String before) {

        StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
        sql.append(annotation_id).append(" FROM ").append(annotationTableName).append(" WHERE TRUE ");
        Map<String, Object> params = new HashMap<String, Object>();

        if (ownerID != null) {
            sql.append(" AND ").append(owner_id).append("  = :ownerId");
            params.put("ownerId", ownerID);
        }

        if (after != null) {
            sql.append(" AND ").append(last_modified).append("  > :afterTimestamp");
            params.put("afterTimestamp", Timestamp.valueOf(after));
        }

        if (before != null) {
            sql.append(" AND ").append(last_modified).append("  < :beforeTimestamp");
            params.put("beforeTimestamp", Timestamp.valueOf(before));
        }

        if (text != null) {
            sql.append(" AND ").append(body_text).append("  LIKE '%").append(text).append("%'");
        }

        return this.loggedQuery(sql.toString(), internalIDRowMapper, params);
    }


    ///////////////////////////////////////////////////////////////////////////////////
    //this method does not include all the annotations which are NOT in the pair with principal in this table
    // they have all default NONE access
    @Override
    public List<Number> getAnnotationIDsPermissionAtLeast(Number principalID, Access access) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(annotation_id).append(" FROM ").append(permissionsTableName).append(" WHERE ").
                append(principal_id).append("  = ?").append(" AND ").
                append(this.sqlAccessConstraint(this.access, access));
        return this.loggedQuery(sql.toString(), internalIDRowMapper, principalID);
    }

    /// helper ///
    private String sqlAccessConstraint(String column, Access access) {
        StringBuilder accessConstraint = new StringBuilder();
        if (access.equals(Access.READ)) {
            accessConstraint.append("(").append(column).append("  = '").append(Access.READ.value()).append("'");
            accessConstraint.append(" OR ").append(column).append("  = '").append(Access.WRITE.value()).append("'");
            accessConstraint.append(" OR ").append(column).append("  = '").append(Access.ALL.value()).append("')");
        } else {
            if (access.equals(Access.WRITE)) {
                accessConstraint.append("(").append(column).append("  = '").append(Access.WRITE.value()).append("'");
                accessConstraint.append(" OR ").append(column).append("  = '").append(Access.ALL.value()).append("')");
            } else if (access.equals(Access.ALL)) {
                accessConstraint.append(column).append("  = '").append(Access.ALL.value()).append("'");
            } else {
                accessConstraint.append(column).append("  = '").append(Access.NONE.value()).append("'");
            }
        }
        return accessConstraint.toString();
    }

    /////////////
    @Override
    public List<Number> getAnnotationIDsPublicAtLeast(Access access) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(annotation_id).append(" FROM ").append(annotationTableName).append(" WHERE ").
                append(this.sqlAccessConstraint(public_, access));
        return this.loggedQuery(sql.toString(), internalIDRowMapper);
    }

    /////////////////////////////////////////
    @Override
    public List<Number> getAllAnnotationIDs() {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(annotation_id).append(" , ").append(last_modified).append(" FROM ").append(annotationTableName).append(" ORDER BY ").append(last_modified).append(" DESC");
        return this.loggedQuery(sql.toString(), internalIDRowMapper);
    }

    @Override
    public List<Number> sublistOrderedAnnotationIDs(List<Number> annotationIDs, int offset, int limit, String orderedBy, String direction) {

        String values = makeListOfValues(annotationIDs);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("offset", offset);
        params.put("limit", limit);

        StringBuilder query = new StringBuilder("SELECT DISTINCT ");
        query.append(annotation_id).append(", ").append(orderedBy).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotation_id).append(" IN ");
        query.append(values).append(" ORDER BY ").append(orderedBy).append(" ").append(direction).append(" ");

        if (limit > -1) {
            query.append(" LIMIT :limit ");
        }

        query.append(" OFFSET :offset ");
        return this.loggedQuery(query.toString(), internalIDRowMapper, params);
    }

    //////////////////////////////////////////////////////////////////////
    @Override
    public AnnotationInfo getAnnotationInfoWithoutTargetsAndOwner(Number annotationID) {
        StringBuilder sql = new StringBuilder("SELECT  ");
        sql.append(annotationStar).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotation_id).append("  = ? ");
        List<AnnotationInfo> result = this.loggedQuery(sql.toString(), annotationInfoRowMapper, annotationID);
        return result.get(0);
    }
    private final RowMapper<AnnotationInfo> annotationInfoRowMapper = new RowMapper<AnnotationInfo>() {
        @Override
        public AnnotationInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            AnnotationInfo annotationInfo = new AnnotationInfo();
            String externalId = rs.getString(external_id);
            annotationInfo.setHref(externalIDtoHref(externalId));
            annotationInfo.setHeadline(rs.getString(headline));
            annotationInfo.setLastModified(timeStampToXMLGregorianCalendar(rs.getString(last_modified)));
            return annotationInfo;
        }
    };

   
    @Override
    public List<String> getAnnotationREFs(List<Number> annotationIDs) {

        if (annotationIDs.isEmpty()) {
            return (new ArrayList<String>());
        }

        String values = makeListOfValues(annotationIDs);
        StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
        sql.append(external_id).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotation_id).append("  IN ").append(values);
        return this.loggedQuery(sql.toString(), annotationREFRowMapper);
    }
    private final RowMapper<String> annotationREFRowMapper = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return externalIDtoHref(rs.getString(external_id));
        }
    };

    //////////////////////////////////////////////////////////////////////////
    @Override
    public Annotation getAnnotationWithoutTargetsAndPemissionList(Number annotationID) {

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(annotationStar).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotation_id).append("= ? LIMIT  1");
        List<Annotation> result = this.loggedQuery(sql.toString(), annotationRowMapper, annotationID);
        return result.get(0);
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
                String bodyText = rs.getString(body_text);
                try {
                    try {
                        try {
                            xmlBody.setAny(Helpers.stringToElement(bodyText));
                            body.setXmlBody(xmlBody);
                        } catch (SAXException e3) {
                            body.setXmlBody(null);
                            throw new SQLException(" body field  " + bodyText + " causes " + e3);
                        }
                    } catch (IOException e2) {
                        body.setXmlBody(null);
                        throw new SQLException("body field" + bodyText + " causes " + e2);
                    }
                } catch (ParserConfigurationException e1) {
                    body.setXmlBody(null);
                    throw new SQLException(" body field   " + bodyText + " causes " + e1);
                }
            } else {
                body.setXmlBody(null);
                TextBody textBody = new TextBody();
                textBody.setMimeType(rs.getString(body_mimetype));
                textBody.setBody(rs.getString(body_text));
                body.setTextBody(textBody);
            }
            annotation.setBody(body);
            
            PermissionList permissions = new PermissionList();
            permissions.setPublic(Access.fromValue(rs.getString(public_)));
            annotation.setPermissions(permissions);
                    
            annotation.setTargets(null);
            String externalId = rs.getString(external_id);
            annotation.setId(externalId);
            annotation.setHref(externalIDtoHref(externalId));
            annotation.setLastModified(timeStampToXMLGregorianCalendar(rs.getString(last_modified)));
            return annotation;
        }
    };

    @Override
    public Number getOwner(Number annotationID) {

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(owner_id).append(" FROM ").append(annotationTableName).append(" WHERE ").
                append(annotation_id).append("= ? LIMIT  1");
        List<Number> result = this.loggedQuery(sql.toString(), ownerIDRowMapper, annotationID);

        return result.get(0);
    }

   
    @Override
    public List<Number> getAnnotations(Number notebookID) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(annotation_id).
                append(" FROM ").append(notebooksAnnotationsTableName).append(" WHERE ").
                append(notebook_id).append(" = :notebookID");
        return this.loggedQuery(sql.toString(), internalIDRowMapper, notebookID);

    }

    /////////////////////////////////////////////////
    @Override
    public boolean targetIsInUse(Number targetID) {
        StringBuilder sqlAnnotations = new StringBuilder("SELECT ");
        sqlAnnotations.append(annotation_id).append(" FROM ").append(annotationsTargetsTableName).append(" WHERE ").append(target_id).append(" = ? LIMIT 1");
        List<Number> resultAnnotations = this.loggedQuery(sqlAnnotations.toString(), internalIDRowMapper, targetID);
        return (resultAnnotations.size() > 0);
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
        int affectedRows = this.loggedUpdate(sql.toString(), params);
        return affectedRows;
    }

    @Override
    public int updateAnnotationHeadline(Number annotationID, String header) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("annotationID", annotationID);
        params.put("headline", header);

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(annotationTableName).append(" SET ").
                append(last_modified).append("=  default,").
                append(_headline).append("= :headline ").
                append(" WHERE ").append(annotation_id).append("= :annotationID");
        int affectedRows = this.loggedUpdate(sql.toString(), params);
        return affectedRows;
    }

    // TODO Unit test
    @Override
    public int updateAnnotation(Annotation annotation, Number annotationID, Number newOwnerID) {

        String[] body = retrieveBodyComponents(annotation.getBody());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("owner", newOwnerID);
        params.put("bodyText", body[0]);
        params.put("bodyMimeType", body[1]);
        params.put("headline", annotation.getHeadline());
        params.put("isXml", annotation.getBody().getXmlBody() != null);
        params.put("externalId", annotation.getId());        
        params.put("publicAccess", annotation.getPermissions().getPublic().value());
        params.put("annotationId", annotationID);

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(annotationTableName).append(" SET ").
                append(owner_id).append("=  :owner ,").
                append(body_text).append("=  :bodyText ,").
                append(body_mimetype).append("= :bodyMimeType ,").
                append(headline).append("=  :headline ,").
                append(last_modified).append("=  default,").
                append(is_xml).append("= :isXml, ").
                append(external_id).append("= :externalId, ").
                append(public_).append("= :publicAccess").
                append(" WHERE ").append(annotation_id).append("= :annotationId");
        int affectedRows = this.loggedUpdate(sql.toString(), params);

        return affectedRows;
    }

    @Override
    public int updatePermission(Number annotationID, Number principalID, Access access) {

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("annotationID", annotationID);
        params.put("principalID", principalID);
        params.put("accessString", access.value());
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(permissionsTableName).append(" SET ").
                append(this.access).append("= :accessString").
                append(" WHERE ").append(annotation_id).append("= :annotationID").
                append(" AND ").append(principal_id).append("= :principalID");
        return this.loggedUpdate(sql.toString(), params);


    }

    @Override
    public int updatePublicAccess(Number annotationID, Access access) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("access", access.value());
        params.put("annotationId", annotationID);
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(annotationTableName).append(" SET ").
                append(public_).append("= :access").
                append(" WHERE ").append(annotation_id).append("= :annotationId");
        return this.loggedUpdate(sql.toString(), params);
    }

    //////////// ADDERS ////////////////////////
    @Override
    public Number addAnnotation(Annotation annotation, Number ownerID) throws NotInDataBaseException {

        String[] body = retrieveBodyComponents(annotation.getBody());

        // generate a new annotation ID 
        UUID externalID = Helpers.generateUUID();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalID.toString());
        params.put("owner", ownerID);
        params.put("headline", annotation.getHeadline());
        params.put("bodyText", body[0]);
        params.put("bodyMimeType", body[1]);
        params.put("isXml", annotation.getBody().getXmlBody() != null);               
        params.put("publicAccess", annotation.getPermissions().getPublic().value());

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(annotationTableName).append("(").append(external_id).append(",").append(owner_id);
        sql.append(",").append(headline).append(",").append(body_text).append(",").append(body_mimetype).append(",").append(is_xml).append(",").append(public_).
                append(" ) VALUES (:externalId, :owner, :headline, :bodyText, :bodyMimeType, :isXml, :publicAccess)");
        int affectedRows = this.loggedUpdate(sql.toString(), params);
        return getInternalID(externalID);
    }

    //////////////////////////////////////////////////////////////////////////////////
    @Override
    public int addAnnotationTarget(Number annotationID, Number targetID) {
        Map<String, Object> paramsAnnotationsTargets = new HashMap<String, Object>();
        paramsAnnotationsTargets.put("annotationId", annotationID);
        paramsAnnotationsTargets.put("targetId", targetID);
        StringBuilder sqlAnnotationsTargets = new StringBuilder("INSERT INTO ");
        sqlAnnotationsTargets.append(annotationsTargetsTableName).append("(").append(annotation_id).append(",").append(target_id).append(" ) VALUES (:annotationId, :targetId)");
        return this.loggedUpdate(sqlAnnotationsTargets.toString(), paramsAnnotationsTargets);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int addPermission(Number annotationID, Number principalID, Access access) {

        Map<String, Object> paramsAccesss = new HashMap<String, Object>();
        paramsAccesss.put("annotationId", annotationID);
        paramsAccesss.put("principalId", principalID);
        paramsAccesss.put("status", access.value());
        StringBuilder sqlUpdateAccessTable = new StringBuilder("INSERT INTO ");
        sqlUpdateAccessTable.append(permissionsTableName).append(" (").append(annotation_id).append(",").append(principal_id).append(",").append(this.access).append(") VALUES (:annotationId, :principalId, :status)");
        final int affectedAccesss = this.loggedUpdate(sqlUpdateAccessTable.toString(), paramsAccesss);
        return affectedAccesss;
    }

    //////////////////////////////////////////////////////////////////////////////////
    /////////////////// DELETERS //////////////////////////
    @Override
    public int deleteAnnotation(Number annotationID) {
        StringBuilder sqlAnnotation = new StringBuilder("DELETE FROM ");
        sqlAnnotation.append(annotationTableName).append(" where ").append(annotation_id).append(" = ?");
        return (this.loggedUpdate(sqlAnnotation.toString(), annotationID));

    }

    @Override
    public int deleteAllAnnotationTarget(Number annotationID) {
        StringBuilder sqlTargetTargets = new StringBuilder("DELETE FROM ");
        sqlTargetTargets.append(annotationsTargetsTableName).append(" WHERE ").append(annotation_id).append(" = ?");
        return this.loggedUpdate(sqlTargetTargets.toString(), annotationID); // # removed "annotations_target_Targets" rows

    }

    //////////////////////////////////////////////////////
    @Override
    public int deletePermissions(Number annotationID) {
        StringBuilder sqlAccesss = new StringBuilder("DELETE FROM ");
        sqlAccesss.append(permissionsTableName).append(" WHERE ").append(annotation_id).append(" = ?");
        return this.loggedUpdate(sqlAccesss.toString(), annotationID); // removed "access" rows 
    }
    
    //////////////////////////////////////////////////////
    @Override
    public int deletePermission(Number annotationID, Number principalID) {
        Map<String, Number> params = new HashMap();
        params.put("annotationId", annotationID);
        params.put("principalId", principalID);
        StringBuilder sqlAccesss = new StringBuilder("DELETE FROM ");
        sqlAccesss.append(permissionsTableName).append(" WHERE ").append(annotation_id).append(" = :annotationId AND ").
                append(principal_id).append(" = :principalId");
        return this.loggedUpdate(sqlAccesss.toString(), params);

    }

    ////////////////////////////////////////
    @Override
    public int deleteAnnotationFromAllNotebooks(Number annotationID) {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(notebooksAnnotationsTableName).append(" WHERE ").append(annotation_id).append(" = ?");
        return this.loggedUpdate(sql.toString(), annotationID); // removed "notebook-annotation" rows 

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