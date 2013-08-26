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


public interface AnnotationDao extends ResourceDao{
    
    
    /**
     * GETTERS
     */
    
    /**
     * 
     * @param annotationID
     * @return the Annotation object with empty list of sources.
     * 
     * (Constructing a complete Annotation object using  "getAnnotationWithoutSources" and "retrieveSourceIDs" is done in "DaoDispatchter".)
     * 
     */
    public Annotation getAnnotationWithoutSources(Number annotationID) throws SQLException;
    
    
     /**
     * 
     * @param annotationIDs: the list of annotationID-s from which the resulting annotations are to be selected.
     * @param text: the text which the resulting annotations' bodies must contain.
     * @param access: the resulting annotations must have permission "access" (owner, or writer, or reader) for the currently inlogged user.
     * @param namespace TODO: do not know what to do with it 
     * @param ownerID: the resulting annotations are owned by the owner "ownerID".
     * @param after: the resulting annotations must have timestamp later than "after".
     * @param before: the resulting annotations must have timestamp earlier than "before".
     * @return the sub-list of internal annotation identifiers from the list "internalIDs" for annotations 
     * -- bodies of which contain the "text", 
     * -- to which inlogged user has "access", 
     * -- owned by "owner", 
     * -- added to the database between "before" and "after" time-dates.
     * 
     */
    public List<Number> getFilteredAnnotationIDs(List<Number> annotationIDs, String text, String access, String namespace, Number ownerID, Timestamp after, Timestamp before);
       
      /**
     * 
     * @param annotationIDs
     * @return the list of annotationInfos (owner, headline, target sources, external_id) for the annotations with the internal IDs from the  input list.
     * 
     */
    public List<AnnotationInfo> getAnnotationInfos(List<Number> annotationIDs);    
     
   
    /**
     * 
     * @param annotationIDs
     * @return list of resource references where an i-th reference is constructed from the external identifier of the annotation with the i-th internal identifier from the list.
     */
    public List<ResourceREF> getAnnotationREFs(List<Number> annotationIDs); 
    
    /**
     * 
     * @param sourceIDs
     * @return the list of annotationdIDs of the annotations which target sources are from "sourceIDs" list.
     */
    public List<Number> retrieveAnnotationList(List<Number> sourceIDs);
   
    
       /**
     * 
     * @param annotationID
     * @return the list of the internal IDs of all the target sources of "annotationID".
     */
    public List<Number> retrieveSourceIDs(Number annotationID);   
    
   
       /**
     * 
     * @param annotationId
     * @return all the pairs (user-permission) for "annotationId" from the table annotations_principals permissions.
     */
    public List<Map<Number, String>>  retrievePermissions(Number annotationId);
    
    
    /**
     * 
     * @param sourceID
     * @return true if "annotationID" is mentioned in at least one of the joint tables:
     * "annotations_target_sources", "annotations_principals_permissions", "notebook_annotations".
     * Otherwise return "false".
     */
    public boolean annotationIsInUse(Number annotationID);
    
    /**
     * ADDERS 
     */
    
    /**
     * 
     * @param annotationID
     * @param sourceID
     * @return # updated rows in the joint table "annotations_target_sources".
     * @throws SQLException 
     * Connects the annotation to its target source by adding the pair (annotationID, sourceID) to the joint table.
     */ 
    public int addAnnotationSourcePair(Number annotationID, Number sourceID) throws SQLException;
    
   
    /**
     * 
     * @param annotationID
     * @param userID
     * @param permission
     * @return # rows added to the table "annotations_principals_permissions"
     * Sets the "permission" for the "userID" w.r.t. the annotation with "annotationID".
     */
    public int addAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission) throws SQLException;
    
    
  
     /**
     * 
     * @param annotation: the object to be added to the table "annotation".
     * @return  the internal ID of the added annotation, if it is added, or null otherwise.
     **/
    
    public Number addAnnotation(Annotation annotation, Number ownerID) throws SQLException;
 
     
    /////// UPDATERS //////////////////
    /**
     * 
     * @param annotationID
     * @param serializedNewBody
     * @return # of updated rows in "annotation" table after updating the annotation's body with "serializedNewBody". Should return 1.
     */
    public int updateBody(Number annotationID, String serializedNewBody);
    
    
   /**
    * DELETERS 
    */
    
    /**
     * 
     * @param annotationId
     * @return # rows in the table "annotation". It should be "1" if the annotation with "annotationID" is successfully deleted, and "0" otherwise.
     */
    
    
   
    public int deleteAnnotation(Number annotationId) throws SQLException;
    
    /**
     * 
     * @param annotationId
     * @return # removed rows in the table "annotations_target_sources". 
     */
    
    public int deleteAllAnnotationSource(Number annotationID) throws SQLException;
    
   
   /**
    * 
    * @param annotationID
    * @return # removed rows in the table "annotations_principals_permissions".
    * @throws SQLException 
    */
    public int deleteAnnotationPrincipalPermissions(Number annotationID) throws SQLException ;

  
  
    
}
