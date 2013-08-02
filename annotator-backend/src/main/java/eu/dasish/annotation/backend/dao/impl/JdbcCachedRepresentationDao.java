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
import eu.dasish.annotation.backend.identifiers.CachedRepresentationIdentifier;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.CachedRepresentations;
import eu.dasish.annotation.schema.ResourceREF;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author olhsha
 */
public class JdbcCachedRepresentationDao extends JdbcResourceDao implements CachedRepresentationDao{
    
     public JdbcCachedRepresentationDao(DataSource dataSource) {
        setDataSource(dataSource);
    }
     
   //////////////////////////////////////////////////////////////////////////////////////////////////////
     @Override
     public CachedRepresentationIdentifier getExternalId(Number internalID){
       if (internalID == null) {
            return null;
        }
       String sql = "SELECT "+external_id+" FROM "+cachedRepresentationTableName+" WHERE "+cached_representation_id  +"= ?";
       List<String> sqlResult= getSimpleJdbcTemplate().query(sql, externalIDRowMapper, internalID); 
       
       if (sqlResult == null) {
           return null;
       }
       if (sqlResult.isEmpty()) {
           return null;
       } 
        
        CachedRepresentationIdentifier result  = new CachedRepresentationIdentifier(sqlResult.get(0));
        return result;
    }  
     
     private final RowMapper<String> externalIDRowMapper = new RowMapper<String>() {        
        @Override
        public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return(rs.getString(external_id));
        }
     };  
     
      //////////////////////////////////////////////////////////////////////////////////////////////////////
     @Override
     public Number getExternalId(CachedRepresentationIdentifier externalID){
       if (externalID == null) {
            return null;
        }
       String sql = "SELECT "+cached_representation_id+" FROM "+cachedRepresentationTableName+" WHERE "+external_id  +"= ?";
       List<Number> sqlResult= getSimpleJdbcTemplate().query(sql, internalIDRowMapper, externalID); 
       
       if (sqlResult == null) {
           return null;
       }
       if (sqlResult.isEmpty()) {
           return null;
       } 
        
        Number result  = sqlResult.get(0);
        return result;
    }  
     
     private final RowMapper<Number> internalIDRowMapper = new RowMapper<Number>() {        
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return(rs.getInt(cached_representation_id));
        }
     }; 
     
     ///////////////////////////////////////////////////////////////
     @Override
     public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID){
         
       String sql = "SELECT "+cachedRepresentationStar+" FROM "+cachedRepresentationTableName+" WHERE "+cached_representation_id  +"= ?";
       List<CachedRepresentationInfo> result= getSimpleJdbcTemplate().query(sql, cachedRepresentationRowMapper, internalID); 
       
       if (result == null) {
           return null;
       }
       if (result.isEmpty()) {
           return null;
       } 
       return result.get(0);
     }
     
     private final RowMapper<CachedRepresentationInfo> cachedRepresentationRowMapper = new RowMapper<CachedRepresentationInfo>() {        
        @Override
        public CachedRepresentationInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            CachedRepresentationInfo result = new CachedRepresentationInfo();
            //external_id, mime_type, tool, type_, where_is_the_file
            result.setMimeType(rs.getString(mime_type));
            result.setRef(rs.getString(external_id));
            result.setTool(rs.getString(tool));
            result.setType(rs.getString(type_));
            // TODO add where is the file when the schem is updated!!!!s
            return result;
        }
     }; 
     
     
     ////////////////////////////////////////////////////////////////////////////
      @Override
      public List<Number> retrieveCachedRepresentationList(Number versionID){
       String sql = "SELECT "+cached_representation_id+" FROM "+versionsCachedRepresentationsTableName+" WHERE "+version_id  +"= ?";
       List<Number> result= getSimpleJdbcTemplate().query(sql, internalIDRowMapper, versionID); 
       
       if (result == null) {
           return null;
       }
       if (result.isEmpty()) {
           return null;
       } 
       return result;
      };
      
      
      /////////////////////////////////////////////////////
      
      @Override
      public CachedRepresentations retrieveCachedRepresentations(Number versionID){
       CachedRepresentations result = new CachedRepresentations();
       
       List<Number> cachedRepresenationIDs = retrieveCachedRepresentationList(versionID);
       List<ResourceREF> cachedRepresenationIdentifierList = new ArrayList<ResourceREF>();
       
       for (Number cachedRepresentationID :  cachedRepresenationIDs){
           ResourceREF resourceREF = new ResourceREF();
           resourceREF.setRef(getExternalId(cachedRepresentationID).toString());
           cachedRepresenationIdentifierList.add(resourceREF);
       }
       
       result.getCachedRepresentation().addAll(cachedRepresenationIdentifierList);
       return result;
      };
      
}
