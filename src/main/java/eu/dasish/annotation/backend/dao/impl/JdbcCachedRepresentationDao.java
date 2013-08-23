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
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import java.lang.String;
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
public class JdbcCachedRepresentationDao extends JdbcResourceDao implements CachedRepresentationDao {
   
   
    public JdbcCachedRepresentationDao(DataSource dataSource) {
        setDataSource(dataSource);
        internalIdName = cached_representation_id;
        resourceTableName = cachedRepresentationTableName;
    }

  

    /////////////////////////// GETTERS  ////////////////////////////////////////
    @Override
    public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID) {

        String sql = "SELECT " + cachedRepresentationStar + " FROM " + cachedRepresentationTableName + " WHERE " + cached_representation_id + "= ? LIMIT 1";
        List<CachedRepresentationInfo> result = getSimpleJdbcTemplate().query(sql, cachedRepresentationRowMapper, internalID);

        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }
    private final RowMapper<CachedRepresentationInfo> cachedRepresentationRowMapper = new RowMapper<CachedRepresentationInfo>() {
        @Override
        public CachedRepresentationInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            CachedRepresentationInfo result = new CachedRepresentationInfo();
            result.setMimeType(rs.getString(mime_type));
            result.setRef(externalIDtoURI(_serviceURI, rs.getString(external_id)));
            result.setTool(rs.getString(tool));
            result.setType(rs.getString(type_));
            // TODO add "where is the file when the schema" is updated!!!!s
            return result;
        }
    };
    
    //////////////////////////////////////

    private boolean cachedIsInUse(Number cachedID) {      
        String sql = "SELECT " + version_id + " FROM " + versionsCachedRepresentationsTableName + " WHERE " + cached_representation_id + "= ? LIMIT 1";
        List<Number> result = getSimpleJdbcTemplate().query(sql, versionIDRowMapper, cachedID);
        return (!result.isEmpty());
    }
    
 

    //////////////////////// ADDERS ///////////////////////////////
    @Override
    public Number addCachedRepresentationInfo(CachedRepresentationInfo cached) {
        UUID externalIdentifier = UUID.randomUUID();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalIdentifier.toString());
        params.put("mime_type", cached.getMimeType());
        params.put("tool", cached.getTool());
        params.put("type", cached.getType());
        String sql = "INSERT INTO " + cachedRepresentationTableName + "(" + external_id + "," + mime_type + "," + tool + "," + type_ + " ) VALUES (:externalId, :mime_type,  :tool, :type)";
        final int affectedRows = getSimpleJdbcTemplate().update(sql, params);
        return (affectedRows > 0 ? getInternalID(externalIdentifier) : null);
    }

    /////////////////////// DELETERS  //////////////////////////////////////////////
    @Override
    public int deleteCachedRepresentationInfo(Number internalID) {
        if (cachedIsInUse(internalID)){
           return 0;
        }
        
        String sql = "DELETE FROM " + cachedRepresentationTableName + " where " + cached_representation_id + " = ?";
        return  getSimpleJdbcTemplate().update(sql, internalID);
    }
    
   
}
