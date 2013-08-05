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
import eu.dasish.annotation.backend.identifiers.VersionIdentifier;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.Version;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author olhsha
 */
public class JdbcVersionDao extends JdbcResourceDao implements VersionDao{
    
      public JdbcVersionDao(DataSource dataSource) {
        setDataSource(dataSource);
    }
      
       //////////////////////////////////////////////////////////////////////////////////////////////////////    
      @Override
      public VersionIdentifier getExternalId(Number internalID){
       if (internalID == null) {
            return null;
        }
       String sql = "SELECT "+external_id+" FROM "+versionTableName+" WHERE "+version_id  +"= ?";
       List<String> sqlResult= getSimpleJdbcTemplate().query(sql, externalIDRowMapper, internalID); 
       
       if (sqlResult == null) {
           return null;
       }
       if (sqlResult.isEmpty()) {
           return null;
       } 
        
        VersionIdentifier result  = new VersionIdentifier(sqlResult.get(0));
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
     public Number getExternalId(VersionIdentifier externalID){
       if (externalID == null) {
            return null;
        }
       String sql = "SELECT "+version_id+" FROM "+versionTableName+" WHERE "+external_id  +"= ?";
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
            return(rs.getInt(version_id));
        }
     }; 
     
     
      ///////////////////////////////////////////////////////////////
     @Override
     public Version getVersion(Number internalID){
         
       String sql = "SELECT "+versionStar+" FROM "+versionTableName+" WHERE "+version_id  +"= ?";
       List<Version> result= getSimpleJdbcTemplate().query(sql, versionRowMapper, internalID); 
       
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
     
}
