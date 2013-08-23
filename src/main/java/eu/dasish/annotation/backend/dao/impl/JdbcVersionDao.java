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

import eu.dasish.annotation.backend.dao.VersionDao;
import eu.dasish.annotation.schema.Version;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author olhsha
 */
public class JdbcVersionDao extends JdbcResourceDao implements VersionDao {

   
    public JdbcVersionDao(DataSource dataSource) {
        setDataSource(dataSource);
        internalIdName = version_id;
        resourceTableName = versionTableName;
    }

  
    /////////////// GETTERS /////////////////////
    @Override
    public Version getVersion(Number internalID) {

        StringBuilder sql  = new StringBuilder("SELECT ");
        sql.append(versionStar).append(" FROM ").append(versionTableName).append(" WHERE ").append(version_id).append("= ? LIMIT 1");
        List<Version> result = getSimpleJdbcTemplate().query(sql.toString(), versionRowMapper, internalID);
        return (!result.isEmpty() ? result.get(0) : null);
    }
    private final RowMapper<Version> versionRowMapper = new RowMapper<Version>() {
        @Override
        public Version mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Version result = new Version();
            // TODO: clarify situation with the attribute cached representation
            //result.setCachedRepresentations!!! The same situation as with permissions lists: we cannot refer from a filed to a list of smth, we have a separate joint table
            // TODO: attribute URI (external-id is missing)
            result.setVersion(externalIDtoURI(_serviceURI, rs.getString(external_id)));
            return result;
        }
    };

    ////////////////////////////////////////////////////////////////////////////
    @Override
    public List<Number> retrieveCachedRepresentationList(Number versionID) {
        StringBuilder sql  = new StringBuilder("SELECT DISTINCT ");
        sql.append(cached_representation_id).append(" FROM ").append(versionsCachedRepresentationsTableName).append(" WHERE ").append(version_id).append("= ?");
        return getSimpleJdbcTemplate().query(sql.toString(), cachedIDRowMapper, versionID);
    }

  
    
    /////////////////////////////////////////////////////////////// 
    
    @Override
    public boolean versionIsInUse(Number versionsID) {
        StringBuilder sqlSources  = new StringBuilder("SELECT ");
        sqlSources.append(source_id).append(" FROM ").append(sourcesVersionsTableName).append(" WHERE ").append(version_id).append("= ? LIMIT 1");
        List<Number> resultSources = getSimpleJdbcTemplate().query(sqlSources.toString(), sourceIDRowMapper, versionsID);
        if (resultSources.size() > 0) {
            return true;
        }
        StringBuilder sqlCached  = new StringBuilder("SELECT ");
        sqlCached.append(cached_representation_id).append(" FROM ").append(versionsCachedRepresentationsTableName).append(" WHERE ").append(version_id).append("= ? LIMIT 1");
        List<Number> resultCached = getSimpleJdbcTemplate().query(sqlCached.toString(), cachedIDRowMapper, versionsID);
        return (resultCached.size() > 0) ;
    }
    
  
    
    /////////////////// ADDERS ///////////////////
    
    @Override
    public Number addVersion(Version freshVersion) {
        UUID externalIdentifier = UUID.randomUUID();
        String newExternalIdentifier = externalIdentifier.toString();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", newExternalIdentifier);
        //TODO: till the schema is fixed, version-text and version's external Id are the same (now version do not have URI's/ext id's)
        params.put("version", newExternalIdentifier);
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(versionTableName).append("(").append(external_id).append(",").append(version).append(" ) VALUES (:externalId, :version)");
        final int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), params);
        return (affectedRows>0 ? getInternalID(externalIdentifier) : null);
    }
    
       ////////////////////////////////////////////
    @Override
    public int addVersionCachedRepresentation(Number versionID, Number cachedID){        
    Map<String, Object> params = new HashMap<String, Object>();
        params.put("versionId", versionID);
        params.put("cachedId", cachedID);
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(versionsCachedRepresentationsTableName).append("(").append(version_id).append(",").append(cached_representation_id).append(" ) VALUES (:versionId, :cachedId)");
        return getSimpleJdbcTemplate().update(sql.toString(), params);
    }
    
    
    /////////////// DELETERS ////////////////////////
    
    @Override
    public int deleteVersion(Number internalID) {
        if (versionIsInUse(internalID)) {
            return 0;
        }        
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(versionTableName).append(" where ").append(version_id).append(" = ?");
        return getSimpleJdbcTemplate().update(sql.toString(), internalID);

    }

    
    
    ////////////////////////////////////////////
    @Override
    public int deleteVersionCachedRepresentation(Number versionID, Number cachedID){
    Map<String, Object> params = new HashMap<String, Object>();
         params.put("versionId", versionID);
         params.put("cachedId", cachedID);    
    StringBuilder sql = new StringBuilder("DELETE FROM ");
    sql.append(versionsCachedRepresentationsTableName).append(" WHERE ").append(version_id).append(" = :versionId AND ");
    sql.append(cached_representation_id).append("= :cachedId");
    return (getSimpleJdbcTemplate().update(sql.toString(), params));
    }
    
    ////////////////////////////////////////////
    @Override
    public int deleteAllVersionCachedRepresentation(Number versionID){
    StringBuilder sql = new StringBuilder("DELETE FROM ");
    sql.append(versionsCachedRepresentationsTableName).append(" WHERE ").append(version_id).append(" = ?");
    return (getSimpleJdbcTemplate().update(sql.toString(), versionID));
    }
        
 
  
}
