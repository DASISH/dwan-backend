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
import eu.dasish.annotation.backend.dao.SourceDao;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
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
public class JdbcSourceDao extends JdbcResourceDao implements SourceDao {

   
    public JdbcSourceDao(DataSource dataSource) {
        setDataSource(dataSource);
        internalIdName = source_id;
        resourceTableName = sourceTableName;
    }


    //////////////////////// GETTERS ///////////////////////////////////
    @Override
    public Source getSource(Number internalID) {
        String sql = "SELECT " + sourceStar + "FROM " + sourceTableName + " WHERE " + source_id + " = ?";
        List<Source> result = getSimpleJdbcTemplate().query(sql, sourceRowMapper, internalID);
        return (!result.isEmpty() ? result.get(0) : null);
    }
    private final RowMapper<Source> sourceRowMapper = new RowMapper<Source>() {
        @Override
        public Source mapRow(ResultSet rs, int rowNumber) throws SQLException {
            try {
                XMLGregorianCalendar xmlDate = Helpers.setXMLGregorianCalendar(rs.getTimestamp(time_stamp));
                Source result = 
                        constructSource(rs.getString(external_id), rs.getString(link_uri), rs.getString(version), xmlDate);
                return result;
            } catch (DatatypeConfigurationException e) {
                // TODO: which logger are we going to use?
                System.out.println("Cannot construct time stamp: probably worng date/time format");
                return null;
            }
        }
    };
    
    /////////////////////////////////////////
    @Override
    public List<Number> retrieveVersionList(Number sourceID) {
        String sql = "SELECT " + version_id + " FROM " + sourcesVersionsTableName + " WHERE " + source_id + " = ?";
        return getSimpleJdbcTemplate().query(sql, versionIDRowMapper, sourceID);
    }

    
     ///////////////////////////////////////////////////////////////////
    @Override
    public List<SourceInfo> getSourceInfos(List<Number> sources) {
        if (sources == null) {
            return null;
        }
        if (sources.isEmpty()) {
            return new ArrayList<SourceInfo>();
        }

        String sourceIDs = makeListOfValues(sources);
        String sql = "SELECT " + external_id + "," + link_uri + "," + version + " FROM " + sourceTableName + " WHERE " + source_id + " IN " + sourceIDs;
        return getSimpleJdbcTemplate().query(sql, SourceInfoRowMapper);
    }
    private final RowMapper<SourceInfo> SourceInfoRowMapper = new RowMapper<SourceInfo>() {
        @Override
        public SourceInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return constructSourceInfo(rs.getString(external_id), rs.getString(link_uri), rs.getString(version));
        }
    };

 
    /////////////////////////////////////////////////////
    @Override
    public List<Number> getSourcesForLink(String link) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(source_id).append(" FROM ").append(sourceTableName).append(" WHERE ").append(link_uri).append(" LIKE '%").append(link).append("%'");
        return getSimpleJdbcTemplate().query(sql.toString(), internalIDRowMapper);
    }

    
    /////////////////////////////////////////////////
    @Override
    public boolean sourceIsInUse(Number sourceID) {
        String sqlAnnotations = "SELECT " + annotation_id + " FROM " + annotationsSourcesTableName + " WHERE " + source_id + "= ? LIMIT 1";
        List<Number> resultAnnotations = getSimpleJdbcTemplate().query(sqlAnnotations, annotationIDRowMapper, sourceID);
        if (resultAnnotations.size() > 0) {
            return true;
        }
        String sqlVersions = "SELECT " + version_id + " FROM " + sourcesVersionsTableName + " WHERE " + source_id + "= ? LIMIT 1";
        List<Number> resultVersions = getSimpleJdbcTemplate().query(sqlVersions, versionIDRowMapper, sourceID);
        return (resultVersions.size() > 0);
    }
    
  
    

    ///////////////////////// ADDERS /////////////////////////////////
    @Override
    public Number addSource(Source source) throws SQLException {        
        UUID externalID = UUID.randomUUID();      
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalID.toString());
        params.put("linkUri", source.getLink());
        params.put("version", source.getVersion());
        String sql = "INSERT INTO " + sourceTableName + "(" + external_id + "," + link_uri + "," + version + " ) VALUES (:externalId, :linkUri,  :version)";
        final int affectedRows = getSimpleJdbcTemplate().update(sql, params);  
        return (affectedRows>0 ? getInternalID(UUID.fromString(externalID.toString())) : null);
    }
    
    
    ///////////////////////////////////////////////////////////////////
    @Override
    public int addSourceVersion(Number sourceID, Number versionID) throws SQLException {
        Map<String, Object> paramsJoint = new HashMap<String, Object>();
        paramsJoint.put("sourceId", sourceID);
        paramsJoint.put("versionId", versionID);
        String sqlJoint = "INSERT INTO " + sourcesVersionsTableName + "(" + source_id + "," + version_id + " ) VALUES (:sourceId, :versionId)";
        return getSimpleJdbcTemplate().update(sqlJoint, paramsJoint);
    }
   
////////////////////// DELETERS ////////////////////////
    @Override
    public int deleteSource(Number internalID) {
        if (sourceIsInUse(internalID)){
            return 0;
        }
        String sqlSourcesVersions = "DELETE FROM " + sourceTableName + " WHERE " + source_id + " = ? ";
        return getSimpleJdbcTemplate().update(sqlSourcesVersions, internalID);

    }

    
    ///////////////////////////////////////////////////////////////////
    @Override
    public int deleteAllSourceVersion(Number internalID) {
        String sqlSourcesVersions = "DELETE FROM " + sourcesVersionsTableName + " WHERE " + source_id + " = ?";
        return getSimpleJdbcTemplate().update(sqlSourcesVersions, internalID);

    }
 
   
  /////////// HELPERS  ////////////////
   

    private SourceInfo constructSourceInfo(String externalID, String link, String version) {
        SourceInfo sourceInfo = new SourceInfo();
        sourceInfo.setRef(externalIDtoURI(_serviceURI,externalID));
        sourceInfo.setLink(link);
        sourceInfo.setVersion(version);
        return sourceInfo;
    }

    private Source constructSource(String externalID, String link, String version, XMLGregorianCalendar xmlTimeStamp) {
        Source source = new Source();
        source.setURI(externalIDtoURI(_serviceURI, externalID));
        source.setTimeSatmp(xmlTimeStamp);
        source.setLink(link);
        source.setVersion(version);

        return source;
    }
}
