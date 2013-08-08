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
import eu.dasish.annotation.backend.identifiers.VersionIdentifier;
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
    ///////////////////////////////////////////////////////////////

    @Override
    public Version getVersion(Number internalID) {

        String sql = "SELECT " + versionStar + " FROM " + versionTableName + " WHERE " + version_id + "= ?";
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

    /////////////////////////////////////////
    @Override
    public List<Number> retrieveVersionList(Number sourceID) {
        String sql = "SELECT " + version_id + " FROM " + sourcesVersionsTableName + " WHERE " + source_id + " = ?";
        List<Number> result = getSimpleJdbcTemplate().query(sql, versionsSourcesRunnerRowMapper, sourceID);
        return result;
    }
    private final RowMapper<Number> versionsSourcesRunnerRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Number result = rs.getInt(version_id);
            return result;
        }
    };

    /////////////////////////////////////////
    @Override
    public int deleteVersionCachedRepresentationRow(Number versionID) {
        // remove all the pairs (internalID, cached_representation) from the joint table        
        String sqlVersionsCachedRepresentations = "DELETE FROM " + versionsCachedRepresentationsTableName + " where " + version_id + " = ?";
        return (getSimpleJdbcTemplate().update(sqlVersionsCachedRepresentations, versionID));
    }

    ///////////////////////////////////////// 
    //TODO: refactor ???
    @Override
    public int deleteVersion(Number internalID) {

        // check if there are sources referring to the version with "internalID", in the table "sources"versions"
        String sqlSourcesVersions = "SELECT " + source_id + " FROM " + sourcesVersionsTableName + " WHERE " + version_id + "= ?";
        List<Number> resultSourcesVersions = getSimpleJdbcTemplate().query(sqlSourcesVersions, sourcesVersionsRowMapper, internalID);

        // check if there is a source referring to the version "intrenalID", in the table "source"
        String sqlSource = "SELECT " + source_id + " FROM " + sourceTableName + " WHERE " + version_id + "= ?";
        List<Number> resultSource = getSimpleJdbcTemplate().query(sqlSource, sourceRowMapper, internalID);


        if (resultSourcesVersions.isEmpty() && resultSource.isEmpty()) {

            // You can remove the version safely!!!

            // retrieve the list of cached representations of the version to be deleted
            List<Number> cachedRepresentations = jdbcCachedRepresentationDao.retrieveCachedRepresentationList(internalID);

            // remove all the pairs (internalID, cached_representation) from the joint table        
            deleteVersionCachedRepresentationRow(internalID);

            // the main action: remove the version with internalID from "version" table
            String sql = "DELETE FROM " + versionTableName + " where " + version_id + " = ?";
            int affected_version_rows = getSimpleJdbcTemplate().update(sql, internalID);

            // remove the cached representations of "cachedRepresentations" from the DB unless they are still mentioned in "versions_cached_representations"
            for (Number cachedID : cachedRepresentations) {
                jdbcCachedRepresentationDao.deleteCachedRepresentationInfo(cachedID);
            }

            return (affected_version_rows);
        } else {
            // do not remove 
            return 0;
        }
    }
    private final RowMapper<Number> sourcesVersionsRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Number result = rs.getInt(source_id);
            return result;
        }
    };
    private final RowMapper<Number> sourceRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Number result = rs.getInt(source_id);
            return result;
        }
    };

    /////////////////////////////////////////////////
    @Override
    public Version addVersion(Version freshVersion) {
        VersionIdentifier externalIdentifier = new VersionIdentifier();
        String newExternalIdentifier = externalIdentifier.toString();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", newExternalIdentifier);
        //TODO: till the schema is fixed, version-text and version's external Id are the same (now version do not have URI's/ext id's)
        params.put("version", newExternalIdentifier);
        String sql = "INSERT INTO " + versionTableName + "(" + external_id + "," + version + " ) VALUES (:externalId, :version)";
        final int affectedRows = getSimpleJdbcTemplate().update(sql, params);

        if (affectedRows == 1) {
            Version versionAdded = makeFreshCopy(freshVersion);
            // TODO change for external identifier when the schema is fixed
            versionAdded.setVersion(newExternalIdentifier);
            return versionAdded;
        } else {
            return null;
        }
        
        // adding the corresponding cached representation is initiated from the separate service POST api/sources/<sid>/cached
        // so it is not implemented here
    }

    private Version makeFreshCopy(Version version) {
        Version result = new Version();
        // TOD: add external ID when the schema is corrected
        result.setVersion(version.getVersion());
        return result;
    }
}
