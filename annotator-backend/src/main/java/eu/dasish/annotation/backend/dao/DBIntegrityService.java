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

import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.NotebookInfoList;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.UserWithPermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.Target;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.User;
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
    Number getResourceInternalIdentifier(UUID externalID, Resource resource);

    Number getResourceInternalIdentifierFromURI(String uri, Resource resource);

    UUID getResourceExternalIdentifier(Number resourceID, Resource resource);

    String getResourceURI(Number resourceID, Resource resource);

    UserWithPermissionList getPermissions(Number resourceID, Resource resource);

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
     * bodies of which contain "text", -- current user has "access" (owner,
     * reader, writer) to them, -- namespace ???, -- owned by "owner", --
     * created after time-samp "after and before time-stamp "before".
     */
    List<Number> getFilteredAnnotationIDs(UUID ownerId, String link, String text, Number inloggedUserID, String access, String namespace, String after, String before);

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
     * bodies of which contain "text", -- current user has "access" (owner,
     * reader, writer) to them, -- namespace ???, -- owned by "owner", --
     * created after time-samp "after and before time-stamp "before".
     */
    AnnotationInfoList getFilteredAnnotationInfos(UUID ownerId, String word, String text, Number inloggedUserID, String access, String namespace, String after, String before);

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
     * "annotations_principals_permissions".
     * @throws SQLException
     */
    Annotation getAnnotation(Number annotationID);

    Number getAnnotationOwner(Number annotationID);

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

    List<String> getUsersWithNoInfo(Number annotationID);

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
     * @param userID
     * @return user with "userID"
     */
    User getUser(Number userID);

    /**
     *
     * @param eMail
     * @return user with e-mail "eMail"
     */
    User getUserByInfo(String eMail);

    String getUserRemoteID(Number internalID);

    Number getUserInternalIDFromRemoteID(String remoteID);

    /**
     *
     * @param annotationID
     * @param userID
     * @return permission of the userID w.r.t. annotationID, or null if the
     * permission is not given
     */
    Permission getPermission(Number annotationID, Number userID);

    String getTypeOfUserAccount(Number userID);

    boolean canRead(Number userID, Number annotationID);

    boolean canWrite(Number userID, Number annotationID);

    /// notebooks ///
    NotebookInfoList getNotebooks(Number prinipalID, String permission);

    boolean hasAccess(Number notebookID, Number principalID, Permission permission);

    ReferenceList getNotebooksOwnedBy(Number principalID);

    ReferenceList getPrincipals(Number notebookID, String permission);

    Notebook getNotebook(Number notebookID);

    Number getNotebookOwner(Number notebookID);

    ReferenceList getAnnotationsForNotebook(Number notebookID, int startAnnotation, int maximumAnnotations, String orderedBy, boolean desc);

    /**
     * UPDATERS
     */
    boolean updateAccount(UUID userExternalID, String account);

    /**
     *
     * @param annotation
     * @return 1 of the annotation if it is updated
     */
    int updateAnnotation(Annotation annotation);

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
     * Sets the "permission" for the "userID" w.r.t. the annotation with
     * "annotationID".
     */
    int updateAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission);

    /**
     *
     * @param annotationID
     * @param permissionList
     * @return # of rows updated or added in the table
     * annotations_principals_permissions
     */
    int updatePermissions(Number annotationID, UserWithPermissionList permissionList);

    Number updateUser(User user);

    /// notebooks ///
    boolean updateNotebookMetadata(Number notebookID, NotebookInfo upToDateNotebookInfo);

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
    Number[] addCachedForTarget(Number targetID, String fragmentDescriptor, CachedRepresentationInfo cachedInfo, InputStream cachedBlob);

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
    Map<String, String> addTargetsForAnnotation(Number annotationID, List<TargetInfo> targets);

    /**
     *
     * @param userID
     * @param annotation
     * @return the internalId of the just added "annotation" (or null if it is
     * not added) by the owner "userID". calls "addTargetsForAnnotation"
     * @throws SQLException
     */
    Number addUsersAnnotation(Number ownerID, Annotation annotation);

    /**
     *
     * @param user
     * @param remoteID is got from the server
     * @return the internal Id of the just added "user", or null if it was not
     * added for some reason (already exists)
     * @throws SQLException
     */
    Number addUser(User user, String remoteID);

    int addAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission);

    /// notebooks ////
    Number createNotebook(Notebook notebook, Number ownerID);

    boolean createAnnotationInNotebook(Number notebookID, Annotation annotation, Number ownerID);

    /**
     * DELETERS
     */
    /**
     *
     * @param userID
     * @return # of affected rows in the table "principal". It is 1 if the
     * userId is found and deleted; it is 0 if it is not found or not deleted,
     * e.g. because it is in use in the table
     * "annotationsPreincipalsPermissions"
     */
    int deleteUser(Number userID);

    /**
     *
     * @param userID
     * @return # of affected rows in the table "principal". It is 1 if the
     * userId is found and deleted; it is 0 if it is not found or not deleted,
     * e.g. because it is in use in the table
     * "annotationsPreincipalsPermissions"
     */
    int deleteUserSafe(Number userID);

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
    int[] deleteAllCachedRepresentationsOfTarget(Number versionID);

    int deleteTarget(Number internalID);

    /**
     *
     * @param annotationID
     * @return result[0] = # deleted rows in the table "annotation" (1 or 0).
     * result[1] = # deleted rows in the table
     * "annotations_principals_permissions". result[2] = # deleted rows in the
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

    ResponseBody makePermissionResponseEnvelope(Number resourceID, Resource resource);
}
