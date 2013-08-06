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

import eu.dasish.annotation.backend.dao.SourceDao;
import eu.dasish.annotation.backend.identifiers.SourceIdentifier;
import eu.dasish.annotation.backend.identifiers.VersionIdentifier;
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfos;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
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
public class JdbcSourceDao extends JdbcResourceDao implements SourceDao{
    
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
    
 
    
    public List<Number> retrieveSourceIDs(Number annotationID){
        return null;
    }
    
    
    public Source getSource(Number inernalID) {
        return null;
    }
    
    
    public int deleteSource(Number internalID){
        return -1;
    }
    
    
    public Source addSource(Source freshSource){
        return null;
    }
    
    
    public int purge(Number internalId){
        return -1;
    }
    
    
    public List<Number> sourceIDs(){
        return null;
    }
    
    
    public int purgeAll(){
        return -1;
    }
    
    ////////////////////////////////////////////////////////////////
    
    public List<SourceInfo> getSourceInfos(Number annotationID){
       String sourceIDs = makeListOfValues(getSourceInternalIdentifiers(annotationID)); 
       String sql = "SELECT "+external_id+","+ link +"," + version+"FROM "+sourcesTableName+" WHERE "+source_id  +" IN "+sourceIDs;
       List<SourceInfo> result= getSimpleJdbcTemplate().query(sql, SourceInfoRowMapper);       
       return result;
    }
    
      private final RowMapper<SourceInfo> SourceInfoRowMapper = new RowMapper<SourceInfo>() {        
        @Override
        public SourceInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
          return constructSourceInfo(new SourceIdentifier(rs.getString(external_id)), rs.getString(link), rs.getString(version));
        }
    }; 
    
    
    //////////////////////////////////////////
    @Override
    public NewOrExistingSourceInfos contructNewOrExistingSourceInfo(List<SourceInfo> sourceInfoList){
           List<NewOrExistingSourceInfo> noeSourceInfoList = new ArrayList<NewOrExistingSourceInfo>();
           for (SourceInfo sourceInfo: sourceInfoList) {
                NewOrExistingSourceInfo noeSourceInfo = new NewOrExistingSourceInfo();
                noeSourceInfo.setSource(sourceInfo);
                noeSourceInfoList.add(noeSourceInfo);
           }   
           NewOrExistingSourceInfos result = new  NewOrExistingSourceInfos();
           result.getTarget().addAll(noeSourceInfoList);
           return result;
     }
    
     
      
    //////// HELPERS //////////////////////
    
    
    private List<Number> getSourceInternalIdentifiers(Number annotationID){
        String sql = "SELECT "+target_source_id+" FROM "+annotationsSourcesTableName+" WHERE "+annotationAnnotation_id  +"= ?"; 
        List<Number> result = getSimpleJdbcTemplate().query(sql, SourceIDRowMapper, annotationID);
        return result;
    }
    
    private final RowMapper<Number> SourceIDRowMapper = new RowMapper<Number>() {        
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
           return rs.getInt(target_source_id);
        }
    }; 
    
    
    ////////////////////////////////////////////////////////
    private SourceInfo constructSourceInfo(SourceIdentifier sourceIdentifier, String link, String version){
        SourceInfo sourceInfo = new SourceInfo();
        sourceInfo.setRef(sourceIdentifier.toString());
        sourceInfo.setLink(link);
        sourceInfo.setVersion(version);
        return sourceInfo;
    }
}
