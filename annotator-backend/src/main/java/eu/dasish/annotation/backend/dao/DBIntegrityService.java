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
 *
 * @author olhsha
 *
 */
public interface DBIntegrityService {

    void setServiceURI(String serviceURI);

    /**
     * GETTERS
     */
      
    Number getResourceInternalIdentifier(UUID externalID, Resource resource) throws NotInDataBaseException;

    Number getResourceInternalIdentifierFromURI(String uri, Resource resource) throws NotInDataBaseException;

    UUID getResourceExternalIdentifier(Number resourceID, Resource resource);

    String getResourceURI(Number resourceID, Resource resource);

    PermissionList getPermissions(Number resourceID, Resource resource);

    /**
     *
     * @param word
     * @param text
     * @param access
     * @param namespace
     * @param after
     * @param before
     * @return the list of internal id-s of the annotations such that: --
     * Targets' links of which contain "link" (as a substring), -- serialized
     * bodies of which contain "text", -- current principal has "access" (owner,
     * read, write) to them, -- namespace ???, -- owned by "owner", --
     * created after time-samp "after and before time-stamp "before".
     */
    List<Number> getFilteredAnnotationIDs(UUID ownerId, String link, String text, Number inloggedPrincipalID, String  accessMode, String namespace, String after, String before) throws NotInDataBaseException;

    AnnotationInfoList getAllAnnotationInfos();

    /**
     *
     * @param word
     * @param text
     * @param access
     * @param namespace
     * @param owner
     * @param after
     * @param before
     * @return the list of the annotationInfos of the annotations such that: --
     * Targets' links of which contain "link" (as a substring), -- serialized
     * bodies of which contain "text", -- current principal has "access" (owner,
     * read, write) to them, -- namespace ???, -- owned by "owner", --
     * created after time-samp "after and before time-stamp "before".
     */
    AnnotationInfoList getFilteredAnnotationInfos(UUID ownerId, String link, String text, Number inloggedPrincipalID, String access, String namespace, String after, String before) throws NotInDataBaseException;

  
    
    /**
     *
     * @param internalID
     * @return CachedRepresentationInfo (i.e. "metadata") for cached
     * representation with the internal id "intenalID"
     */
    CachedRepresentationInfo getCachedRepresentationInfo(Number internalID);

    /**
     *
     * @param annotationID
     * @return the object Annotation generated from the tables "annotation",
     * "annotations_target_Targets", "Target",
     * "annotations_principals_accesss".
     * @throws SQLException
     */
    Annotation getAnnotation(Number annotationID);

    Number getAnnotationOwnerID(Number annotationID);

    Principal getAnnotationOwner(Number annotationID);
    
    /**
     *
     * @param annotationID
     * @return the object TargetList containing all target Targets of the
     * annotationID
     * @throws SQLException
     */
    ReferenceList getAnnotationTargets(Number annotationID);

    /**
     *
     * @param annotationID
     * @return the list of targetURI's for which there is no cached
     * representation
     */
    List<String> getTargetsWithNoCachedRepresentation(Number annotationID);

    List<String> getPrincipalsWithNoInfo(Number annotationID);

    /**
     *
     * @param targetID
     * @return the list of the external version ID-s that refers to the same
     * source (link) as targetID
     */
    ReferenceList getTargetsForTheSameLinkAs(Number targetID);

    /**
     *
     * @param cachedID
     * @return BLOB of the cachedID
     */
    InputStream getCachedRepresentationBlob(Number cachedID);

    Target getTarget(Number internalID);

    /**
     *
     * @param principalID
     * @return principal with "principalID"
     */
    Principal getPrincipal(Number principalID) ;

    /**
     *
     * @param eMail
     * @return principal with e-mail "eMail"
     */
    Principal getPrincipalByInfo(String eMail) throws NotInDataBaseException;

    String getPrincipalRemoteID(Number internalID);

    Number getPrincipalInternalIDFromRemoteID(String remoteID) throws NotInDataBaseException;

    /**
     *
     * @param annotationID
     * @param principalID
     * @return access of the principalID w.r.t. annotationID, or null if the
     * access is not given
     */
    Access getAccess(Number annotationID, Number principalID);
    
    Access getPublicAttribute(Number annotationID);

    String getTypeOfPrincipalAccount(Number principalID);
    
    Principal getDataBaseAdmin() ;

    boolean canDo(Access access, Number principalID, Number annotationID);

    
    /// notebooks ///
    NotebookInfoList getNotebooks(Number prinipalID, Access access);

    boolean hasAccess(Number notebookID, Number principalID, Access access);

    ReferenceList getNotebooksOwnedBy(Number principalID);

    ReferenceList getPrincipals(Number notebookID, String access);

    Notebook getNotebook(Number notebookID);

    Number getNotebookOwner(Number notebookID);

    ReferenceList getAnnotationsForNotebook(Number notebookID, int startAnnotation, int maximumAnnotations, String orderedBy, boolean desc);

    /**
     * UPDATERS
     */
    boolean updateAccount(UUID principalExternalID, String account) throws NotInDataBaseException;

    /**
     *
     * @param annotation
     * @return 1 of the annotation if it is updated
     */
    int updateAnnotation(Annotation annotation) throws NotInDataBaseException;

    /**
     *
     * @param principalID
     * @param annotationBody
     * @return 1 of the annotation if it is updated
     */
    int updateAnnotationBody(Number internalID, AnnotationBody annotationBody);

    /**
     *
     * @param annotationID
     * @param principalID
     * @param access
     * @return # rows updated to the table "annotations_principals_accesss"
     * Sets the "access" for the "principalID" w.r.t. the annotation with
     * "annotationID".
     */
    int updateAnnotationPrincipalAccess(Number annotationID, Number principalID, Access access);

    /**
     *
     * @param annotationID
     * @param accessList
     * @return # of rows updated or added in the table
     * annotations_principals_accesss
     */
    int updatePermissions(Number annotationID, PermissionList permissionList) throws NotInDataBaseException ;
    
    int updatePublicAttribute(Number annotationID, Access publicAttribute);
    
    int updatePrincipal(Principal principal) throws NotInDataBaseException;

    
    int updateTargetCachedFragment(Number targetID, Number cachedID, String fragmentDescriptor) throws NotInDataBaseException;
    
    int updateCachedMetada(CachedRepresentationInfo cachedInfo)  throws NotInDataBaseException;
    
    public int updateCachedBlob(Number internalID, InputStream cachedBlob) throws IOException;
    
    
    /// notebooks ///
    boolean updateNotebookMetadata(Number notebookID, NotebookInfo upToDateNotebookInfo) throws NotInDataBaseException;

    boolean addAnnotationToNotebook(Number notebookID, Number annotationID);

    /**
     * ADDERS
     */
    /**
     *
     * @param targetID
     * @param cachedInfo
     * @param cachedBlob
     * @return result[0] = # updated rows in the table
     * "Targets_cached_representations" (must be 1 or 0). result[1] = the
     * internal ID of the added cached (a new one if "cached" was new for the
     * Data Base).
     */
    Number[] addCachedForTarget(Number targetID, String fragmentDescriptor, CachedRepresentationInfo cachedInfo, InputStream cachedBlob) throws NotInDataBaseException, IOException;

    /**
     *
     * @param annotationID
     * @param targets
     * @return map temporaryTargetID |--> TargetExternalID. Its domain is the
     * temporary IDs of all the new Targets. While adding a new Target a new
     * external ID is generated for it and it becomes the value of the map. The
     * TargetIDs which are already present in the DB are not in the domain. If
     * all Targets are old, then the map is empty.
     * @throws SQLException
     */
    Map<String, String> addTargetsForAnnotation(Number annotationID, List<TargetInfo> targets)  throws NotInDataBaseException;

    /**
     *
     * @param principalID
     * @param annotation
     * @return the internalId of the just added "annotation" (or null if it is
     * not added) by the owner "principalID". calls "addTargetsForAnnotation"
     * @throws SQLException
     */
    Number addPrincipalsAnnotation(Number ownerID, Annotation annotation) throws NotInDataBaseException;

    /**
     *
     * @param principal
     * @param remoteID is got from the server
     * @return the internal Id of the just added "principal", or null if it was not
     * added for some reason (already exists)
     * @throws SQLException
     */
    Number addPrincipal(Principal principal, String remoteID) throws NotInDataBaseException, PrincipalExists;

       /// notebooks ////
    Number createNotebook(Notebook notebook, Number ownerID) throws NotInDataBaseException;

    boolean createAnnotationInNotebook(Number notebookID, Annotation annotation, Number ownerID) throws NotInDataBaseException;

    public Principal createShibbolizedPrincipal(String remoteID);
    
    /**
     * DELETERS
     */
    
    int deleteAnnotationPrincipalAccess(Number annotationID, Number principalID); 
    
    /**
     *
     * @param principalID
     * @return # of affected rows in the table "principal". It is 1 if the
     * principalId is found and deleted; it is 0 if it is not found or not deleted,
     * e.g. because it is in use in the table
     * "annotationsPreincipalsAccesss"
     */
    int deletePrincipal(Number principalID) throws PrincipalCannotBeDeleted;

   
    int deleteCachedRepresentation(Number internalID);

    /**
     *
     * @param TargetID
     * @param cachedID
     * @return result[0] = # deleted rows in the table
     * "Targets_cached_representations" (1, or 0). result[1] = # deleted rows in
     * the table "cached_representation" (should be 0 if the cached
     * representation is in use by some other Target???).
     */
    int[] deleteCachedRepresentationOfTarget(Number TargetID, Number cachedID);

    /**
     *
     * @param targetID
     * @return result[0] = # deleted rows in the table
     * "targets_cached_representations". result[1] = # deleted rows in the table
     * "cached_representation".
     *
     */
    int[] deleteAllCachedRepresentationsOfTarget(Number targetID);

    int deleteTarget(Number internalID);
    
   
    /**
     *
     * @param annotationID
     * @return result[0] = # deleted rows in the table "annotation" (1 or 0).
     * result[1] = # deleted rows in the table
     * "annotations_principals_accesss". result[2] = # deleted rows in the
     * table "annotations_target_Targets". result[3] = # deleted rows in the
     * table "Target".
     * @throws SQLException
     */
    int[] deleteAnnotation(Number annotationID);

    /// notebooks ///
    boolean deleteNotebook(Number notebookID);

    //////// HELPERS for resources /////
    ResponseBody makeAnnotationResponseEnvelope(Number annotationID);

    ResponseBody makeNotebookResponseEnvelope(Number notebookID);

    ResponseBody makeAccessResponseEnvelope(Number resourceID, Resource resource);
}
