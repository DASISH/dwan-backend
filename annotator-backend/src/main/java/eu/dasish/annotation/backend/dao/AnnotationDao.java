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
import java.util.UUID;

/**
 * @author olhsha@mpi.nl
 */


public interface AnnotationDao extends ResourceDao{
    
    
    /**
     * GETTERS
     */
    
    /**
     * 
     * @param headline the headline of an annotation.
     * @return the list of uuid-s of the annotations with this headline; may be empty.
     */
    public List<UUID> getExternalIdFromHeadline(String headline);
    
    /**
     * 
     * @param headline the headline of an annotation.
     * @return the list of internal (database) identifiers of the annotations with this headline; may be empty.
     */
    public List<Number>  getInternalIDsFromHeadline(String headline);
    /**
     * 
     * @param annotationID the internal (database) identifier of an annotation.
     * @return the {@link Annotation} object whose fields are filled partially, 
     * only with the information accessible from the table "annotation". 
     * Constructing a complete {@link Annotation} object is done in the implementation of {@link DBDispatcher}.
     */
    public Annotation getAnnotationWithoutTargetsAndPemissionList(Number annotationID);
    
    /**
     * 
     * @param ownerID the owner of annotations we search for; if null then the owner may be any.
     * @param text the text fragment which must occur in an annotation-body; if null, then no requirements on the annotation body.
     * @param namespace (search on the parameter is not implemented).
     * @param after the earliest time of creating/last-update of annotations; if null then search "from the beginning of time".
     * @param before the latest time of creating/last-update of annotations; if null then search "till now".
     * @return the list of the internal database identifiers of the annotations that satisfy the criteria defined by the parameters.
     */ 
    public List<Number> getFilteredAnnotationIDs(Number ownerID, String text, String namespace, String after, String before);
     
    /**
     * 
     * @param principalID the internal database ID of a principal (a user or, in general, a group).
     * @param access an {@link Access} object representing an access level (none, read, write, all).
     * @return the list of the internal (database) identifiers of annotations to which "principalID" has access at least "acess".
     * For instance, if access' value is Access.WRITE, the method should output id-s of the annotations to which user has "write" (update bodies), 
     * or "all" access, according to the junction table "annotations-principals-accesses".
     */
    public List<Number> getAnnotationIDsPermissionAtLeast(Number principalID, Access access);
    
    /**
     * 
     * @param access an {@link Access} object representing  the public assess level of an annotation.
     * @return The list of the internal database identifiers of annotations for which "public" attribute has at least "access" access;
     * for instance on "write" the method should output annotations 
     * whose "public" value is "write" or "all".
     */
    public List<Number> getAnnotationIDsPublicAtLeast(Access access);
    
   
  
    /**
     * 
     * @return the list of the internal database identifiers of all the annotations, ordered by last-update, with the latest on top; 
     * used only by developers and the admin in the debugging service to access all the existing annotations.
     */
    public List<Number> getAllAnnotationIDs();
    
    /**
     * 
     * @param annotationIDs the list of internal database annotation identifiers.
     * @param offset the offset parameter for SELECT SQL request.
     * @param limit the limit parameter for SELECT SQL request.
     * @param orderedBy a criterion of ordering according to the SQL syntax (by some field).
     * @param desc direction of ordering according to the SQL syntax.
     * @return the SELECT response on the given parameters: i.e. the list of annotationID's starting from offset and to limit,
     * if the database rows are ordered according to "orderedBy" and "desc".
     */
    public List<Number> sublistOrderedAnnotationIDs(List<Number> annotationIDs, int offset, int limit, String orderedBy, String desc);
    
     /**
     * @param annotationIDs the internal database identifier of an annotation.
     * @return an {@link AnnotationInfo} object for the annotation with the internal annotationID: 
     * i.e. the information which you can take only from the "annotation" table
     * and not from the junction tables connecting the annotations to permissions and targets.
     * 
     */
    public AnnotationInfo getAnnotationInfoWithoutTargetsAndOwner(Number annotationID);    
   
    /**
     * 
     * @param annotationIDs the list of the internal database annotation identifiers.
     * @return the list of target h-references where the i-th reference is constructed from the external 
     * identifier of the annotation with the i-th internal identifier from the list.
     */
    public List<String> getAnnotationREFs(List<Number> annotationIDs); 
    
  
    /**
     * 
     * @param annotationID the internal database identifier of an annotation.
     * @return the internal database ID of the owner of the annotation.
     */
    public Number  getOwner(Number annotationID);
    
    /**
     * 
     * @param annotationID the internal database identifier of an annotation.
     * @param principalID the internal database identifier of a principal.
     * @return access of the principalID w.r.t. annotationID, or Access.NONE if the access is not given.
     */ 
    public Access  getAccess(Number annotationID, Number principalID);
    
    /**
     * 
     * @param annotationID the internal database identifier of an annotation.
     * @param principalID the internal database identifier of a principal.
     * @return true if there is triple (annotationID, principalID, access) for some access
     * in the corresponding junction table; false otherwise.
     */
    public boolean  hasExplicitAccess(Number annotationID, Number principalID);
    
    /**
     * 
     * @param annotationID the internal database identifier of an annotation.
     * @return the value of the "public" field for annotationID; can be one of ACCESS values.
     */
    public Access getPublicAttribute(Number annotationID);
    
    
    /**
     * 
     * @param notebookID the internal database identifier of a notebook.
     * @return the list of internal database identifiers of the annotations from the notebook with notebookID.
     */
    public List<Number> getAnnotations(Number notebookID);
    
    /**
     * 
     * @param targetID the internal database identifier of a target.
     * @return true if at least one annotation refers to the target with targetID; false otherwise.
     */
    public boolean targetIsInUse(Number targetID);
  
    /**
     * ADDERS 
     */
    
    /**
     * 
     * @param annotationID the internal database identifier of an annotation.
     * @param targetID the internal database identifier of a target.
     * @return # of updated rows in the junction table "annotations_targets".
     * Connects the annotation to its target by adding the pair (annotationID, targetID) 
     * to the junction table.
     */ 
    public int addAnnotationTarget(Number annotationID, Number targetID);
    
   
    /**
     * 
     * @param annotationID the internal database identifier of an annotation.
     * @param principalID the internal database identifier of a principal.
     * @param access the {@link Access} object representing an access level.
     * @return # of rows added to the table "annotations_principals_accesses".
     * Sets the "access" for the "principalID" w.r.t. the annotation with "annotationID".
     */
    public int addPermission(Number annotationID, Number principalID, Access access);
    
    
 
    /**
     * 
     * @param annotation the object to be added to the table "annotation".
     * @param newOwnerID the ownerID.
     * @return the internal ID of the added annotation, if it is added, or throws NotInDataBaseException otherwise.
     * @throws NotInDataBaseException if the request for the internal database annotation ID for the added annotation throws this exception.
     */
    public Number addAnnotation(Annotation annotation, Number newOwnerID)  throws NotInDataBaseException;
 
     
    /////// UPDATERS //////////////////
    
   /**
    * 
    * @param annotationID the internal database ID of the annotation to be updated.
    * @param text the new body text.
    * @param mimeType the new mime type.
    * @param isXml true if the new body is an xml, and false if it is a text.
    * @return # of updated rows in the table "annotation".
    */
    public int updateAnnotationBody(Number annotationID, String text, String mimeType, Boolean isXml);
    
    /**
     * 
     * @param annotationID the internal database ID of the annotation to be updated.
     * @param text the new headline.
     * @return # of updated rows in the table "annotation".
     */
    public int updateAnnotationHeadline(Number annotationID, String text);
    
   
    
    /**
     * 
     * @param annotation
     * @return # of updated rows in "annotation" table after updating the annotation. Should return 1 if update  happens.
     */
    /**
     * 
     * @param annotation the new annotation (including targets and permissions).
     * @param annotationID the internal database ID of the annotation to be updated.
     * @param ownerID the internal database Id of the new owner.
     * @return # of updated rows in "annotation" table after this FULL updating the annotation. Should return 1 if update  happens.
     */
    public int updateAnnotation(Annotation annotation, Number annotationID, Number ownerID);
    
    
     /**
     * 
     * @param annotationID the internal database identifier of an annotation.
     * @param principalID the internal database identifier of a principal.
     * @param access a {@link Access} object representing an access level.
     * @return # of rows updated in the table "annotations_principals_accesses".
     * Sets the "access" for the "principalID" w.r.t. the annotation with "annotationID".
     */
    public int updatePermission(Number annotationID, Number principalID, Access access);
    
    /**
     * 
     * @param annotationID the internal database ID of the annotation to be updated.
     * @param access access level.
     * @return # of updated rows in "annotation". Should be "1" if updated and "0" otherwise.
     */
    public int updatePublicAccess(Number annotationID, Access access);
    
    
   /**
    * DELETERS 
    */
    
    /**
     * 
     * @param annotationID the internal database identifier of an annotation.
     * @return # of deleted rows in the table "annotation". It should be "1" if the annotation with "annotationID" is successfully deleted, and "0" otherwise.
     */
    
    
   
    public int deleteAnnotation(Number annotationID);
    
    /**
     * 
     * @param annotationID the internal database identifier of an annotation.
     * @return # of removed rows in the table "annotations_targets". 
     */
    
    public int deleteAllAnnotationTarget(Number annotationID);
    
   
   /**
    * 
    * @param annotationID the internal database identifier of an annotation.
    * @return # of removed rows in the table "annotations_principals_accesses".
    */
    public int deletePermissions(Number annotationID);
    
    /**
     * 
     * @param annotationID the internal database identifier of an annotation.
     * @param principalID the internal database identifier of a principal.
     * @return # removed rows in the table "annotations_principals_accesses". 
     * Should be "1" is removed and "0" otherwise.
     */
    public int deletePermission(Number annotationID, Number principalID);
    
    /**
     * 
     * @param annotationID the internal database identifier of an annotation.
     * @return # of removed rows in the table "notebookds_annotations".
     */
    public int deleteAnnotationFromAllNotebooks(Number annotationID);
    
    /*
     * HELPERS 
     */
    
    /**
     * 
     * @param annotationBody a {@link AnnotationBody} object.
     * @return two string components of the annotationBody: the text or xml content, and the mime type.
     */
    public String[] retrieveBodyComponents(AnnotationBody annotationBody);

}
