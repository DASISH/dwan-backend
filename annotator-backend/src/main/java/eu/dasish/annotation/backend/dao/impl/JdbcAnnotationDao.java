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
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.ResourceREF;
import eu.dasish.annotation.schema.Sources;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

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
        String sql = "SELECT "+annotationStar+" FROM "+annotationTableName +" WHERE "+annotationAnnotation_id+"  IN "+values;
        return getSimpleJdbcTemplate().query(sql, annotationInfoRowMapper); 
    }
    
    private final RowMapper<AnnotationInfo> annotationInfoRowMapper = new RowMapper<AnnotationInfo>() {        
        @Override
        public AnnotationInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
           AnnotationInfo annotationInfo = new AnnotationInfo();
           annotationInfo.setOwner(getResourceREF(Integer.toString(rs.getInt(owner_id))));
           annotationInfo.setHeadline(rs.getString(headline)); 
           annotationInfo.setTargetSources(getSources(rs.getString(body_xml)));
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
        String sql = "SELECT "+annotationAnnotation_id+" FROM "+annotationTableName+" WHERE "+annotationAnnotation_id+"  IN "+values;
        return getSimpleJdbcTemplate().query(sql, annotationREFRowMapper); 
    }
    
    private final RowMapper<ResourceREF> annotationREFRowMapper = new RowMapper<ResourceREF>() {        
        @Override
        public ResourceREF mapRow(ResultSet rs, int rowNumber) throws SQLException {
           ResourceREF annotationREF = new ResourceREF();
           annotationREF.setRef(Integer.toString(rs.getInt(annotation_id)));
           return annotationREF;
        }
    };
 
   @Override
    public Annotation getAnnotation(Number annotationID) throws SQLException{
        if (annotationID == null) {
            return null;
        }
       String sql = "SELECT "+annotationStar+" FROM "+annotationTableName+" WHERE "+annotationAnnotation_id  +"= ?";
       List<Annotation> result= getSimpleJdbcTemplate().query(sql, annotationRowMapper, annotationID); 
       
       if (result == null) {
           return null;
       }
       if (result.isEmpty()) {
           return null;
       } 
       
        if (result.size()>1) {
           throw new SQLException("There are "+result.size()+" annotations with "+ annotation_id + " "+annotationID);
       }
       return result.get(0);
   }
     
      private final RowMapper<Annotation> annotationRowMapper = new RowMapper<Annotation>() {        
        @Override
        public Annotation mapRow(ResultSet rs, int rowNumber) throws SQLException {
           Annotation result = new Annotation();
           result.setHeadline(rs.getString(headline));
           
           ResourceREF ownerREF = new ResourceREF();
           ownerREF.setRef(String.valueOf(rs.getInt(owner_id)));
           result.setOwner(ownerREF);
           
           /*TODO 
            * Add permissions also to the database
            * ResourceREF permissionsREF = new ResourceREF();
           permissionsREF.setRef(String.valueOf(rs.getInt("permissions")));
           result.setPermissions(permissionsREF);*/
           
           
           // TODO: add source, also to the database
           
           // TODO add external reference 
           
           result.setBody(convertToAnnotationBody(rs.getString(body_xml)));
           return result;
        }
    };
   
   // TODO: fill in the stub, when the annotation body is elaborated
   private AnnotationBody convertToAnnotationBody(String input){
     if (input == null) {
         return null;
     }  
     
     AnnotationBody result = new AnnotationBody();
     List<Object> element =result.getAny();
     element.add(input);
     return result;
   }  
      
      
   //////////////////////////////////////////////////
     @Override
    public Number getAnnotationID(AnnotationIdentifier externalID) throws SQLException{
        if (externalID == null) {
            return null;
        }
        
       String sql = "SELECT "+annotationAnnotation_id+" FROM "+annotationTableName+" WHERE "+annotationExternal_id+"  = ?";
       List<Number> result= getSimpleJdbcTemplate().query(sql, annotationIDRowMapper, externalID.toString());
       if (result == null) {
           return null;
       }
       if (result.isEmpty()) {
           return null;
       }
       
       if (result.size()>1) {
           throw new SQLException("There are "+result.size()+" annotations with"+ external_id +" "+externalID);
       }
       return result.get(0);
   }
     
      private final RowMapper<Number> annotationIDRowMapper = new RowMapper<Number>() {        
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
           Number result = rs.getInt(annotation_id);
           return result;
        }
    };
      
   @Override   
     public int deleteAnnotation(Number annotationId) throws SQLException{
        String sqlAnnotation = "DELETE FROM " + annotationTableName + " where "+annotation_id + " = ?";
        String sqlPermissions = "DELETE FROM " + permissionsTableName + " where "+annotation_id + " = ?";
        String sqlNotebooks = "DELETE FROM " + notebooksAnnotationsTableName + " where "+annotation_id + " = ?";
        int affectedNotebooks = getSimpleJdbcTemplate().update(sqlNotebooks, annotationId);
        int affectedPermissions = getSimpleJdbcTemplate().update(sqlPermissions, annotationId);
        int affectedAnnotations = getSimpleJdbcTemplate().update(sqlAnnotation, annotationId);
        if (affectedAnnotations>1) {
            throw new SQLException("There was more than one annotation ("+affectedAnnotations+") with the same ID "+annotationId);
        }
        return affectedAnnotations;
        //TODO implement deleting sources (see the specification document and the interfaces' javadoc
    }
     
 
   
   // TODO: so far URI in the xml is the same as the external_id in the DB!!
   // Chnage it when the decision is taken!!!
    @Override
    public Annotation addAnnotation(Annotation annotation, Number ownerID) {
        
        if (annotation == null) {
            return null;
        } 
        
        try {
            Annotation result = makeFreshCopy(annotation);
            
            ResourceREF ownerRef = new ResourceREF();
            ownerRef.setRef(String.valueOf(ownerID));
            result.setOwner(ownerRef);
            
            AnnotationIdentifier annotationIdentifier = new AnnotationIdentifier();
            result.setURI(annotationIdentifier.toString());
            
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("externalId", annotationIdentifier.toString());
            params.put("timeStamp", annotation.getTimeStamp());           
            params.put("ownerId", ownerID);
            params.put("headline", annotation.getHeadline());
            // may be changed once we elaborate the body
            params.put("bodyXml", annotation.getBody().getAny().get(0).toString());
            String sql = "INSERT INTO "+annotationTableName + "("+external_id+","+ time_stamp+"," + owner_id+","+headline+","+body_xml+" ) VALUES (:externalId, :timeStamp,  :ownerId, :headline, :bodyXml)";
            final int affectedRows = getSimpleJdbcTemplate().update(sql, params);
            if (affectedRows == 1) {
                return result;
            }
            else {
                // something went wrong
                return null;
            }
        } catch (DataAccessException exception) {
            throw exception;
        }
    }
     
    
      //////////////////////////////////////////////////
    @Override
    public AnnotationIdentifier getExternalID(Number internalID) {
        if (internalID == null) {
            return null;
        }
        
       String sql = "SELECT "+annotationExternal_id+" FROM "+annotationTableName+" WHERE "+annotationAnnotation_id+"  = ?";
       List<AnnotationIdentifier> result= getSimpleJdbcTemplate().query(sql, externalIDRowMapper, internalID.toString());
       if (result == null) {
           return null;
       }
       if (result.isEmpty()) {
           return null;
       }
       return result.get(0);
   }
     
      private final RowMapper<AnnotationIdentifier> externalIDRowMapper = new RowMapper<AnnotationIdentifier>() {        
        @Override
        public AnnotationIdentifier mapRow(ResultSet rs, int rowNumber) throws SQLException {
           AnnotationIdentifier result = new AnnotationIdentifier(rs.getString(external_id));
           return result;
        }
    };
    
    
    //////////// helpers /////////////////////// 
    
    
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
   
    //TODO: update when target sources and permissions are added
    private  Annotation makeFreshCopy(Annotation annotation){
        
        if (annotation == null) {
            return null;
        }
        
        Annotation result = new Annotation();
        result.setBody(annotation.getBody());
        result.setHeadline(annotation.getHeadline());
        result.setOwner(annotation.getOwner());
        result.setPermissions(annotation.getPermissions());
        result.setTargetSources(annotation.getTargetSources());
        result.setTimeStamp(annotation.getTimeStamp());
        result.setURI(annotation.getURI());
        
        return result;
    }
    
}
           
