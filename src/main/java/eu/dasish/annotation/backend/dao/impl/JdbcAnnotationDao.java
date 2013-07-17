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

import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.Annotations;
import eu.dasish.annotation.schema.ResourceREF;
import eu.dasish.annotation.schema.Sources;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

/**
 * Created on : Jun 27, 2013, 10:30:52 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */




public class JdbcAnnotationDao extends JdbcResourceDao implements AnnotationDao {

    public JdbcAnnotationDao(DataSource dataSource) {
        setDataSource(dataSource);
    }
    
    
   
    ////////////////////////////////////////////////////////////////////////////

   /**
    * 
    * @param annotationIDs is a list of internal annotation identifiers 
    * @return the list of the corresponding annotation-infos for the annotation identifiers from the list; 
    * if the input list is null return null
    * if the input list is empty (zero elements) returns an empty list
    */
    
    @Override
    public List<AnnotationInfo> getAnnotationInfos(List<Number> annotationIDs) {
        
        if (annotationIDs == null) {
            return null;
        }
        
        if (annotationIDs.isEmpty()) {
            return (new ArrayList<AnnotationInfo>());
        }
                
        String values = makeListOfValues(annotationIDs);
        String sql = "SELECT annotation.* FROM annotation WHERE annotation.annotation_id  IN "+values;
        return getSimpleJdbcTemplate().query(sql, annotationInfoRowMapper); 
    }
    
    private final RowMapper<AnnotationInfo> annotationInfoRowMapper = new RowMapper<AnnotationInfo>() {        
        @Override
        public AnnotationInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
           AnnotationInfo annotationInfo = new AnnotationInfo();
           annotationInfo.setOwner(getResourceREF(Integer.toString(rs.getInt("owner_id"))));
           annotationInfo.setHeadline(rs.getString("headline")); 
           annotationInfo.setTargetSources(getSources(rs.getString("body_xml")));
           return annotationInfo;
        }
    };
    
    
   
    /////////////////////////////////////////////////
    
    /**
     * 
     * @param annotationIDs
     * @return list of annotation references corresponding to the annotation-ids from the input list
     * if the input list is null or empty (zero elements) returns an empty list
     */
    
    @Override
    public List<ResourceREF> getAnnotationREFs(List<Number> annotationIDs) {
        
        if (annotationIDs == null) {
            return null;
        }
        
        if (annotationIDs.isEmpty()) {
            return (new ArrayList<ResourceREF>());
        }
        
        String values = makeListOfValues(annotationIDs);
        String sql = "SELECT annotation.annotation_id FROM annotation WHERE annotation.annotation_id  IN "+values;
        return getSimpleJdbcTemplate().query(sql, annotationREFRowMapper); 
    }
    
    private final RowMapper<ResourceREF> annotationREFRowMapper = new RowMapper<ResourceREF>() {        
        @Override
        public ResourceREF mapRow(ResultSet rs, int rowNumber) throws SQLException {
           ResourceREF annotationREF = new ResourceREF();
           annotationREF.setRef(Integer.toString(rs.getInt("annotation_id")));
           return annotationREF;
        }
    };
    
   
     
    
    
    
    /////////////////////////////////////////////////
    private ResourceREF getResourceREF(String resourceID){
       ResourceREF result = new ResourceREF();
       result.setRef(resourceID); 
       return result;
    }
    
    //TODO implement when xml-body stucture is discussed!
    //BTW do we have to get source REF, not the whole sources here??
    private Sources getSources(String someXml) {
        Sources result = new Sources();
        return result;
    }
    
    private <T> String makeListOfValues(List<T> vals) {
        
        if (vals == null) {
            return "()";
        }
        
        if (vals.isEmpty()) {            
            return "()";
        }
        
        String result = "(";
        int length = vals.size();
        for (int i=0; i<length-1; i++){
            result = result + vals.get(i).toString() +", ";
        }
        result = result +vals.get(length-1).toString()+")";
        return result;
    }
   
    
}
           
