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
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.UserWithPermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.Target;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.User;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author olhsha
 **/




public interface DBIntegrityService{
    
    public void setServiceURI(String serviceURI);
    
    
    /**
     * GETTERS
     */
    
    /**
     * 
     * @param UUID
     * @return the internal identifier of the annotations with "externalID", or null if no such annotation. 
     */
    Number getAnnotationInternalIdentifier(UUID externalID);
    
    /**
     * 
     * @param annotationID
     * @return the externalID of the annotation with "internalID" or null if there is no such annotation.
     */
    UUID getAnnotationExternalIdentifier(Number annotationID);
    

    Number getAnnotationInternalIdentifierFromURI(String uri);
    

   /**
    * 
    * @param word
    * @param text
    * @param access
    * @param namespace
    * @param after
    * @param before
    * @return the list of internal id-s of the annotations such that:
    * -- Targets' links of which contain "link" (as a substring),
    * -- serialized bodies of which contain "text",
    * -- current user has "access" (owner, reader, writer)  to them,
    * -- namespace ???,
    * -- owned by "owner",
    * -- created after time-samp "after and before time-stamp "before".
    */
    List<Number> getFilteredAnnotationIDs(String link, String text, Number inloggedUserID, String[] accessModes, String namespace, Timestamp after, Timestamp before);
    
    AnnotationInfoList getAllAnnotationInfos();
    
    /**
     * 
     @param word
    * @param text
    * @param access
    * @param namespace
    * @param owner
    * @param after
    * @param before
     * @return the list of the annotationInfos of the annotations such that:
    * -- Targets' links of which contain "link" (as a substring),
    * -- serialized bodies of which contain "text",
    * -- current user has "access" (owner, reader, writer)  to them,
    * -- namespace ???,
    * -- owned by "owner",
    * -- created after time-samp "after and before time-stamp "before".
     */
    AnnotationInfoList getFilteredAnnotationInfos(String word, String text, Number inloggedUserID, String[] accessModes, String namespace, UUID
            ownerID, Timestamp after, Timestamp before);

    /**
     * 
     * @param userID
     * @return the external identifier of the user "userID", or null if no such user.
     */
    UUID getUserExternalIdentifier(Number userID);

    /**
     * 
     * @param externalID
     * @return the internal identifier of the user with "externalID", or null if there is no such user.
     */
    Number getUserInternalIdentifier(UUID externalID);
    
      /**
     * 
     * @param cachedID
     * @return the external identifier of the cached representation "cachedID", or null if no such one.
     */
    UUID getCachedRepresentationExternalIdentifier(Number cachedID);

    /**
     * 
     * @param externalID
     * @return the internal identifier of the cachedRepresentation with "externalID", or null if there is no such one.
     */
    Number getCachedRepresentationInternalIdentifier(UUID externalID);
    
    Number getTargetInternalIdentifier(UUID externalID); 

    String getTargetURI(Number targetID);
   
    UUID getTargetExternalIdentifier(Number targetID);
     
    String getUserURI(Number userID); 
    
    /**
     * 
     * @param internalID
     * @return CachedRepresentationInfo (i.e. "metadata") for cached representation with the internal id "intenalID"
     */
    CachedRepresentationInfo getCachedRepresentationInfo(Number internalID);
    
    /**
     * 
     * @param annotationID
     * @return the object Annotation generated from the tables "annotation", "annotations_target_Targets", "Target", "annotations_principals_permissions".
     * @throws SQLException 
     */
    Annotation getAnnotation(Number annotationID);
    
     /**
     * 
     * @param annotationID
     * @return the object TargetList containing all target Targets of the annotationID
     * @throws SQLException 
     */
    ReferenceList getAnnotationTargets(Number annotationID);
    
    
    /**
     * 
     * @param annotationID
     * @return the list of targetURI's for which there is no cached representation
     */
    List<String> getTargetsWithNoCachedRepresentation(Number annotationID);
    
    List<String> getUsersWithNoInfo(Number annotationID);
    
    /**
     * 
     * @param annotationID
     * @return the list of TargetID's for which there is no cached representation
     */
    public UserWithPermissionList getPermissionsForAnnotation(Number annotationID);
    
     /**
     * 
     * @param TargetID
     * @return the list of the external version ID-s that refers to the same source (link) as targetID 
     */
    public ReferenceList getTargetsForTheSameLinkAs(Number targetID);
    
    /**
     * 
     * @param cachedID
     * @return BLOB of the cachedID
     */
    public InputStream getCachedRepresentationBlob(Number cachedID) ;
    
    
    
    public Target getTarget(Number internalID);
    
    /**
     * 
     * @param userID
     * @return user with "userID"
     */
    public User getUser(Number userID);
            
    /**
     * 
     * @param eMail
     * @return user with e-mail "eMail"
     */
    public User getUserByInfo(String eMail);
    
    
    public String getUserRemoteID(Number internalID);
    
    public Number getUserInternalIDFromRemoteID(String remoteID);
  
    /**
     * 
     * @param annotationID
     * @param userID
     * @return permission of the userID w.r.t. annotationID, or null if the permission is not given
     */ 
    public Permission  getPermission(Number annotationID, Number userID);
    
    
    public String getTypeOfUserAccount(Number userID);
    
    /**
     * UPDATERS
     */
    
    public boolean updateAccount(UUID userExternalID, String account);
   
    
    /**
     * 
     * @param userID
     * @param annotation
     * @return 1 of the annotation if it is updated
     */
    int updateUsersAnnotation(Number userID, Annotation annotation);
    
     
    
    /**
     * 
     * @param userID
     * @param annotationBody
     * @return 1 of the annotation if it is updated
     */
    int updateAnnotationBody(Number internalID, AnnotationBody annotationBody);
    
    
    
       /**
     * 
     * @param annotationID
     * @param userID
     * @param permission
     * @return # rows updated to the table "annotations_principals_permissions"
     * Sets the "permission" for the "userID" w.r.t. the annotation with "annotationID".
     */
    public int updateAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission);
    
   /**
     * 
     * @param annotationID
     * @param permissionList
     * @return # of rows updated or added in the table annotations_principals_permissions
     */
    public int updatePermissions(Number annotationID, UserWithPermissionList permissionList);
    
    public Number updateUser(User user);
   
   /**
    * ADDERS
    */
    /**
     * 
     * @param targetID
     * @param cachedInfo
     * @param cachedBlob
     * @return result[0] = # updated rows in the table "Targets_cached_representations" (must be 1 or 0).
     * result[1] = the internal ID of the added cached (a new one if "cached" was new for the Data Base).
     */
    Number[] addCachedForTarget(Number targetID, String fragmentDescriptor, CachedRepresentationInfo cachedInfo, InputStream cachedBlob);
    
 
    /**
     * 
     * @param annotationID
     * @param targets
     * @return map temporaryTargetID |--> TargetExternalID. Its domain is the temporary IDs of all the new Targets. While adding a new Target a new external ID is generated for it and it becomes the value of the map. The TargetIDs which are already present in the DB are not in the domain. If all Targets are old, then the map is empty. 
     * @throws SQLException 
     */
    Map<String, String> addTargetsForAnnotation(Number annotationID, List<TargetInfo> targets);

    /**
     * 
     * @param userID
     * @param annotation
     * @return the internalId of the just added "annotation" (or null if it is not added) by the owner "userID". 
     * calls "addTargetsForAnnotation" 
     * @throws SQLException 
     */
    Number addUsersAnnotation(Number userID, Annotation annotation);
    
    /**
     * 
     * @param user
     * @param remoteID is got from the server
     * @return the internal Id of the just added "user", or null if it was not added for some reason (already exists)
     * @throws SQLException 
     */
    Number addUser(User user, String remoteID);

    int addAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission);
    
    /**
     * DELETERS
     */
    
    /**
     * 
     * @param userID
     * @return # of affected rows in the table "principal". 
     * It is 1 if the userId is found and deleted; 
     * it is 0 if it is not found or not deleted, e.g. because it is in use in the table "annotationsPreincipalsPermissions"
     */
    public int deleteUser(Number userID);
    
     /**
     * 
     * @param userID
     * @return # of affected rows in the table "principal". 
     * It is 1 if the userId is found and deleted; 
     * it is 0 if it is not found or not deleted, e.g. because it is in use in the table "annotationsPreincipalsPermissions"
     */
    public int deleteUserSafe(Number userID);
    
    /**
     * 
     * @param TargetID
     * @param cachedID
     * @return result[0] = # deleted rows in the table "Targets_cached_representations" (1, or 0).
     * result[1] = # deleted rows in the table "cached_representation" (should be 0 if the cached representation is in use by some other Target???).
     */
    int[] deleteCachedRepresentationOfTarget(Number TargetID, Number cachedID) ;

    
    

    
    /**
     * 
     * @param targetID
     * @return 
     * result[0] =  # deleted rows in the table "targets_cached_representations".
     * result[1] = # deleted rows in the table "cached_representation".
     **/
    
    int[] deleteAllCachedRepresentationsOfTarget(Number versionID);

      
    
    /**
     * 
     * @param annotationID
     * @return result[0] = # deleted rows in the table "annotation" (1 or 0).
     * result[1] = # deleted rows in the table "annotations_principals_permissions".
     * result[2] = # deleted rows in the table "annotations_target_Targets".
     * result[3] = # deleted rows in the table "Target".
     * @throws SQLException 
     */
    int[] deleteAnnotation(Number annotationID);

    
    
}
