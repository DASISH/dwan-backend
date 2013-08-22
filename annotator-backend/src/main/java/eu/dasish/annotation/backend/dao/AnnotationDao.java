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
package eu.dasish.annotation.backend.dao;

import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.ResourceREF;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Created on : Jun 27, 2013, 10:34:13 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */

// TODO: Getting Target Sources from Body and testing must be added!!!


public interface AnnotationDao extends ResourceDao{
    
    ////////////// GETTERS //////////////////////////
    
    
    
    /**
     * 
     * @param annotationID
     * @return the Annotation object with empty list of sources 
     * constructing a complete Annotation object from the result and "retrieveSourceIDs" is done in "DaoDispatchter"
     * 
     */
    public Annotation getAnnotationWithoutSources(Number annotationID) throws SQLException;
    
    
     /**
     * 
     * @param annotationIDs optional
     * @param text optional
     * @param access optional
     * @param namespace optional TODO: do not know what to do with it 
     * @param ownerID optional 
     * @param after optional
     * @param before optional 
     * @return the sub-list of internal annotation identifiers from the list "internalIDs" for annotations 
     * -- bodies of which contain the "text", 
     * -- to which inlogged user has "access", 
     * -- owned by "owner", 
     * -- added to the database between "before" and "after" time-dates.
     * 
     * 
     * The first step for GET api/annotations?<filters>
     */
    public List<Number> getFilteredAnnotationIDs(List<Number> annotationIDs, String text, String access, String namespace, Number ownerID, Timestamp after, Timestamp before);
       
      /**
     * 
     * @param annotationIDs
     * @return the list of annotationInfos (owner, headline, target sources, external_id) for the internal Ids from the  input list
     * used on the second step for GET api/annotations?<filters>
     */
    public List<AnnotationInfo> getAnnotationInfos(List<Number> annotationIDs);    
     
   
    /**
     * 
     * @param annotationIDs
     * @return list of resource references where an i-th reference is constructed from 
     */
    public List<ResourceREF> getAnnotationREFs(List<Number> annotationIDs); 
    
    /**
     * 
     * @param sourceIDs
     * @return the list of annotationdIDs of the annotations that are having target sources from "sourceIDs" list
     */
    public List<Number> retrieveAnnotationList(List<Number> sourceIDs);
   
    
       /**
     * 
     * @param annotationID
     * @return the list of the source's internal IDs of all the target sources of annotationID
     */
    public List<Number> retrieveSourceIDs(Number annotationID);   
    
   
       /**
     * 
     * @param annotationId
     * @return retrieves all the pairs (user-permission) for "annotationId" from the table annotations_principals permissions
     */
    public List<Map<Number, String>>  retrievePermissions(Number annotationId);
    
    
    
    ///////////// ADDERS /////////////////////
    
     public int addAnnotationSourcePair(Number annotationID, Number sourceID) throws SQLException;
    
   
    /**
     * 
     * @param annotationID
     * @param userID
     * @param permission
     * @return the amount of rows added to the table annotations_principals_permissions
     */
    public int addAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission) throws SQLException;
    
    
  
     /**
     * 
     * @param annotation added to the table with annotations 
     * @return  internal Id of the added annotation
     **/
    
    public Number addAnnotation(Annotation annotation, Number ownerID) throws SQLException;
 
     
    /////// UPDATERS //////////////////
    
    public int updateBody(Number annotationID, String serializedNewBody);
    
    
    //////////// DELETERS ///////////////////////
    
    /**
     * 
     * @param annotationId
     * @return removed annotation rows (should be 1)
     */
    
    
   
    public int deleteAnnotation(Number annotationId) throws SQLException;
    
    /**
     * 
     * @param annotationId
     * @return # removed annotation_source rows for given annotationID 
     */
    
    public int deleteAllAnnotationSource(Number annotationId) throws SQLException;
    
   
   
    public int deleteAnnotationPrincipalPermissions(Number annotationID) throws SQLException ;

  
  
    
}
