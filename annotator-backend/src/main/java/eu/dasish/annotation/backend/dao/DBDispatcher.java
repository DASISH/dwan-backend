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

import eu.dasish.annotation.backend.ForbiddenException;
import eu.dasish.annotation.backend.MatchMode;
import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.PrincipalCannotBeDeleted;
import eu.dasish.annotation.backend.PrincipalExists;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.NotebookInfoList;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.PermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.Target;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.Principal;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * An implementation for this interface does not perform access to the database directly but dispatches 
 * requests to the corresponding dao implementations. Each defined in the 
 * interface method implements a simple or complex request. If the request addresses more than one
 * table in the database then the request is split up in basic per-table 
 * requests whose calls are ordered to avoid constraint violation;
 * e.g when a target is to be deleted then first it is checked if no annotation 
 * refers to it, and if this is the case then it is deleted from "target" table.
 * 
 * @author olhsha
 *
 */
public interface DBDispatcher {
    
    /**
     * Sets the server address, e.g. "ds/webannotator/ ".
     * @param relServiceURI a string representing a relative service URI.
     * 
     */
    void setResourcesPaths(String relServiceURI);

    /**
     * GETTERS
     */
     
    /**
     * 
     * @param externalID the external UUID of the resource.
     * @param resource  a {@link Resource} object representing a type of resource (annotation, principal, cached representation, target, notebook);
     * it tells in which table to look for.
     * @return the internal database id of the resource.
     * @throws NotInDataBaseException 
     */
    Number getResourceInternalIdentifier(UUID externalID, Resource resource) throws NotInDataBaseException;

    /**
     * 
     * @param resourceID the internal database identifier of the resource,
     * @param resource a {@link Resource} object representing a type of resource (annotation, principal, cached representation, target, notebook);
     * it tells in which table to look for.
     * @return the external UUID of the resource.
     */
    UUID getResourceExternalIdentifier(Number resourceID, Resource resource);

    /**
     * 
     * @param resourceID the internal database identifier of the resource.
     * @param resource  a {@link Resource} object representing a type of resource (annotation, principal, cached representation, target, notebook);
     * it tells in which table to look for.
     * @return a {@link PermissionList} object, representing pairs (principal, permission) for the resource; 
     * currently makes sense  only for annotations and notebooks.
     */
    PermissionList getPermissions(Number resourceID, Resource resource);

    /**
     * 
     * @param ownerId the internal database identifier of the owner whose annotations are searched for;
     * any owner is considered when "null".
     * @param link the link which at least one target source of the output annotations must contain;
     * no limitations on links when "null".
     * @param matchMode if the link of a target source must contain, beginWith, 
     * endsWith or be exact match of the "link" parameter above.
     * @param text that must be in the body of the requested annotations; no limitations when "null".
     * @param inloggedPrincipalID the internal database id of the inlogged principal.
     * @param accessMode the access mode that relates the inlogged principal to the requested annotations.
     * @param namespace not implemented.
     * @param after the lower limit of the last-update date for requested annotations (the begin of the time, if "null").
     * @param before the upper limit of the lust-update date (now, if "null"). 
     * @return the list of the internal database identifiers of annotations satisfying the criteria defined by the parameters.
     * @throws NotInDataBaseException if one of the getters used in the implementation throws this exception.
     */
    List<Number> getFilteredAnnotationIDs(UUID ownerId, String link, MatchMode matchMode, String text, Number inloggedPrincipalID, String  accessMode, String namespace, String after, String before) throws NotInDataBaseException;

    /**
     * The method must be used only in debug services by principals with "developer" role.
     * @return the {@link AnotationInfoList} object representing AnnotationInfo's  of all the annotations.
     */
    AnnotationInfoList getAllAnnotationInfos();

  /**
   * 
     * @param ownerId the internal database identifier of the owner whose annotations are searched for;
     * any owner is considered when "null".
     * @param link the link which at least one target source of the output annotation(s) must contain;
     * no limitations on links when "null".
     * @param matchMode tells if the link of a target source must contain, beginWith, 
     * endsWith or be exact match of the "link" parameter above.
     * @param text that must be in the body of the requested annotations; no limitations when null.
     * @param inloggedPrincipalID the internal database id of the inlogged principal.
     * @param accessMode the access mode that relate the inlogged principal to the requested annotations.
     * @param namespace not implemented.
     * @param after the lower limit of the last-update date for requested annotations (the begin of the time, if null).
     * @param before the upper limit of the lust-update date(now, if null). 
     * @return the {@link AnotationInfoList} object representing the list of the annotation info's (i.e. the information taken only from the table "annotations", and not from junction tables)
     * of the annotations satisfying the criteria defined by the parameters.
     * @throws NotInDataBaseException if getFileteredAnnotationIDs throws this exception.
   */
    AnnotationInfoList getFilteredAnnotationInfos(UUID ownerId, String link, MatchMode matchMode, String text, Number inloggedPrincipalID, String access, String namespace, String after, String before) throws NotInDataBaseException;

  
    
    /**
     *
     * @param internalID the internal database id of a cached representation.
     * @return a {@link CachedRepresentationInfo} object representing the metadata for the cached representation with the internal id "intenalID".
     */
    CachedRepresentationInfo getCachedRepresentationInfo(Number internalID);

    /**
     *
     * @param annotationID an internal database annotation identifier.
     * @return the {@link Annotation} object for this "annotationID", generated from the tables "annotation", "annotations_targets", "target", "annotations_principals_accesses".
     * 
     */
    Annotation getAnnotation(Number annotationID);

    /**
     * 
     * @param annotationID an internal database annotation identifier.
     * @return the internal database id of the owner of the annotation.
     */
    Number getAnnotationOwnerID(Number annotationID);

    /**
     * 
     * @param annotationID an internal database annotation identifier.
     * @return the {@link Principal} object that represents the owner of the annotation with "annotationID".
     */
    Principal getAnnotationOwner(Number annotationID);
    
    /**
     *
     * @param annotationID an internal database annotation identifier.
     * @return the {@link ReferenceList} object containing the h-references of all the targets of the annotationID.
     */
    ReferenceList getAnnotationTargets(Number annotationID);

    /**
     *
     * @param annotationID an internal database annotation identifier.
     * @return the list of h-references for which there is no cached representation.
     */
    List<String> getTargetsWithNoCachedRepresentation(Number annotationID);

    /**
     * 
     * @param annotationID an internal database annotation identifier.
     * @return the list of h-references of the principals which are present in 
     * the junction table "annotations_principals_accesses" in a pair with "annotationID"
     * and for which principal's name and / or principal's e-mail is missing.
     */
    List<String> getPrincipalsWithNoInfo(Number annotationID);

    /**
     *
     * @param targetID an internal database target identifier.
     * @return the list of the external  ID of targets that refer to the same source (link) as targetID
     */
    ReferenceList getTargetsForTheSameLinkAs(Number targetID);

    /**
     *
     * @param cachedID an internal database cached-representation identifier.
     * @return the BLOB of the cached representation.
     */
    InputStream getCachedRepresentationBlob(Number cachedID);

    /**
     * 
     * @param internalID an internal database target identifier.
     * @return the {@link Taget} object representing  the target with "internalID".
     */
    Target getTarget(Number internalID);

    /**
     *
     * @param principalID an internal database principal identifier.
     * @return the {@link Principal} object representing the principal with "principalID".
     */
    Principal getPrincipal(Number principalID) ;


    /**
     * 
     * @param eMail an email address.
     * @return a {@link Principal} object representing a principal with e-mail "eMail".
     * @throws NotInDataBaseException if an principal with such eMail is not found.
     */
    Principal getPrincipalByInfo(String eMail) throws NotInDataBaseException;

    /**
     * 
     * @param internalID the internal database Id of a principal.
     * @return a {@link Principal} object with this internalID; will throw runtime 
     * "array out of bound exception" if a principal is not found due to the attempt
     * to access the empty array; not crucial since if you already have the internalId
     * then the object must exist in the database, but fix it by throwing NotInDataBaseException.
     */
    String getPrincipalRemoteID(Number internalID);

    /**
     * 
     * @param remoteID the remote id of a principal.
     * @return the internal database id of the principal with "remotedID".
     * @throws NotInDataBaseException if a principal with "remoteID" is not found.
     */
    Number getPrincipalInternalIDFromRemoteID(String remoteID) throws NotInDataBaseException;
    
    /**
     * 
     * @param remoteID the remote id of a principal.
     * @return the external UUID  of the principal with "remotedID".
     * @throws NotInDataBaseException if a principal with "remoteID" is not found.
     */
    UUID getPrincipalExternalIDFromRemoteID(String remoteID) throws NotInDataBaseException;

    /**
     *
     * @param annotationID the internal database id of an annotation.
     * @param principalID the internal database id of a principal.
     * @return access of the principalID w.r.t. annotationID, computed is a maximum 
     * from "public" attribute of the annotation and the value "access" in the row 
     * (annotationId, principalID, access) of the table "annotations_principals_accesses".
     */
    Access getAccess(Number annotationID, Number principalID) ;
    
    /**
     * 
     * @param annotationID the internal database id of an annotation.
     * @return the public-attribute value for "annotationID".
     */
    Access getPublicAttribute(Number annotationID);

    /**
     * 
     * @param principalID the internal database id of a principal.
     * @return "user" or "developer" or "admin".
     */
    String getTypeOfPrincipalAccount(Number principalID);
    
    /**
     * 
     * @return  the {@link Principal} object corresponding to the "first in the list" admin of the database.
     */
    Principal getDataBaseAdmin() ;

    /**
     * 
     * @param action an {@link Access} object defining which action is to be perfomed.
     * @param principalID the internal Id of the principal who wants to perform "action".
     * @param resourceID the internal id of the resource on which "action" must be performed.
     * @param resource a type of resource (annotation, notebook, cached representation, target, 
     * principal).
     * @return true iff "principalId" is allowed to perform "action" on "resourceID"; false
     * otherwise; currently is implemented only for "annotation" and return "true" on
     * all other type of resources.
     */
    boolean canDo(Access action, Number principalID, Number resourceID, Resource resource);

    
    /// notebooks ///
    /**
     * 
     * @param prinipalID the internal database id of a principal.
     * @param access an {@link Access} object.
     * @return a {@link NotebookInfoList} object containing the list of all notebooks to which 
     * "principalID" has "access".
     */
    NotebookInfoList getNotebooks(Number prinipalID, Access access);

    /**
     * 
     * @param notebookID the internal database id of a notebook.
     * @param principalID the internal database id of a principal.
     * @param access an {@link Access} object.
     * @return true if "principalID" has "access" to  "notebookID".
     */
    boolean hasAccess(Number notebookID, Number principalID, Access access);

    /**
     * 
     * @param principalID the internal database id of a principal.
     * @return a {@link ReferenceList} object containing hrefs of notebooks owned
     * by "principlaID".
     */
    ReferenceList getNotebooksOwnedBy(Number principalID);

    /**
     * 
     * @param notebookID the internal database id of a notebook.
     * @param access an {@link Access} object.
     * @return the {@link ReferenceList} object containing hrefs of principals
     * which have "access" to "notebookID".
     */
    ReferenceList getPrincipals(Number notebookID, String access);
/**
 * 
 * @param notebookID the internal database id of a notebook.
 * @return the {@link Notebook} object corresponding to the notebook with "notebookID".
 */
    Notebook getNotebook(Number notebookID);

    /**
     * 
     * @param notebookID the internal database id of a notebook.
     * @return the internal database Id of the owner of the "notebookID".
     */
    Number getNotebookOwner(Number notebookID);

    /**
     * 
     * @param notebookID the internal database id of a notebook.
     * @param startAnnotation the first index for the list (of annotations). min value is "1".
     * @param maximumAnnotations the maximum # of annotations in the output list.
     * @param orderedBy SQL "orderedBy" string value, must be one of the fields in the table "annotation".
     * @param desc direction of ordering.
     * @return a {@link ReferenceList} object containing "maximum" hrefs of annotations in the notebook with "notebookID",
     * "orderedBy" in "desc" order, starting from "startAnnotation". 
     */
    ReferenceList getAnnotationsForNotebook(Number notebookID, int startAnnotation, int maximumAnnotations, String orderedBy, boolean desc);

    /**
     * UPDATERS
     */
    
    /**
     * 
     * @param resource a type of resource (annotation,notebook, cached representation, target,  principal).
     * @param oldIdentifier the external UUD of the resource.
     * @param newIdentifier the new external UUID of the resource.
     * @return # of updated rows in the corresponding table. Must be 1 if the resource with "oldIdentifier"
     * gets the external identifier updated by "newIdentifier", and 0 otherwise.
     * @throws NotInDataBaseException if the resource with "oldIdentifier" is not found.
     */
    boolean updateResourceIdentifier(Resource resource, UUID oldIdentifier, UUID newIdentifier) throws NotInDataBaseException;
    
    /**
     * 
     * @param principalExternalID the external UUD of a principle.
     * @param account a new type of account for this principle (a role: user, developer, admin).
     * @return "true" if the account gets updated, and "false" otherwise.
     * @throws NotInDataBaseException if the principal with the external UUD of a principle.
     */
    boolean updateAccount(UUID principalExternalID, String account) throws NotInDataBaseException;

   
    /**
     * 
     * @param annotation an {@link Annotation} object.
     * @param remoteUser   the remote id of the owner principal.
     * @return # of updated rows in "annotation" table: "1" if the annotation gets updated, and "0" otherwise.
     * @throws NotInDataBaseException if the annotation with the external id, given in the annotation object,
     * or the remote user are not found in the database.
     * @throws ForbiddenException if the "remoteUser" does not have "all" rights for the annotation with the external id,
     * given in the "annotation" object.
     */
    int updateAnnotation(Annotation annotation, String remoteUser) throws NotInDataBaseException, ForbiddenException;

    /**
     *
     * @param internalID the internal database id of an annotation whose body must be updated.
     * @param annotationBody an {@link AnnotationBody} object.
     * @return # of updated rows in "annotation" table: "1" if the annotation body gets updated by
     * "annotationBody" gets updated, and "0" otherwise.
     */
    int updateAnnotationBody(Number internalID, AnnotationBody annotationBody);
    
    /**
     * 
     * @param internalID the internal database id of an annotation whose header must be updated.
     * @param newHeader  the new header for the annotation with "internalID".
     * @return # of updated rows in "annotation" table: "1" if the annotation body gets updated by "newHeader",
     * and "0" otherwise.
     */
    int updateAnnotationHeadline(Number internalID, String newHeader);

    /**
     *
     * @param annotationID the internal database id of an annotation.
     * @param principalID the internal database id of a principal.
     * @param access an {@link Access} object representing an access level.
     * @return # rows updated in the table "annotations_principals_access: "1" if 
     * "access" is assigned to the "principlaID" for the "ammotationID".
     */
    int updatePermission(Number annotationID, Number principalID, Access access);

    /**
     * 
     * @param annotationID the internal database id of an annotation.
     * @param accessList  a {@link PermissionList} object containing the list  representing (principal, access) pairs.
     * @return  # of rows updated or added in the table "annotations_principals_access"  according to "permissionList".
     * @throws NotInDataBaseException if one of the principals mentioned in the list, is not found.
     */
    int updateOrAddPermissions(Number annotationID, PermissionList permissionList) throws NotInDataBaseException ;
    
    /**
     * 
     * @param annotationID the internal database id of an annotation.
     * @param publicAttribute  an {@link Access} object.
     * @return # of updated rows in "annotation" table; must be 1 if "public" field for 
     * "annotationID" has been updated by the value of "publicAttribute".
     */
    int updatePublicAttribute(Number annotationID, Access publicAttribute);
    
    /**
     * 
     * @param principal a {@link Principal} object.
     * @return the internal database id of the updated principal.
     * @throws NotInDataBaseException if the principal with the external id declared in "principal"
     * is not found.
     */
    Number updatePrincipal(Principal principal) throws NotInDataBaseException;

    /**
     * 
     * @param targetID the internal database id of a target.
     * @param cachedID the internal database id of a cached representation.
     * @param fragmentDescriptor the fragment string that locates the target in the cached representation.
     * @return # of updated rows in the junction table "targets_cached_representations".
     */
    int updateTargetCachedFragment(Number targetID, Number cachedID, String fragmentDescriptor);
    
    /**
     * 
     * @param cachedInfo a {@link CachedRepresentationInfo} object representing the metadata of a cached representation.
     * @return # of updated rows in the table "cached_representation"; must be 1 if the
     * row with the external id declared in "cachedInfo" has been updated.
     * @throws NotInDataBaseException if the cached representation with the external id declared in the
     * "cachedInfo" is not found.
     */
    int updateCachedMetada(CachedRepresentationInfo cachedInfo)  throws NotInDataBaseException;
    
    /**
     * 
     * @param internalID the internal database id of a cached representation.
     * @param cachedBlob a new BLOB for the cached representation.
     * @return # of updated rows in "cached_representations_table"; must be "1" if the row with 
     * "internalID" has been updated.
     * @throws IOException if reading BLOB fails.
     */
    public int updateCachedBlob(Number internalID, InputStream cachedBlob) throws IOException;
    
    
    /**
     * 
     * @param notebookID the internal database id of a notebook.
     * @param upToDateNotebookInfo a {@link NotebookInfo} object.
     * @return true if the "notebooks" table has been updated, namely the row with "notebookID" updated 
     * by the corresponding values declared in "upToDateNotebookInfo"; false when no updated happens.
     * @throws NotInDataBaseException if the notebook with "internalID" is not found.
     */
    boolean updateNotebookMetadata(Number notebookID, NotebookInfo upToDateNotebookInfo) throws NotInDataBaseException;

    /**
     * 
     * @param notebookID the internal database id of a notebook.
     * @param annotationID the internal database id of an annotation.
     * @return true if the junction table "notebooks_annotations" is updated by adding row with values (notebookID, annotationID).
     */
    boolean addAnnotationToNotebook(Number notebookID, Number annotationID);

    /**
     * ADDERS
     */
 
    /**
     * 
     * @param targetID the internal database id of a target.
     * @param fragmentDescriptor a string that locates the target within the cached representation.
     * @param cachedInfo a {@link CachedRepresentationInfo} object representing the metadata of the cached representation.
     * @param cachedBlob a BLOB, representing the content of the cached representation.
     * @return result[0] = # updated rows in the table "targets_cached_representations" (must be 1 or 0). result[1] = the
     * internal ID of the added cached (a new one if "cached" was new for the database).
     * @throws NotInDataBaseException when adding a cached representation to the corresponding table fails.
     * @throws IOException if reading BLOB fails.
     */
    Number[] addCachedForTarget(Number targetID, String fragmentDescriptor, CachedRepresentationInfo cachedInfo, InputStream cachedBlob) throws NotInDataBaseException, IOException;

    /**
     *
     * @param annotationID the internal database id of an annotation.
     * @param targets list of {@link TargetInfo} objects, where a {@link TargetInfo} object represents
     * the most essential metadata of a target.
     * @return a map "temporary targetID |--> target externalID"; its domain is the
     * temporary IDs of all the new targets; while adding a new target, a new
     * external ID is generated for it and it becomes the value of the map; the
     * targetIDs which are already present in the database are not in the domain. If
     * all targets are old, then the map is empty.
     * @throws NotInDataBaseException if adding of one of fresh targets fails.
     */
    Map<String, String> addTargetsForAnnotation(Number annotationID, List<TargetInfo> targets)  throws NotInDataBaseException;

    /**
     * 
     * @param ownerID the internal database id of a principal.
     * @param annotation an {@link Annotation} object, representing an annotation to be added to the database.
     * @return the internal database id of the just added fresh annotation.
     * @throws NotInDataBaseException if adding fails.
     */
    Number addPrincipalsAnnotation(Number ownerID, Annotation annotation) throws NotInDataBaseException;

   /**
    * 
    * @param principal a {@link Principal} object representing a principal to be added.
    * @param remoteID a remote id of the principal to be added.
    * @return the internal database id of the just added principal.
    * @throws NotInDataBaseException if adding fails.
    * @throws PrincipalExists if the principal with the remoteID already exists in the database.
    */
    Number addPrincipal(Principal principal, String remoteID) throws NotInDataBaseException, PrincipalExists;

    /**
     * 
     * @param username 
     * @param password
     * @param strength
     * @param salt
     * @return the total # of updated rows in the tables related to the Spring authentication; must be 
     * 2=1+1, where 1 is for the table "users", and 1 is for the table "roles".
     */
    public int addSpringUser(String username, String password, int strength, String salt);
    
       /// notebooks ////
    /**
     * 
     * @param notebook a {@link Noetbook} object to be added.
     * @param ownerID the internal database id of a principal who will be the owner of the notebook.
     * @return the internal database id of a new notebook.
     * @throws NotInDataBaseException if adding fails.
     */
    Number createNotebook(Notebook notebook, Number ownerID) throws NotInDataBaseException;

    /**
     * 
     * @param notebookID  the internal database id of a notebook.
     * @param annotation an {@link Annotation} object representing an annotation to be added. 
     * @param ownerID the internal database id of the notebook's owner (principal).
     * @return true iff the new annotation has been added to the database and then it was added  to the notebook.
     * @throws NotInDataBaseException if adding annotation to the database fails.
     */
    boolean createAnnotationInNotebook(Number notebookID, Annotation annotation, Number ownerID) throws NotInDataBaseException;

   
    /**
     * DELETERS
     */
    
    /**
     * 
     * @param annotationID the internal database ID of an annotation.
     * @param principalID the internal database ID of a principal.
     * @return # number of removed rows in the junction table "annotations_principals_accesses"; 
     * must be 1 if a row with "annotationID" and "principalID" has been removed.
     */
    int deleteAnnotationPrincipalAccess(Number annotationID, Number principalID); 
    
    /**
     *
     * @param principalID the internal database id of a principal to be deleted.
     * @return # of affected rows in the table "principal; it is 1 if the
     * principalID is found and deleted; it is 0 if it is not found or not deleted,
     * e.g. because it is in use in the table "annotations_principals_accesses".
     */
    /**
     * 
     * @param principalID the internal database id of a principal to be deleted.
     * @return # of affected rows in the table "principal"; it is 1 if the
     * principalID is found and deleted; it is 0 if not deleted.
     * @throws PrincipalCannotBeDeleted  if the principal cannot be deleted, 
     * e.g. because it is in use in the table "annotations_principals_accesses".
     */
    int deletePrincipal(Number principalID) throws PrincipalCannotBeDeleted;

 /**
  * 
  * @param internalID the internal id of a cached representation to be deleted;
  * @return # of affected rows in the table "cached_representations"; must be "1" if deleted.
  */
    int deleteCachedRepresentation(Number internalID);

    /**
     *
     * @param targetID the internal database ID of a target.
     * @param cachedID the internal database id of a cached representation.
     * @return result[0] = # deleted rows in the table "targets_cached_representations" (1, or 0). result[1] = # deleted rows in
     * the table "cached_representation" (should be 0 if the cached representation is in use by some other target).
     */
    int[] deleteCachedRepresentationOfTarget(Number TargetID, Number cachedID);

    /**
     *
     * @param targetID the internal database ID of a target.
     * @return result[0] = # deleted rows in the table "targets_cached_representations". result[1] = # deleted rows in the table "cached_representation".
     *
     */
    int[] deleteAllCachedRepresentationsOfTarget(Number targetID);

    /**
     * 
     * @param internalID the internal database ID of a target to be deleted.
     * @return # of deleted rows in the table "target"; must be one if "internalID" is deleted.
     */
    int deleteTarget(Number internalID);
    
   
    /**
     *
     * @param annotationID the internal database id of an annotation to be deleted.
     * @return result[0] = # deleted rows in the table "annotation" (1 or 0);
     * result[1] = # deleted rows in the table "annotations_principals_accesses".;
     * result[2] = # deleted rows in the table "annotations_targets";
     * result[3] = # deleted rows in the table "target".
     */
    int[] deleteAnnotation(Number annotationID);

    /// notebooks ///
    /**
     * 
     * @param notebookID the internal database id of a notebook to be deleted.
     * @return true if the notebook with "notebookID" has been deleted.
     */
    boolean deleteNotebook(Number notebookID);

    //////// HELPERS for resources /////
    /**
     * 
     * @param annotationID the internal database id of an annotation.
     * @return a {@link ResponseBody} object containing an annotation and a list of actions to do;
     * for now, the result object contains the {@link Annotation} element representing annotation
     * with "annotationID" and list of targets for which cached representations are missing.
     */
    ResponseBody makeAnnotationResponseEnvelope(Number annotationID);

    /**
     * 
     * @param notebookID the internal database id of a notebook.
     * @return a {@link ResponseBody} object containing a notebook and a list of actions to do;
     * actions are not specified yet.
     * 
     */
    ResponseBody makeNotebookResponseEnvelope(Number notebookID);

    /**
     * 
     * @param resourceID the internal database id of a resource.
     * @param resource a {@link Resource} object, specifying the type of the resource:
     * annotation, principal, notebook, target, cached_representation.
     * @return a {@link ResponseBody} object containing the list of (principal, permission) for this resource;
     * if information about a principal (e.g. an e-mail) is missing then the action like "add_principal_info"
     * is added to the list of actions.
     */
    ResponseBody makeAccessResponseEnvelope(Number resourceID, Resource resource);
    
    /**
     * 
     * @param fullName a string representing a full name of a principal.
     * @return the external Id of the (first) principal with this full name.
     * @throws NotInDataBaseException if such principal is not found.
     */
    UUID getPrincipalExternalIdFromName(String fullName) throws NotInDataBaseException;
    
    /**
     * 
     * @param headline the headline of an annotation.
     * @return  the list of UUID's of all the annotations with this headline.
     */
    List<UUID> getAnnotationExternalIdsFromHeadline(String headline);
    
    /**
     * 
     * @param headline the headline of an annotation.
     * @return the list of internal database id-s of all the annotations with this headline.
     */
    List<Number> getAnnotationInternalIDsFromHeadline(String headline);
}
