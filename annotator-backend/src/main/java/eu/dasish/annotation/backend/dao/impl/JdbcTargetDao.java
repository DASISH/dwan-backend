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
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author olhsha
 */
public class JdbcTargetDao extends JdbcResourceDao implements TargetDao {

    public JdbcTargetDao(DataSource dataTarget) {
        setDataSource(dataTarget);
        internalIdName = target_id;
        resourceTableName = targetTableName;
    }

    @Override
    public void setServiceURI(String serviceURI) {
        _serviceURI = serviceURI;
    }

    //////////////////////// GETTERS ///////////////////////////////////
    @Override
    public Target getTarget(Number internalID) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(TargetStar).append(" FROM ").append(targetTableName).append(" WHERE ").append(target_id).append("= ? LIMIT 1");
        List<Target> result = getSimpleJdbcTemplate().query(sql.toString(), TargetRowMapper, internalID);
        return (!result.isEmpty() ? result.get(0) : null);
    }
    private final RowMapper<Target> TargetRowMapper = new RowMapper<Target>() {
        @Override
        public Target mapRow(ResultSet rs, int rowNumber) throws SQLException {
            XMLGregorianCalendar xmlDate = timeStampToXMLGregorianCalendar(rs);
            Target result =
                    constructTarget(rs.getString(external_id), rs.getString(link_uri), rs.getString(version), xmlDate, rs.getString(fragment_descriptor));
            return result;
        }
    };

    @Override
    public String getLink(Number internalID) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(TargetStar).append(" FROM ").append(targetTableName).append(" WHERE ").append(target_id).append("= ? LIMIT 1");
        List<String> result = getSimpleJdbcTemplate().query(sql.toString(), linkRowMapper, internalID);
        return (!result.isEmpty() ? result.get(0) : null);
    }
    private final RowMapper<String> linkRowMapper = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getString(link_uri);
        }
    };

    /////////////////////////////////////////
    @Override
    public List<Number> getCachedRepresentations(Number targetID) {

        String sql = "SELECT " + cached_representation_id + " FROM " + targetsCachedRepresentationsTableName + " WHERE " + target_id + " = ?";
        return getSimpleJdbcTemplate().query(sql, cachedIDRowMapper, targetID);
    }

    @Override
    public Map<Number, String> getCachedRepresentationFragmentPairs(Number targetID) {
        Map<Number, String> result = new HashMap<Number, String>();
        String sql = "SELECT " + cached_representation_id + "," + fragment_descriptor_in_cached + " FROM " + targetsCachedRepresentationsTableName + " WHERE " + target_id + " = ?";
        List<Map<Number, String>> respond = getSimpleJdbcTemplate().query(sql, cachedFragmentRowMapper, targetID);
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
        if (targets == null) {
            return null;
        }
        if (targets.isEmpty()) {
            return new ArrayList<TargetInfo>();
        }

        String targetIDs = makeListOfValues(targets);

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(external_id).append(",").append(link_uri).append(",").append(version).
                append(" FROM ").append(targetTableName).append(" WHERE ").append(target_id).append(" IN ").append(targetIDs);
        return getSimpleJdbcTemplate().query(sql.toString(), targetInfoRowMapper);
    }
    private final RowMapper<TargetInfo> targetInfoRowMapper = new RowMapper<TargetInfo>() {
        @Override
        public TargetInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return constructTargetInfo(rs.getString(external_id), rs.getString(link_uri), rs.getString(version));
        }
    };

    /////////////////////////////////////////////////////
    @Override
    public List<Number> getTargetsReferringTo(String word) {
        String searchTerm = "%" + word + "%";
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(target_id).append(" FROM ").append(targetTableName).append(" WHERE ").append(link_uri).append(" LIKE ? ");
        return getSimpleJdbcTemplate().query(sql.toString(), internalIDRowMapper, searchTerm);
    }

    /////////////////////////////////////////////////////
    @Override
    public List<Number> getTargetsForLink(String link) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(target_id).append(" FROM ").append(targetTableName).append(" WHERE ").append(link_uri).append(" =  ? ");
        return getSimpleJdbcTemplate().query(sql.toString(), internalIDRowMapper, link);
    }

    /////////////////////////////////////////////////
    @Override
    public boolean targetIsInUse(Number targetID) {
        StringBuilder sqlAnnotations = new StringBuilder("SELECT ");
        sqlAnnotations.append(annotation_id).append(" FROM ").append(annotationsTargetsTableName).append(" WHERE ").append(target_id).append(" = ? LIMIT 1");
        List<Number> resultAnnotations = getSimpleJdbcTemplate().query(sqlAnnotations.toString(), annotationIDRowMapper, targetID);
        if (resultAnnotations == null) {
            return false;
        }
        return (resultAnnotations.size() > 0);
    }

    ///////////////////////// ADDERS /////////////////////////////////
    @Override
    public Number addTarget(Target target) {
        UUID externalID = UUID.randomUUID();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalID.toString());
        params.put("linkUri", target.getLink());
        params.put("version", target.getVersion());
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(targetTableName).append("(").append(external_id).append(",").append(link_uri).append(",").append(version).append(",").append(last_modified).append(" ) VALUES (:externalId, :linkUri,  :version, current_timestamp AT TIME ZONE INTERVAL '00:00')");
        final int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), params);
        return (affectedRows > 0 ? getInternalID(UUID.fromString(externalID.toString())) : null);
    }

    ///////////////////////////////////////////////////////////////////
    @Override
    public int addTargetCachedRepresentation(Number targetID, Number cachedID, String fragmentDescriptor) {
        Map<String, Object> paramsJoint = new HashMap<String, Object>();
        paramsJoint.put("targetId", targetID);
        paramsJoint.put("cachedId", cachedID);
        paramsJoint.put("fragmentDescriptor", fragmentDescriptor);
        StringBuilder sqlJoint = new StringBuilder("INSERT INTO ").append(targetsCachedRepresentationsTableName).append("(").append(target_id).append(",").append(cached_representation_id).append(",").append(fragment_descriptor_in_cached).append(" ) VALUES (:targetId, :cachedId, :fragmentDescriptor)");
        return getSimpleJdbcTemplate().update(sqlJoint.toString(), paramsJoint);
    }

    ///////////////////////////////////////////////////////////////////
    @Override
    public int updateSiblingClass(Number TargetID, int classID) {
        if (TargetID == null) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(targetTableName).append(" SET ").append(sibling_Target_class).append("= '").append(classID).append("' WHERE ").append(target_id).append("= ?");
        return getSimpleJdbcTemplate().update(sql.toString(), TargetID);
    }

////////////////////// DELETERS ////////////////////////
    @Override
    public int deleteTarget(Number internalID) {
        if (targetIsInUse(internalID)) {
            return 0;
        }
        StringBuilder sqlTargetsVersions = new StringBuilder("DELETE FROM ");
        sqlTargetsVersions.append(targetTableName).append(" WHERE ").append(target_id).append(" = ? ");
        return getSimpleJdbcTemplate().update(sqlTargetsVersions.toString(), internalID);

    }

    ///////////////////////////////////////////////////////////////////
    @Override
    public int deleteTargetCachedRepresentation(Number targetID, Number cachedID) {
        if (targetID == null || cachedID == null) {
            return 0;
        }
        Map<String, Object> paramsJoint = new HashMap<String, Object>();
        paramsJoint.put("targetId", targetID);
        paramsJoint.put("cachedId", cachedID);
        StringBuilder sqlTargetsVersions = new StringBuilder("DELETE FROM ");
        sqlTargetsVersions.append(targetsCachedRepresentationsTableName).append(" WHERE ").append(target_id).append(" = :targetId").
                append(" AND ").append(cached_representation_id).append(" = :cachedId");
        return getSimpleJdbcTemplate().update(sqlTargetsVersions.toString(), paramsJoint);

    }

    /////////// HELPERS  ////////////////
    private TargetInfo constructTargetInfo(String externalID, String link, String version) {
        TargetInfo targetInfo = new TargetInfo();
        targetInfo.setRef(externalIDtoURI(externalID));
        targetInfo.setLink(link);
        targetInfo.setVersion(version);
        return targetInfo;
    }

    private Target constructTarget(String externalID, String link, String version, XMLGregorianCalendar xmlTimeStamp, String fragment) {
        Target target = new Target();
        target.setURI(externalIDtoURI(externalID));
        target.setLastModified(xmlTimeStamp);
        target.setLink(link);
        target.setVersion(version);
        target.setFragmentDescriptor(fragment);
        return target;
    }
}
