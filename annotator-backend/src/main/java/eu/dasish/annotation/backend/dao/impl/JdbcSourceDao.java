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
import eu.dasish.annotation.backend.identifiers.SourceIdentifier;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public SourceIdentifier getExternalID(Number internalID) {
        return new SourceIdentifier(super.getExternalIdentifier(internalID));
    }

    ///////////////////////////////////////////////////////////////////////////////
    @Override
    public Source getSource(Number internalID) {
        String sql = "SELECT " + sourceStar + "FROM " + sourceTableName + " WHERE " + source_id + " = ?";
        List<Source> result = getSimpleJdbcTemplate().query(sql, sourceRowMapper, internalID);
        return result.get(0);
    }
    private final RowMapper<Source> sourceRowMapper = new RowMapper<Source>() {
        @Override
        public Source mapRow(ResultSet rs, int rowNumber) throws SQLException {
            try {
                XMLGregorianCalendar xmlDate = Helpers.setXMLGregorianCalendar(rs.getTimestamp(time_stamp));
                Source result = 
                        constructSource(new SourceIdentifier(rs.getString(external_id)), rs.getString(link_uri), rs.getString(version), xmlDate);
                return result;
            } catch (DatatypeConfigurationException e) {
                // TODO: which logger are we going to use?
                System.out.println("Cannot construct time stamp: probably worng date/time format");
                return null;
            }
        }
    };

    ///////////////////////////////////////////////////////////////////
    @Override
    public int deleteSource(Number internalID) {
        if (sourceIsInUse(internalID)){
            return 0;
        }
        String sqlSourcesVersions = "DELETE FROM " + sourceTableName + " WHERE " + source_id + " = ? ";
        int result = getSimpleJdbcTemplate().update(sqlSourcesVersions, internalID);
        return result;

    }

    
    ///////////////////////////////////////////////////////////////////
    @Override
    public int deleteAllSourceVersion(Number internalID) {
        String sqlSourcesVersions = "DELETE FROM " + sourcesVersionsTableName + " WHERE " + source_id + " = ?";
        int result = getSimpleJdbcTemplate().update(sqlSourcesVersions, internalID);
        return result;

    }

    ///////////////////////////////////////////////////////////////////
    @Override
    public Number addSource(Source source) throws SQLException {        
        String externalID = source.getURI();        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalID);
        params.put("linkUri", source.getLink());
        params.put("version", source.getVersion());
        String sql = "INSERT INTO " + sourceTableName + "(" + external_id + "," + link_uri + "," + version + " ) VALUES (:externalId, :linkUri,  :version)";
        final int affectedRows = getSimpleJdbcTemplate().update(sql, params);        
        Number internalID = getInternalID(new SourceIdentifier(externalID));
        return internalID;
    }
    
    ///////////////////////////////
    
    
    ///////////////////////////////////////////////////////////////////
    @Override
    public int addSourceVersion(Number sourceID, Number versionID) throws SQLException {
        Map<String, Object> paramsJoint = new HashMap<String, Object>();
        paramsJoint.put("sourceId", sourceID);
        paramsJoint.put("versionId", versionID);
        String sqlJoint = "INSERT INTO " + sourcesVersionsTableName + "(" + source_id + "," + version_id + " ) VALUES (:sourceId, :versionId)";
        return getSimpleJdbcTemplate().update(sqlJoint, paramsJoint);
    }
   

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
        List<SourceInfo> result = getSimpleJdbcTemplate().query(sql, SourceInfoRowMapper);
        return result;
    }
    private final RowMapper<SourceInfo> SourceInfoRowMapper = new RowMapper<SourceInfo>() {
        @Override
        public SourceInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return constructSourceInfo(new SourceIdentifier(rs.getString(external_id)), rs.getString(link_uri), rs.getString(version));
        }
    };

 
    /////////////////////////////////////////////////////
    @Override
    public List<Number> getSourcesForLink(String link) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(source_id).append(" FROM ").append(sourceTableName).append(" WHERE ").append(link_uri).append(" LIKE '%").append(link).append("%'");
        List<Number> result = getSimpleJdbcTemplate().query(sql.toString(), internalIDRowMapper);
        return result;
    }

    

    @Override
    public boolean sourceIsInUse(Number sourceID) {
        String sql = "SELECT " + annotation_id + " FROM " + annotationsSourcesTableName + " WHERE " + source_id + "= ? LIMIT 1";
        List<Number> result = getSimpleJdbcTemplate().query(sql, annotationIDRowMapper, sourceID);
        if (result.size() > 0) {
            return true;
        }
        return false;
    }
    
      private final RowMapper<Number> annotationIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getInt(annotation_id);
        }
    };
    
  /////////// HELPERS  ////////////////
   

    private SourceInfo constructSourceInfo(SourceIdentifier sourceIdentifier, String link, String version) {
        SourceInfo sourceInfo = new SourceInfo();
        sourceInfo.setRef(sourceIdentifier.toString());
        sourceInfo.setLink(link);
        sourceInfo.setVersion(version);
        return sourceInfo;
    }

    private Source constructSource(SourceIdentifier sourceIdentifier, String link, String version, XMLGregorianCalendar xmlTimeStamp) {
        Source source = new Source();
        source.setURI(sourceIdentifier.toString());
        source.setTimeSatmp(xmlTimeStamp);
        source.setLink(link);
        source.setVersion(version);

        return source;
    }
}
