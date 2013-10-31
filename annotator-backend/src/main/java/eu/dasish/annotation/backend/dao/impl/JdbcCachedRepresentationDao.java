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
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.SqlLobValue;

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

    @Override
    public void setServiceURI(String serviceURI) {
        _serviceURI = serviceURI;
    }

    /////////////////////////// GETTERS  ////////////////////////////////////////
    @Override
    public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID) {

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(cachedRepresentationStar).append(" FROM ").append(cachedRepresentationTableName).append(" WHERE ").append(cached_representation_id).append("= ? LIMIT 1");
        List<CachedRepresentationInfo> result = getSimpleJdbcTemplate().query(sql.toString(), cachedRepresentationRowMapper, internalID);

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
            result.setURI(externalIDtoURI(rs.getString(external_id)));
            result.setTool(rs.getString(tool));
            result.setType(rs.getString(type_));
            return result;
        }
    };

    /////////////////////////// GETTERS  ////////////////////////////////////////
    @Override
    public InputStream getCachedRepresentationBlob(Number internalID) {

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(file_).append(" FROM ").append(cachedRepresentationTableName).append(" WHERE ").append(cached_representation_id).append("= ? LIMIT 1");
        List<InputStream> result = getSimpleJdbcTemplate().query(sql.toString(), cachedRepresentationBlobRowMapper, internalID);

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }
    private final RowMapper<InputStream> cachedRepresentationBlobRowMapper = new RowMapper<InputStream>() {
        @Override
        public InputStream mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getBinaryStream(file_);
        }
    };

    //////////////////////////////////////
    private boolean cachedIsInUse(Number cachedID) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(target_id).append(" FROM ").append(TargetsCachedRepresentationsTableName).append(" WHERE ").append(cached_representation_id).append("= ? LIMIT 1");
        List<Number> result = getSimpleJdbcTemplate().query(sql.toString(), TargetIDRowMapper, cachedID);
        if (result != null) {
            return (!result.isEmpty());
        } else {
            return false;
        }
    }

    //////////////////////// ADDERS ///////////////////////////////
    @Override
    public Number addCachedRepresentation(CachedRepresentationInfo cachedInfo, InputStream streamCached) {
        try {
            UUID externalIdentifier = UUID.randomUUID();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("externalId", externalIdentifier.toString());
            params.put("mime_type", cachedInfo.getMimeType());
            params.put("tool", cachedInfo.getTool());
            params.put("type", cachedInfo.getType());
            params.put("blob", getBytesForBlob(streamCached));
            StringBuilder sql = new StringBuilder("INSERT INTO ");
            sql.append(cachedRepresentationTableName).append("(").append(external_id).append(",").append(mime_type).append(",").append(tool).append(",").append(type_).append(",").append(file_).append(" ) VALUES (:externalId, :mime_type,  :tool, :type, :blob)");
            final int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), params);
            return (affectedRows > 0 ? getInternalID(externalIdentifier) : null);

        } catch (IOException ioe) {
            return null;
        }

    }

    /////////////////////// DELETERS  //////////////////////////////////////////////
    @Override
    public int deleteCachedRepresentation(Number internalID) {
        if (cachedIsInUse(internalID)) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(cachedRepresentationTableName).append(" WHERE ").append(cached_representation_id).append(" = ?");
        return getSimpleJdbcTemplate().update(sql.toString(), internalID);
    }

    /////////// HELPERS ///////////////
    private byte[] getBytesForBlob(InputStream iStream) throws IOException {
        int max_buffer_size = 1024 * 1024 * 3;
        byte[] buffer = new byte[max_buffer_size];
        int bytesRead = iStream.read(buffer);
        byte[] result = new byte[bytesRead];
        for (int i = 0; i < bytesRead; i++) {
            result[i] = buffer[i];
        }
        return result;
    }
}
