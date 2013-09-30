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
    
   @Override
    public void setServiceURI(String serviceURI){
        _serviceURI = serviceURI;
    }

    //////////////////////// GETTERS ///////////////////////////////////
    @Override
    public Source getSource(Number internalID) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(sourceStar).append(" FROM ").append(sourceTableName).append(" WHERE ").append(source_id).append("= ? LIMIT 1");
        List<Source> result = getSimpleJdbcTemplate().query(sql.toString(), sourceRowMapper, internalID);
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
    
    ///////////////////////////////////////////////////////
    @Override
    public Integer getSourceSiblingClass(Number sourceID) {
         if (sourceID == null) {
            return null;
        }
         
        String sql = "SELECT " + sibling_source_class + " FROM " + sourceTableName + " WHERE " + source_id + " = ?";
        List<Integer> classIDs =  getSimpleJdbcTemplate().query(sql, sourceClassRowMapper, sourceID);                
        if (classIDs == null) {
            return null;
        } 
        if (classIDs.isEmpty()) {
            return null;
        }  
        return classIDs.get(0);
     }
    
    /////////////////////////////////////////
    @Override
    public List<Number> getSiblingSources(Number sourceID) {
        Integer classID = getSourceSiblingClass(sourceID);
        if (classID == null) {
            return null;
        }
        String sqlSources = "SELECT " + source_id + " FROM " + sourceTableName + " WHERE " + sibling_source_class + " = ?"; 
        return getSimpleJdbcTemplate().query(sqlSources, sourceIDRowMapper, classID); 
    }

    private final RowMapper<Integer> sourceClassRowMapper = new RowMapper<Integer>() {
        @Override
        public Integer mapRow(ResultSet rs, int rowNumber) throws SQLException {
             return Integer.valueOf(rs.getInt(sibling_source_class));
        }
    };
    
      /////////////////////////////////////////
    @Override
    public List<Number> getCachedRepresentations(Number sourceID) {
       
        String sql = "SELECT " + cached_representation_id + " FROM " + sourcesCachedRepresentationsTableName + " WHERE " + source_id + " = ?";
        return getSimpleJdbcTemplate().query(sql, cachedIDRowMapper, sourceID); 
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
        
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(external_id).append(",").append(link_uri).append(",").append(version).
                append(" FROM ").append(sourceTableName).append(" WHERE ").append(source_id).append(" IN ").append(sourceIDs);
        return getSimpleJdbcTemplate().query(sql.toString(), SourceInfoRowMapper);
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
        StringBuilder sqlAnnotations = new StringBuilder("SELECT ");
        sqlAnnotations.append(annotation_id).append(" FROM ").append(annotationsSourcesTableName).append(" WHERE ").append(source_id).append(" = ? LIMIT 1");
        List<Number> resultAnnotations = getSimpleJdbcTemplate().query(sqlAnnotations.toString(), annotationIDRowMapper, sourceID);
        if (resultAnnotations == null) {
            return false;
        }
        return (resultAnnotations.size() > 0) ;
    }
    
  
    

    ///////////////////////// ADDERS /////////////////////////////////
    @Override
    public Number addSource(Source source) throws SQLException {        
        UUID externalID = UUID.randomUUID();      
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalID.toString());
        params.put("linkUri", source.getLink());
        params.put("version", source.getVersion());
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(sourceTableName).append("(").append(external_id).append(",").append(link_uri).append(",").append(version).append(" ) VALUES (:externalId, :linkUri,  :version)");
        final int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), params);  
        return (affectedRows>0 ? getInternalID(UUID.fromString(externalID.toString())) : null);
    }
    
    
    ///////////////////////////////////////////////////////////////////
    @Override
    public int addSourceCachedRepresentation(Number sourceID, Number cachedID) throws SQLException{
        Map<String, Object> paramsJoint = new HashMap<String, Object>();
        paramsJoint.put("sourceId", sourceID);
        paramsJoint.put("cachedId", cachedID);
        StringBuilder sqlJoint = new StringBuilder("INSERT INTO ").append(sourcesCachedRepresentationsTableName).append("(").append(source_id).append(",").append(cached_representation_id).append(" ) VALUES (:sourceId, :cachedId)");
        return getSimpleJdbcTemplate().update(sqlJoint.toString(), paramsJoint);
    }
    
     ///////////////////////////////////////////////////////////////////
    @Override
    public int updateSiblingClass(Number sourceID, int classID) throws SQLException{
        if (sourceID == null) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(sourceTableName).append(" SET ").append(sibling_source_class).append("= '").append(classID).append("' WHERE ").append(source_id).append("= ?");
        return getSimpleJdbcTemplate().update(sql.toString(), sourceID);
    }
   
////////////////////// DELETERS ////////////////////////
    @Override
    public int deleteSource(Number internalID) {
        if (sourceIsInUse(internalID)){
            return 0;
        }
        StringBuilder sqlSourcesVersions = new StringBuilder("DELETE FROM ");
        sqlSourcesVersions.append(sourceTableName).append(" WHERE ").append(source_id).append(" = ? ");
        return getSimpleJdbcTemplate().update(sqlSourcesVersions.toString(), internalID);

    }

    
    ///////////////////////////////////////////////////////////////////
    @Override
    public int deleteSourceCachedRepresentation(Number sourceID, Number cachedID) throws SQLException {
        if (sourceID == null || cachedID == null) {
            return 0;
        }
        Map<String, Object> paramsJoint = new HashMap<String, Object>();
        paramsJoint.put("sourceId", sourceID);
        paramsJoint.put("cachedId", cachedID);
        StringBuilder sqlSourcesVersions = new StringBuilder("DELETE FROM ");
        sqlSourcesVersions.append(sourcesCachedRepresentationsTableName).append(" WHERE ").append(source_id).append(" = :sourceId").
                append(" AND ").append(cached_representation_id).append(" = :cachedId");
        return getSimpleJdbcTemplate().update(sqlSourcesVersions.toString(), sourceID);

    }
 
   
  /////////// HELPERS  ////////////////
   

    private SourceInfo constructSourceInfo(String externalID, String link, String version) {
        SourceInfo sourceInfo = new SourceInfo();
        sourceInfo.setRef(externalIDtoURI(externalID));
        sourceInfo.setLink(link);
        sourceInfo.setVersion(version);
        return sourceInfo;
    }

    private Source constructSource(String externalID, String link, String version, XMLGregorianCalendar xmlTimeStamp) {
        Source source = new Source();
        source.setURI(externalIDtoURI(externalID));
        source.setTimeStamp(xmlTimeStamp);
        source.setLink(link);
        source.setVersion(version);

        return source;
    }
}
