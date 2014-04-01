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

import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.Access;
import java.sql.SQLException;
import java.util.List;

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
     * @return the pair (annotation, owner_id) with empty list of Targets.
     * 
     * (Constructing a complete Annotation object using  "getAnnotationWithoutTargets" and "retrieveTargetIDs" is done in "DaoDispatchter".)
     * 
     */
    public Annotation getAnnotationWithoutTargetsAndPemissions(Number annotationID);
    
      
    public List<Number> getFilteredAnnotationIDs(Number ownerID, String text, String namespace, String after, String before);
     
  
    public List<Number> getAnnotationIDsForPermission(Number principalID, Access acess);
    
    public List<Number> getAnnotationIDsForPublicAccess(Access access);
    
    
    public List<Number> getAnnotationIDsForTargets(List<Number> TargetIDs);   
    
    /*
     * Use inly in the debugging mode to acces all the existing annotations.
     */
    public List<Number> getAllAnnotationIDs();
    
    public List<Number> sublistOrderedAnnotationIDs(List<Number> annotationIDs, int offset, int limit, String orderedBy, String desc);
    
       /**
     * unit test is missing
     * @param annotationIDs
     * @return annotationInfo  for the annotation with the internal annotationID.
     * 
     */
    public AnnotationInfo getAnnotationInfoWithoutTargetsAndOwner(Number annotationID);    
   
    /**
     * 
     * @param annotationIDs
     * @return list of reTarget references where an i-th reference is constructed from the external identifier of the annotation with the i-th internal identifier from the list.
     */
    public List<String> getAnnotationREFs(List<Number> annotationIDs); 
    
   
    
    
    
       /**
     * 
     * @param annotationID
     * @return the list of the internal IDs of all the target Targets of "annotationID".
     */
    //public List<Number> retrieveTargetIDs(Number annotationID);   
    
    
    public Number  getOwner(Number annotationID);
    
    /**
     * 
     * @param annotationID
     * @param principalID
     * @return access of the principalID w.r.t. annotationID, or null if the access is not given
     */ 
    public Access  getAccess(Number annotationID, Number principalID);
    
    public Access getPublicAttribute(Number annotationID);
    
    
    
    public List<Number> getAnnotations(Number notebookID);
    
    
    public boolean targetIsInUse(Number targetID);
    /**
     * 
     * @param annotationID
     * @return true if "annotationID" is mentioned in at least one of the joint tables:
     * "annotations_targets", "annotations_principals_accesss", "notebook_annotations".
     * Otherwise return "false".
     */
    //public boolean annotationIsInUse(Number annotationID);
    
    /**
     * ADDERS 
     */
    
    /**
     * 
     * @param annotationID
     * @param targetID
     * @return # updated rows in the joint table "annotations_target_Targets".
     * @throws SQLException 
     * Connects the annotation to its target Target by adding the pair (annotationID, TargetID) to the joint table.
     */ 
    public int addAnnotationTarget(Number annotationID, Number targetID);
    
   
    /**
     * 
     * @param annotationID
     * @param principalID
     * @param access
     * @return # rows added to the table "annotations_principals_accesss"
     * Sets the "access" for the "principalID" w.r.t. the annotation with "annotationID".
     */
    public int addAnnotationPrincipalAccess(Number annotationID, Number principalID, Access access);
    
    
  
     /**
     * 
     * @param annotation: the object to be added to the table "annotation".
     * @return  the internal ID of the added annotation, if it is added, or null otherwise.
     **/
    
    public Number addAnnotation(Annotation annotation, Number newOwnerID)  throws NotInDataBaseException;
 
     
    /////// UPDATERS //////////////////
    
   
    public int updateAnnotationBody(Number annotationID, String text, String mimeType, Boolean isXml);
    
   
    
    /**
     * 
     * @param annotation
     * @return # of updated rows in "annotation" table after updating the annotation. Should return 1 if update  happens
     * @throws SQLException 
     */
    public int updateAnnotation(Annotation annotation, Number annotationID, Number ownerID);
    
    
     /**
     * 
     * @param annotationID
     * @param principalID
     * @param access
     * @return # rows updated to the table "annotations_principals_accesss"
     * Sets the "access" for the "principalID" w.r.t. the annotation with "annotationID".
     */
    public int updateAnnotationPrincipalAccess(Number annotationID, Number principalID, Access access);
    
    
    public int updatePublicAttribute(Number annotationID, Access access);
    
    
   /**
    * DELETERS 
    */
    
    /**
     * 
     * @param annotationId
     * @return # rows in the table "annotation". It should be "1" if the annotation with "annotationID" is successfully deleted, and "0" otherwise.
     */
    
    
   
    public int deleteAnnotation(Number annotationId);
    
    /**
     * 
     * @param annotationId
     * @return # removed rows in the table "annotations_target_Targets". 
     */
    
    public int deleteAllAnnotationTarget(Number annotationID);
    
   
   /**
    * 
    * @param annotationID
    * @return # removed rows in the table "annotations_principals_accesss".
    * @throws SQLException 
    */
    public int deleteAnnotationPermissions(Number annotationID);
    
    public int deleteAnnotationPrincipalAccess(Number annotationID, Number principalID);
    
    public int deleteAnnotationFromAllNotebooks(Number annotationID);
    
    /*
     * HELPERS 
     */
    
    public String[] retrieveBodyComponents(AnnotationBody annotationBody);

}
