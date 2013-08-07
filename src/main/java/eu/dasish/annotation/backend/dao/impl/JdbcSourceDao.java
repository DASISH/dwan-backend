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
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfos;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
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
    
 
    @Override
    public List<Number> retrieveSourceIDs(Number annotationID){
       String sql = "SELECT "+source_id+" FROM "+annotationsSourcesTableName+" WHERE "+annotation_id  +"= ?";
       List<Number> result= getSimpleJdbcTemplate().query(sql, annotationSourceRowMapper, annotationID); 
       
       if (result == null) {
           return null;
       }
       if (result.isEmpty()) {
           return null;
       } 
       return result;
     }
     
     private final RowMapper<Number> annotationSourceRowMapper = new RowMapper<Number>() {        
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Number result = rs.getInt(source_id);
            return result;
        }
     }; 
    
    public Source getSource(Number internalID) {
       String sql = "SELECT "+sourceStar+"FROM "+sourceTableName+" WHERE "+source_id  +" = ?";
       List<Source> result= getSimpleJdbcTemplate().query(sql, SourceRowMapper, internalID);       
       return result.get(0);
    }
    
      private final RowMapper<Source> SourceRowMapper = new RowMapper<Source>() {        
        @Override
        public Source mapRow(ResultSet rs, int rowNumber) throws SQLException {            
            try {
                Source result = constructSource(new SourceIdentifier(rs.getString(external_id)), rs.getString(link), rs.getString(version), rs.getString(time_stamp));
                return result;
            }
            catch (DatatypeConfigurationException e) {
                // TODO: what logger are we going to use
                System.out.println("Cannot construct time stam: probably worng date/time format");
                return null;
            }
        }
    }; 
    
    
    public int deleteSource(Number internalID){
        String sql = "DELETE FROM " + sourceTableName + " where " + source_id + " = ?";
        return (getSimpleJdbcTemplate().update(sql, internalID));
    }
    
    
    public Source addSource(Source freshSource){
        return null;
    }
    
    
    public List<Number> sourceIDs(){
        return null;
    }
    
    
    public int purgeAll(){
        return -1;
    }
    
    ////////////////////////////////////////////////////////////////
    
    public List<SourceInfo> getSourceInfos(Number annotationID){
       String sourceIDs = makeListOfValues(retrieveSourceIDs(annotationID)); 
       String sql = "SELECT "+external_id+","+ link +"," + version+"FROM "+sourceTableName+" WHERE "+source_id  +" IN "+sourceIDs;
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
    
  
    ////////////////////////////////////////////////////////
    private SourceInfo constructSourceInfo(SourceIdentifier sourceIdentifier, String link, String version){
        SourceInfo sourceInfo = new SourceInfo();
        sourceInfo.setRef(sourceIdentifier.toString());
        sourceInfo.setLink(link);
        sourceInfo.setVersion(version);
        return sourceInfo;
    }
    
    private Source constructSource(SourceIdentifier sourceIdentifier, String link, String version, String timeStamp) throws DatatypeConfigurationException{
        Source source = new Source();         
        XMLGregorianCalendar xmlTimeStamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(timeStamp);
        source.setURI(sourceIdentifier.toString());
        source.setTimeSatmp(xmlTimeStamp);
        source.setLink(link);
        source.setVersion(version);
        return source;
    }
}
