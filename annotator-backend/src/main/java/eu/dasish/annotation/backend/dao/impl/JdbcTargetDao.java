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
import eu.dasish.annotation.backend.dao.TargetDao;
import eu.dasish.annotation.schema.Target;
import eu.dasish.annotation.schema.TargetInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author olhsha
 */
public class JdbcTargetDao extends JdbcResourceDao implements TargetDao {

    private final Logger loggerTargetDao = LoggerFactory.getLogger(JdbcTargetDao.class);
    
    public JdbcTargetDao(DataSource dataTarget) {
        setDataSource(dataTarget);
        internalIdName = target_id;
        resourceTableName = targetTableName;
    }

    @Override
    public void setResourcePath(String relResourcePath) {
        _relResourcePath = relResourcePath;
    }

    //////////////////////// GETTERS ///////////////////////////////////
    @Override
    public Target getTarget(Number internalID) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(targetStar).append(" FROM ").append(targetTableName).append(" WHERE ").append(target_id).append("= ? LIMIT 1");
        List<Target> result = this.loggedQuery(sql.toString(), targetRowMapper, internalID);
        return result.get(0);
    }
    private final RowMapper<Target> targetRowMapper = new RowMapper<Target>() {
        @Override
        public Target mapRow(ResultSet rs, int rowNumber) throws SQLException {
            XMLGregorianCalendar xmlDate = timeStampToXMLGregorianCalendar(rs.getString(last_modified));
            Target result =
                    constructTarget(rs.getString(external_id), rs.getString(link_uri), rs.getString(version), xmlDate, rs.getString(fragment_descriptor));
            return result;
        }
    };

    @Override
    public String getLink(Number internalID) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(targetStar).append(" FROM ").append(targetTableName).append(" WHERE ").append(target_id).append("= ? LIMIT 1");
        List<String> result = this.loggedQuery(sql.toString(), linkRowMapper, internalID);
        return result.get(0);
    }
    private final RowMapper<String> linkRowMapper = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getString(link_uri);
        }
    };

    @Override
    public Map<Number, String> getCachedRepresentationFragmentPairs(Number targetID) {

        if (targetID == null) {
            loggerTargetDao.debug("targetID: " + nullArgument);
            return null;
        }

        Map<Number, String> result = new HashMap<Number, String>();
        String sql = "SELECT " + cached_representation_id + "," + fragment_descriptor_in_cached + " FROM " + targetsCachedRepresentationsTableName + " WHERE " + target_id + " = ?";
        List<Map<Number, String>> respond = this.loggedQuery(sql, cachedFragmentRowMapper, targetID);
        for (Map<Number, String> pair : respond) {
            result.putAll(pair);
        }
        return result;
    }
    private final RowMapper<Map<Number, String>> cachedFragmentRowMapper = new RowMapper<Map<Number, String>>() {
        @Override
        public Map<Number, String> mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Map<Number, String> result = new HashMap<Number, String>();
            result.put(rs.getInt(cached_representation_id), rs.getString(fragment_descriptor_in_cached));
            return result;
        }
    };

    ///////////////////////////////////////////////////////////////////
    @Override
    public List<TargetInfo> getTargetInfos(List<Number> targets) {

        if (targets.isEmpty()) {
            return new ArrayList<TargetInfo>();
        }

        String targetIDs = makeListOfValues(targets);

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(external_id).append(",").append(link_uri).append(",").append(version).append(",").append(fragment_descriptor).
                append(" FROM ").append(targetTableName).append(" WHERE ").append(target_id).append(" IN ").append(targetIDs);
        return this.loggedQuery(sql.toString(), targetInfoRowMapper);
    }
    private final RowMapper<TargetInfo> targetInfoRowMapper = new RowMapper<TargetInfo>() {
        @Override
        public TargetInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return constructTargetInfo(rs.getString(external_id), rs.getString(link_uri), rs.getString(version), rs.getString(fragment_descriptor));
        }
    };

    
    /////////////////////////////////////////////////////
    @Override
    public List<Number> getTargetsForLink(String link) {
        if (link == null) {
            loggerTargetDao.debug("link: " + nullArgument);
            return new ArrayList<Number>();
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(target_id).append(" FROM ").append(targetTableName).append(" WHERE ").append(link_uri).append(" =  ? ");
        return this.loggedQuery(sql.toString(), internalIDRowMapper, link);
    }

    //////////////////////////////////////
    @Override
    public boolean cachedIsInUse(Number cachedID) {

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(target_id).append(" FROM ").append(targetsCachedRepresentationsTableName).append(" WHERE ").append(cached_representation_id).append("= ? LIMIT 1");
        List<Number> result = this.loggedQuery(sql.toString(), internalIDRowMapper, cachedID);
        if (result != null) {
            return (!result.isEmpty());
        } else {
            return false;
        }
    }

    @Override
    public List<Number> getTargetIDs(Number annotationID) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
        sql.append(target_id).append(" FROM ").append(annotationsTargetsTableName).append(" WHERE ").append(annotation_id).append("= ?");
        return this.loggedQuery(sql.toString(), internalIDRowMapper, annotationID);

    }

    //////// UPDATERS //////////
    @Override
    public int updateTargetCachedRepresentationFragment(Number targetID, Number cachedID, String fragmentDescription) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("targetID", targetID);
        params.put("cachedID", cachedID);
        params.put("fragment", fragmentDescription);
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(targetsCachedRepresentationsTableName).append(" SET ").
                append(fragment_descriptor_in_cached).append("= :fragment").
                append(" WHERE ").append(target_id).append("= :targetID").append(" AND ").append(cached_representation_id).append("= :cachedID");
        return this.loggedUpdate(sql.toString(), params);
    }

    ///////////////////////// ADDERS /////////////////////////////////
    @Override
    public Number addTarget(Target target) throws NotInDataBaseException {

        UUID externalID = Helpers.generateUUID();
        String[] linkParts = splitLink(target.getLink());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalID.toString());
        params.put("linkUri", linkParts[0]);
        params.put("version", target.getVersion());
        params.put("fragmentDescriptor", linkParts[1]);
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(targetTableName).append("(").append(external_id).append(",").append(link_uri).append(",").append(version).append(",").append(fragment_descriptor).append(" ) VALUES (:externalId, :linkUri,  :version, :fragmentDescriptor)");
        final int affectedRows = this.loggedUpdate(sql.toString(), params);
        return getInternalID(UUID.fromString(externalID.toString()));
    }

    ///////////////////////////////////////////////////////////////////
    @Override
    public int addTargetCachedRepresentation(Number targetID, Number cachedID, String fragmentDescriptor) {

        Map<String, Object> paramsJoint = new HashMap<String, Object>();
        paramsJoint.put("targetId", targetID);
        paramsJoint.put("cachedId", cachedID);
        paramsJoint.put("fragmentDescriptor", fragmentDescriptor);
        StringBuilder sqlJoint = new StringBuilder("INSERT INTO ").append(targetsCachedRepresentationsTableName).append("(").append(target_id).append(",").append(cached_representation_id).append(",").append(fragment_descriptor_in_cached).append(" ) VALUES (:targetId, :cachedId, :fragmentDescriptor)");
        return this.loggedUpdate(sqlJoint.toString(), paramsJoint);
    }

    ///////////////////////////////////
    @Override
    public int deleteTarget(Number internalID) {

        StringBuilder sqlTargetsVersions = new StringBuilder("DELETE FROM ");
        sqlTargetsVersions.append(targetTableName).append(" WHERE ").append(target_id).append(" = ? ");
        return this.loggedUpdate(sqlTargetsVersions.toString(), internalID);

    }

    ///////////////////////////////////////////////////////////////////
    @Override
    public int deleteTargetCachedRepresentation(Number targetID, Number cachedID) {

        Map<String, Object> paramsJoint = new HashMap<String, Object>();
        paramsJoint.put("targetId", targetID);
        paramsJoint.put("cachedId", cachedID);
        StringBuilder sqlTargetsVersions = new StringBuilder("DELETE FROM ");
        sqlTargetsVersions.append(targetsCachedRepresentationsTableName).append(" WHERE ").append(target_id).append(" = :targetId").
                append(" AND ").append(cached_representation_id).append(" = :cachedId");
        return this.loggedUpdate(sqlTargetsVersions.toString(), paramsJoint);

    }

    /////////// HELPERS  ////////////////
    private TargetInfo constructTargetInfo(String externalID, String link, String version, String fragment) {
        TargetInfo targetInfo = new TargetInfo();
        targetInfo.setHref(externalIDtoHref(externalID));
        targetInfo.setLink(((new StringBuilder(link)).append("#").append(fragment)).toString());
        targetInfo.setVersion(version);
        return targetInfo;
    }

    private Target constructTarget(String externalID, String link, String version, XMLGregorianCalendar xmlTimeStamp, String fragment) {
        Target target = new Target();
        target.setId(externalID);
        target.setHref(externalIDtoHref(externalID));
        target.setLastModified(xmlTimeStamp);
        if (fragment != null) {
            target.setLink(((new StringBuilder(link)).append("#").append(fragment)).toString());
        } else {
            target.setLink(link);
        }
        target.setVersion(version);
        return target;
    }

    private String[] splitLink(String link) {
        if (link != null) {
            String[] result = new String[2];
            String[] parts = link.split("#");
            result[0] = parts[0];
            if (parts.length > 1) {
                StringBuilder buffer = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    if (parts[i] != null) {
                        buffer.append(parts[i]);
                    }
                }
                result[1] = buffer.toString();
            }
            return result;
        } else {
            return null;
        }
    }
}
