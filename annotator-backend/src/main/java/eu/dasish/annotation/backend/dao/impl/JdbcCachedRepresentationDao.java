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

import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.dao.CachedRepresentationDao;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author olhsha
 */
public class JdbcCachedRepresentationDao extends JdbcResourceDao implements CachedRepresentationDao {

    private final Logger loggerCachedDao = LoggerFactory.getLogger(JdbcCachedRepresentationDao.class);

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
        List<CachedRepresentationInfo> result = this.loggedQuery(sql.toString(), cachedRepresentationRowMapper, internalID);
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
        List<InputStream> result = this.loggedQuery(sql.toString(), cachedRepresentationBlobRowMapper, internalID);
        return result.get(0);
    }
    private final RowMapper<InputStream> cachedRepresentationBlobRowMapper = new RowMapper<InputStream>() {
        @Override
        public InputStream mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getBinaryStream(file_);
        }
    };

    /////////////////////////////////////////
    @Override
    public List<Number> getCachedRepresentationsForTarget(Number targetID) {

        String sql = "SELECT " + cached_representation_id + " FROM " + targetsCachedRepresentationsTableName + " WHERE " + target_id + " = ?";
        return this.loggedQuery(sql, internalIDRowMapper, targetID);
    }
    
    

//    Map<String, Object> params = new HashMap<String, Object>();
//
//        params.put("annotationID", annotationID);
//        params.put("principalID", principalID);
//        params.put("accessString", access.value());
//        StringBuilder sql = new StringBuilder("UPDATE ");
//        sql.append(permissionsTableName).append(" SET ").
//                append(this.access).append("= :accessString").
//                append(" WHERE ").append(annotation_id).append("= :annotationID").
//                append(" AND ").append(principal_id).append("= :principalID");
//        return this.loggedUpdate(sql.toString(), params);
    
    //// UPDATERS ///
    
    @Override
    public int updateCachedRepresentationMetadata(Number internalID, CachedRepresentationInfo cachedInfo){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("internalID", internalID);
        params.put("mimetype", cachedInfo.getMimeType());
        params.put("tool", cachedInfo.getTool());
        params.put("type", cachedInfo.getType());
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(cachedRepresentationTableName).append(" SET ").
                append(mime_type).append("= :mimetype").append(",").
                append(type_).append("= :type").append(",").
                append(tool).append("= :tool").
                append(" WHERE ").append(cached_representation_id).append("= :internalID");
        return this.loggedUpdate(sql.toString(), params);
    }

    @Override
    public int updateCachedRepresentationBlob(Number internalID, InputStream cachedBlob) throws IOException{
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("internalID", internalID);
        params.put("blob", IOUtils.toByteArray(cachedBlob));
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(cachedRepresentationTableName).append(" SET ").
                append(file_).append("= :blob").
                append(" WHERE ").append(cached_representation_id).append("= :internalID");
        return this.loggedUpdate(sql.toString(), params);
    }
    

    
    
    
    //////////////////////// ADDERS ///////////////////////////////
    @Override
    public Number addCachedRepresentation(CachedRepresentationInfo cachedInfo, InputStream streamCached) throws NotInDataBaseException, IOException {

        UUID externalIdentifier = UUID.randomUUID();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalIdentifier.toString());
        params.put("mime_type", cachedInfo.getMimeType());
        params.put("tool", cachedInfo.getTool());
        params.put("type", cachedInfo.getType());
        params.put("blob", IOUtils.toByteArray(streamCached));
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(cachedRepresentationTableName).append("(").append(external_id).append(",").append(mime_type).append(",").append(tool).append(",").append(type_).append(",").append(file_).append(" ) VALUES (:externalId, :mime_type,  :tool, :type, :blob)");
        final int affectedRows = this.loggedUpdate(sql.toString(), params);
        return getInternalID(externalIdentifier);
    }

    /////////////////////// DELETERS  //////////////////////////////////////////////
    @Override
    public int deleteCachedRepresentation(Number internalID) {

        if (internalID == null) {
            loggerCachedDao.debug("internalID: " + nullArgument);
            return 0;
        }

        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(cachedRepresentationTableName).append(" WHERE ").append(cached_representation_id).append(" = ?");
        return this.loggedUpdate(sql.toString(), internalID);
    }
}
