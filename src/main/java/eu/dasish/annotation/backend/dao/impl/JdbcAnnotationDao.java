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
import eu.dasish.annotation.schema.Access;
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
   

    ///////////////////////////////////////////////////////////////////
    @Override
    public List<Map<Number,String>>  getPermissions(Number annotationID) {
        if (annotationID == null) {
            loggerAnnotationDao.debug(nullArgument);
            return null;
        }
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(principal_id).append(",").append(access).append(" FROM ").append(accesssTableName).append(" WHERE ").append(annotation_id).append("  = ?");
        logger.debug("Parameter "+annotation_id+" := "+annotationID.intValue());
        return this.loggedQuery(sql.toString(), principalsAccesssRowMapper, annotationID);
    }
    
    
    @Override
    public Access getAccess(Number annotationID, Number principalID) {
        if (annotationID == null) {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return null;
        }

        if (principalID == null) {
            loggerAnnotationDao.debug("principalID: " + nullArgument);
            return null;
        }
        
        Map<String, Number> params = new HashMap<String,Number>();
        params.put("annotationId", annotationID);
        params.put("principalId", principalID);
        
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(access).append(" FROM ").append(accesssTableName).append(" WHERE ").
                append(annotation_id).append("  =  :annotationId ").append(" AND ").
                append(principal_id).append("  = :principalId").append(" LIMIT 1");
        List<Access> result = this.loggedQuery(sql.toString(), accessRowMapper, params);
       
        if (result == null) {
            return null;
        }
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }
    private final RowMapper<Access> accessRowMapper = new RowMapper<Access>() {
        @Override
        public Access mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return Access.fromValue(rs.getString(access));
        }
    };

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
    @Override
    public List<Number> getAnnotationIDsForPermission(Number principalID, String access) {

        if (principalID == null) {
            loggerAnnotationDao.debug("principalID: " + nullArgument);
            return null;
        }

        if (access == null) {
            logger.info("The access argument is null. I assign it a default value 'read'.");
            access = "read";
        }


        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(annotation_id).append(" FROM ").append(accesssTableName).append(" WHERE ").
                append(principal_id).append("  = ?").append(" AND ").
                append(access).append("  = ").append(access);
        return this.loggedQuery(sql.toString(), internalIDRowMapper, principalID);
    }

    //////////////////////////////
    @Override
    public List<Number> getAnnotationIDsForTargets(List<Number> targetIDs) {
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
        return this.loggedQuery(query.toString(), internalIDRowMapper);
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
        if (annotationIDs == null) {
            return null;
        }

        if (annotationIDs.isEmpty()) {
            return annotationIDs;
        }

        
        String values = makeListOfValues(annotationIDs);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("offset", offset);
        params.put("limit", limit);

        StringBuilder query = new StringBuilder("SELECT DISTINCT ");
        query.append(annotation_id).append(" FROM ").append(annotationsTargetsTableName).append(" WHERE ").append(annotation_id).append(" IN ");
        query.append(values).append(" ORDER BY ").append(orderedBy).append(" ").append(direction).append(" ");

        if (limit > -1) {
            query.append(direction).append(" LIMIT :limit ");
        }

        query.append(" OFFSET :offset ");
        return this.loggedQuery(query.toString(), internalIDRowMapper, params);
    }

    //////////////////////////////////////////////////////////////////////
    @Override
    public AnnotationInfo getAnnotationInfoWithoutTargets(Number annotationID) {
        if (annotationID == null) {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return null;
        }
        StringBuilder sql = new StringBuilder("SELECT  ");
        sql.append(annotationStar).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotation_id).append("  = ? ");
        List<AnnotationInfo> result = this.loggedQuery(sql.toString(), annotationInfoRowMapper, annotationID);
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
            annotationInfo.setLastModified(timeStampToXMLGregorianCalendar(rs.getString(last_modified)));
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
        sql.append(external_id).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotation_id).append("  IN ").append(values);
        return this.loggedQuery(sql.toString(), annotationREFRowMapper);
    }
    private final RowMapper<String> annotationREFRowMapper = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return externalIDtoURI(rs.getString(external_id));
        }
    };

    //////////////////////////////////////////////////////////////////////////
    @Override
    public Annotation getAnnotationWithoutTargetsAndAccesss(Number annotationID) {
        if (annotationID == null) {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return null;
        }
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(annotationStar).append(" FROM ").append(annotationTableName).append(" WHERE ").append(annotation_id).append("= ? LIMIT  1");
        List<Annotation> respond = this.loggedQuery(sql.toString(), annotationRowMapper, annotationID);
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
            annotation.setLastModified(timeStampToXMLGregorianCalendar(rs.getString(last_modified)));
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
        sql.append(owner_id).append(" FROM ").append(annotationTableName).append(" WHERE ").
                append(annotation_id).append("= ? LIMIT  1");
        List<Number> respond = this.loggedQuery(sql.toString(), ownerIDRowMapper, annotationID);
        return (respond.isEmpty() ? null : respond.get(0));
    }

    /////////////////////////////
//    @Override
//    public boolean annotationIsInUse(Number annotationID) {
//        StringBuilder sqlNotebooks = new StringBuilder("SELECT ");
//        sqlNotebooks.append(notebook_id).append(" FROM ").append(notebooksAnnotationsTableName).append(" WHERE ").append(annotation_id).append("= ? LIMIT 1");
//        List<Number> resultNotebooks = this.loggedQuery(sqlNotebooks.toString(), notebookIDRowMapper, annotationID);
//        if (resultNotebooks.size() > 0) {
//            return true;
//        }
//
//        StringBuilder sqlTargets = new StringBuilder("SELECT ");
//        sqlTargets.append(target_id).append(" FROM ").append(annotationsTargetsTableName).append(" WHERE ").append(annotation_id).append("= ? LIMIT 1");
//        List<Number> resultTargets = this.loggedQuery(sqlTargets.toString(), targetIDRowMapper, annotationID);
//        if (resultTargets.size() > 0) {
//            return true;
//        }
//
//        StringBuilder sqlAccesss = new StringBuilder("SELECT ");
//        sqlAccesss.append(principal_id).append(" FROM ").append(accesssTableName).append(" WHERE ").append(annotation_id).append("= ? LIMIT 1");
//        List<Number> resultAccesss = this.loggedQuery(sqlAccesss.toString(), principalIDRowMapper, annotationID);
//        return (resultAccesss.size() > 0);
//    }
    
     @Override
    public List<Number> getAnnotations(Number notebookID) {

        if (notebookID == null) {
            loggerAnnotationDao.debug("notebookID: " + nullArgument);
            return null;
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(annotation_id).
                append(" FROM ").append(notebooksAnnotationsTableName).append(" WHERE ").
                append(notebook_id).append(" = :notebookID");
        return this.loggedQuery(sql.toString(), internalIDRowMapper, notebookID);

    }
     
       /////////////////////////////////////////////////
    @Override
    public boolean targetIsInUse(Number targetID) {
        if (targetID == null) {
            loggerAnnotationDao.debug("targetID: " + nullArgument);
            return false;
        }


        StringBuilder sqlAnnotations = new StringBuilder("SELECT ");
        sqlAnnotations.append(annotation_id).append(" FROM ").append(annotationsTargetsTableName).append(" WHERE ").append(target_id).append(" = ? LIMIT 1");
        List<Number> resultAnnotations = this.loggedQuery(sqlAnnotations.toString(), internalIDRowMapper, targetID);
        if (resultAnnotations == null) {
            return false;
        }
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

    // TODO Unit test
    @Override
    public int updateAnnotation(Annotation annotation, Number newOwnerID) {

        String[] body = retrieveBodyComponents(annotation.getBody());
        String externalID = stringURItoExternalID(annotation.getURI());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("owner", newOwnerID);
        params.put("bodyText", body[0]);
        params.put("bodyMimeType", body[1]);
        params.put("headline", annotation.getHeadline());
        params.put("isXml", annotation.getBody().getXmlBody() != null);
        params.put("externalID", externalID);


        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(annotationTableName).append(" SET ").
                append(owner_id).append("=  :owner ,").
                append(body_text).append("=  :bodyText ,").
                append(body_mimetype).append("= :bodyMimeType ,").
                append(headline).append("=  :headline ,").
                append(last_modified).append("=  default,").
                append(is_xml).append("= :isXml").
                append(" WHERE ").append(external_id).append("= :externalID");
        int affectedRows = this.loggedUpdate(sql.toString(), params);
        return affectedRows;
    }

    @Override
    public int updateAnnotationPrincipalAccess(Number annotationID, Number principalID, Access access) {

        if (annotationID == null) {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return 0;
        }

        if (principalID == null) {
            loggerAnnotationDao.debug("principalID: " + nullArgument);
            return 0;
        }

        if (access == null) {
            loggerAnnotationDao.debug("access: " + nullArgument);
            return 0;
        }

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("annotationID", annotationID);
        params.put("principalID", principalID);

        if (access != null) {
            params.put("access", access.value());
            StringBuilder sql = new StringBuilder("UPDATE ");
            sql.append(accesssTableName).append(" SET ").
                    append(this.access).append("= :access").
                    append(" WHERE ").append(annotation_id).append("= :annotationID").
                    append(" AND ").append(principal_id).append("= :principalID");
            return this.loggedUpdate(sql.toString(), params);
        } else {
            StringBuilder sql = new StringBuilder("DELETE FROM ");
            sql.append(accesssTableName).append(" WHERE ").append(annotation_id).append("= :annotationID").
                    append(" AND ").append(principal_id).append("= :principalID");
            return (this.loggedUpdate(sql.toString(), params));
        }

    }

    //////////// ADDERS ////////////////////////
    @Override
    public Number addAnnotation(Annotation annotation, Number ownerID) {

        String[] body = retrieveBodyComponents(annotation.getBody());

        if (annotation == null) {
            loggerAnnotationDao.debug("annotation: " + nullArgument);
            return null;
        }
        
        if (ownerID == null) {
            loggerAnnotationDao.debug("ownerID: " + nullArgument);
            return null;
        }

        // generate a new annotation ID 
        UUID externalID = UUID.randomUUID();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalID.toString());
        params.put("owner", ownerID);
        params.put("headline", annotation.getHeadline());
        params.put("bodyText", body[0]);
        params.put("bodyMimeType", body[1]);
        params.put("isXml", annotation.getBody().getXmlBody() != null);

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(annotationTableName).append("(").append(external_id).append(",").append(owner_id);
        sql.append(",").append(headline).append(",").append(body_text).append(",").append(body_mimetype).append(",").append(is_xml).
                append(" ) VALUES (:externalId, :owner, :headline, :bodyText, :bodyMimeType, :isXml)");
        int affectedRows = this.loggedUpdate(sql.toString(), params);
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
        return this.loggedUpdate(sqlAnnotationsTargets.toString(), paramsAnnotationsTargets);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int addAnnotationPrincipalAccess(Number annotationID, Number principalID, Access access) {

        if (annotationID == null) {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return 0;
        }

        if (principalID == null) {
            loggerAnnotationDao.debug("principalID: " + nullArgument);
            return 0;
        }

        if (access == null) {
            loggerAnnotationDao.debug("access: " + nullArgument);
            return 0;
        }


        Map<String, Object> paramsAccesss = new HashMap<String, Object>();
        paramsAccesss.put("annotationId", annotationID);
        paramsAccesss.put("principalId", principalID);
        paramsAccesss.put("status", access.value());
        StringBuilder sqlUpdateAccessTable = new StringBuilder("INSERT INTO ");
        sqlUpdateAccessTable.append(accesssTableName).append(" (").append(annotation_id).append(",").append(principal_id).append(",").append(this.access).append(") VALUES (:annotationId, :principalId, :status)");
        final int affectedAccesss = this.loggedUpdate(sqlUpdateAccessTable.toString(), paramsAccesss);
        return affectedAccesss;
    }

    //////////////////////////////////////////////////////////////////////////////////
    /////////////////// DELETERS //////////////////////////
    @Override
    public int deleteAnnotation(Number annotationID) {
        if (annotationID != null) {
            StringBuilder sqlAnnotation = new StringBuilder("DELETE FROM ");
            sqlAnnotation.append(annotationTableName).append(" where ").append(annotation_id).append(" = ?");
            return (this.loggedUpdate(sqlAnnotation.toString(), annotationID));
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
            return this.loggedUpdate(sqlTargetTargets.toString(), annotationID); // # removed "annotations_target_Targets" rows
        } else {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return 0;
        }
    }

    //////////////////////////////////////////////////////
    @Override
    public int deleteAnnotationPrincipalAccesss(Number annotationID) {
        if (annotationID != null) {
            StringBuilder sqlAccesss = new StringBuilder("DELETE FROM ");
            sqlAccesss.append(accesssTableName).append(" WHERE ").append(annotation_id).append(" = ?");
            return this.loggedUpdate(sqlAccesss.toString(), annotationID); // removed "access" rows 
        } else {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return 0;
        }

    }
    
    ////////////////////////////////////////
    @Override
    public int deleteAnnotationFromAllNotebooks(Number annotationID){
        if (annotationID != null) {
            StringBuilder sql = new StringBuilder("DELETE FROM ");
            sql.append(notebooksAnnotationsTableName).append(" WHERE ").append(annotation_id).append(" = ?");
            return this.loggedUpdate(sql.toString(), annotationID); // removed "notebook-annotation" rows 
        } else {
            loggerAnnotationDao.debug("annotationID: " + nullArgument);
            return 0;
        }
    }

    //////////////////////////////////////////////////////
    @Override
    public int deleteAnnotationPrincipalAccess(Number annotationID, Number principalID) {
        if (annotationID != null) {
            if (principalID != null) {
                Map<String, Number> params = new HashMap();
                params.put("annotationId", annotationID);
                params.put("principalId", principalID);
                StringBuilder sqlAccesss = new StringBuilder("DELETE FROM ");
                sqlAccesss.append(accesssTableName).append(" WHERE ").append(annotation_id).append(" = :annotationId AND ").
                        append(principal_id).append(" = :principalId");
                return this.loggedUpdate(sqlAccesss.toString(), params);
            } else {
                loggerAnnotationDao.debug("principalID: " + nullArgument);
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