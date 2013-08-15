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

import eu.dasish.annotation.backend.dao.CachedRepresentationDao;
import eu.dasish.annotation.backend.dao.VersionDao;
import eu.dasish.annotation.backend.identifiers.CachedRepresentationIdentifier;
import eu.dasish.annotation.backend.identifiers.VersionIdentifier;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.Version;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author olhsha
 */
public class JdbcVersionDao extends JdbcResourceDao implements VersionDao {

    @Autowired
    CachedRepresentationDao jdbcCachedRepresentationDao;

    public JdbcVersionDao(DataSource dataSource) {
        setDataSource(dataSource);
        internalIdName = version_id;
        resourceTableName = versionTableName;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////    
    @Override
    public VersionIdentifier getExternalID(Number internalID) {
        return new VersionIdentifier(super.getExternalIdentifier(internalID));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////    
    @Override
    public Number getInternalID(VersionIdentifier externalID) {
        return (super.getInternalID(externalID));
    }

    ///////////////////////////////////////////////////////////////
    @Override
    public Version getVersion(Number internalID) {

        String sql = "SELECT " + versionStar + " FROM " + versionTableName + " WHERE " + version_id + "= ? LIMIT 1";
        List<Version> result = getSimpleJdbcTemplate().query(sql, versionRowMapper, internalID);

        if (result == null) {
            return null;
        }
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }
    private final RowMapper<Version> versionRowMapper = new RowMapper<Version>() {
        @Override
        public Version mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Version result = new Version();
            // TODO: clarify situation with the attribute cached representation
            //result.setCachedRepresentations!!! The same situation as with permissions lists: we cannot refer from a filed to a list of smth, we have a separate joint table
            // TODO: attribute URI (external-id is missing)
            result.setVersion(rs.getString("external_id"));
            return result;
        }
    };

    ////////////////////////////////////////////////////////////////////////////
    @Override
    public List<Number> retrieveCachedRepresentationList(Number versionID) {
        String sql = "SELECT " + cached_representation_id + " FROM " + versionsCachedRepresentationsTableName + " WHERE " + version_id + "= ?";
        List<Number> result = getSimpleJdbcTemplate().query(sql, cachedIDRowMapper, versionID);

        if (result == null) {
            return null;
        }

        return result;
    }

    private final RowMapper<Number> cachedIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getInt(cached_representation_id);
        }
    };
    
    ///////////////////////////////////////// 
    @Override
    public int[] deleteVersion(Number internalID) {
        
        int[] result = new int[3];
        
        if (versionIsInUse(internalID)) {
            // firs delete sources that refer to it!
            result[0]=0;
            result[1]=0;
            result[2]=0;
            return result;
        }
        
        // retrieve the list of cached representations of the version to be deleted;
        // they should be deleted if they are not used
        // you will use it over the next 2 steps
        List<Number> cachedRepresentations = retrieveCachedRepresentationList(internalID);

        // remove all the pairs (internalID, cached_representation) from the joint table "versions_cahched_representations"   
        String sqlVersionsCachedRepresentations = "DELETE FROM " + versionsCachedRepresentationsTableName + " where " + version_id + " = ?";
        result[0] = getSimpleJdbcTemplate().update(sqlVersionsCachedRepresentations, internalID);
        
        // the main action: remove the version with internalID from "version" table
        String sql = "DELETE FROM " + versionTableName + " where " + version_id + " = ?";
        result[1] = getSimpleJdbcTemplate().update(sql, internalID);

        // remove the cached representations of "cachedRepresentations" from the DB unless they are still mentioned in "versions_cached_representations"
        result[2]=0;
        for (Number cachedID : cachedRepresentations) {
             result[2]=result[2]+jdbcCachedRepresentationDao.deleteCachedRepresentationInfo(cachedID);
            
        }
        return result;

    }

    /////////////////////////////////////////////////
    @Override
    public Number addVersion(Version freshVersion) {
        VersionIdentifier externalIdentifier = new VersionIdentifier();
        String newExternalIdentifier = externalIdentifier.toString();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", newExternalIdentifier);
        //TODO: till the schema is fixed, version-text and version's external Id are the same (now version do not have URI's/ext id's)
        params.put("version", newExternalIdentifier);
        String sql = "INSERT INTO " + versionTableName + "(" + external_id + "," + version + " ) VALUES (:externalId, :version)";
        final int affectedRows = getSimpleJdbcTemplate().update(sql, params);
        return getInternalID(externalIdentifier);
    }
    
  
  
    
    //////////////////////////////
    private boolean versionIsInUse(Number versionsID) {
        String sql = "SELECT " + source_id + " FROM " + sourcesVersionsTableName + " WHERE " + version_id + "= ? LIMIT 1";
        List<Number> result = getSimpleJdbcTemplate().query(sql, sourceIDRowMapper, versionsID);
        if (result.size() > 0) {
            return true;
        }
        return false;
    }
    
     private final RowMapper<Number> sourceIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getInt(source_id);
        }
    };
}
