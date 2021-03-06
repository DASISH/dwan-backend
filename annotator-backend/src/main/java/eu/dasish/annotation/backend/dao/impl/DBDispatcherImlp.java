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

import eu.dasish.annotation.backend.ForbiddenException;
import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.backend.MatchMode;
import eu.dasish.annotation.backend.PrincipalCannotBeDeleted;
import eu.dasish.annotation.backend.PrincipalExists;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.CachedRepresentationDao;
import eu.dasish.annotation.backend.dao.DBDispatcher;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.ResourceDao;
import eu.dasish.annotation.backend.dao.TargetDao;
import eu.dasish.annotation.backend.dao.PrincipalDao;
import eu.dasish.annotation.schema.Action;
import eu.dasish.annotation.schema.ActionList;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationActionName;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.CachedRepresentationFragment;
import eu.dasish.annotation.schema.CachedRepresentationFragmentList;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.NotebookInfoList;
import eu.dasish.annotation.schema.TargetInfoList;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.PermissionActionName;
import eu.dasish.annotation.schema.PermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.Target;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.Principal;
import eu.dasish.annotation.schema.Permission;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.Number;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author olhsha
 */
public class DBDispatcherImlp implements DBDispatcher {

    @Autowired
    PrincipalDao principalDao;
    @Autowired
    CachedRepresentationDao cachedRepresentationDao;
    @Autowired
    TargetDao targetDao;
    @Autowired
    AnnotationDao annotationDao;
    @Autowired
    NotebookDao notebookDao;
    final static protected String admin = "admin";
    private static final Logger logger = LoggerFactory.getLogger(DBDispatcherImlp.class);

    //////////////////////////////////
    private ResourceDao getDao(Resource resource) {
        switch (resource) {
            case PRINCIPAL:
                return principalDao;
            case ANNOTATION:
                return annotationDao;
            case TARGET:
                return targetDao;
            case CACHED_REPRESENTATION:
                return cachedRepresentationDao;
            case NOTEBOOK:
                return notebookDao;
            default:
                return null;
        }
    }

    @Override
    public void setResourcesPaths(String relServiceURI) {
        principalDao.setResourcePath(relServiceURI + "/principals/");
        cachedRepresentationDao.setResourcePath(relServiceURI + "/cached/");
        targetDao.setResourcePath(relServiceURI + "/targets/");
        annotationDao.setResourcePath(relServiceURI + "/annotations/");
        notebookDao.setResourcePath(relServiceURI + "/notebooks/");
    }

    ///////////// GETTERS //////////////////////////
    @Override
    public Number getResourceInternalIdentifier(UUID externalID, Resource resource) throws NotInDataBaseException {
        return this.getDao(resource).getInternalID(externalID);
    }

    @Override
    public UUID getResourceExternalIdentifier(Number resourceID, Resource resource) {
        return this.getDao(resource).getExternalID(resourceID);
    }

    @Override
    public Annotation getAnnotation(Number annotationID) {
        Annotation result = annotationDao.getAnnotationWithoutTargetsAndPemissionList(annotationID);
        result.setOwnerHref(principalDao.getHrefFromInternalID(annotationDao.getOwner(annotationID)));
        List<Number> targetIDs = targetDao.getTargetIDs(annotationID);
        TargetInfoList sis = new TargetInfoList();
        for (Number targetID : targetIDs) {
            TargetInfo targetInfo = this.getTargetInfoFromTarget(targetDao.getTarget(targetID));
            sis.getTargetInfo().add(targetInfo);
        }
        result.setTargets(sis);
        this.fillInPermissionList(result.getPermissions().getPermission(), annotationID, Resource.ANNOTATION);
        return result;
    }

    @Override
    public Number getAnnotationOwnerID(Number annotationID) {
        return annotationDao.getOwner(annotationID);
    }

    @Override
    public Principal getAnnotationOwner(Number annotationID) {
        Number ownerID = annotationDao.getOwner(annotationID);
        return principalDao.getPrincipal(ownerID);
    }

    ///////////////////////////////////////////////////
    private void fillInPermissionList(List<Permission> listPermissions, Number resourceID, Resource resource) {
        List<Map<Number, String>> principalsAccesss = this.getDao(resource).getPermissions(resourceID);
        for (Map<Number, String> principalAccess : principalsAccesss) {
            Number[] principal = new Number[1];
            principalAccess.keySet().toArray(principal);
            Permission permission = new Permission();
            permission.setPrincipalHref(principalDao.getHrefFromInternalID(principal[0]));
            permission.setLevel(Access.fromValue(principalAccess.get(principal[0])));
            listPermissions.add(permission);
        }
    }

    @Override
    public PermissionList getPermissions(Number resourceID, Resource resource) {
        PermissionList result = new PermissionList();
        result.setPublic(this.getDao(resource).getPublicAttribute(resourceID));
        this.fillInPermissionList(result.getPermission(), resourceID, resource);
        return result;
    }

////////////////////////////////////////////////////////////////////////
    @Override
    public List<Number> getFilteredAnnotationIDs(UUID ownerId, String link, MatchMode matchMode, String text, Number inloggedPrincipalID, String accessMode, String namespace, String after, String before) throws NotInDataBaseException {

        Number ownerID;

        if (ownerId != null) {
            if (accessMode.equals("owner")) { // inloggedUser is the owner of the annotations
                if (!ownerId.equals(principalDao.getExternalID(inloggedPrincipalID))) {
                    logger.info("The inlogged principal is demanded to be the owner of the annotations, however the expected owner is different and has the UUID " + ownerId.toString());
                    return new ArrayList<Number>();
                } else {
                    ownerID = inloggedPrincipalID;
                }
            } else {
                ownerID = principalDao.getInternalID(ownerId);
            }

        } else {
            if (accessMode.equals("owner")) {
                ownerID = inloggedPrincipalID;
            } else {
                ownerID = null;
            }
        }


        //Filtering on the columns  of the annotation table 
        List<Number> annotationIDs = annotationDao.getFilteredAnnotationIDs(ownerID, text, namespace, after, before);


        // Filetring on accessMode, the junction table
        if (annotationIDs != null) {
            if (!annotationIDs.isEmpty()) {
                if (!accessMode.equals("owner")) {
                    Access access = Access.fromValue(accessMode);
                    if (access.equals(Access.NONE)) {
                        List<Number> annotationIDsfiletered = new ArrayList<Number>();
                        Access accessCurrent;
                        for (Number annotationID : annotationIDs) {
                            accessCurrent = annotationDao.getAccess(inloggedPrincipalID, annotationID);
                            if (accessCurrent.equals(Access.NONE)) {
                                annotationIDsfiletered.add(annotationID);
                            }
                        }
                        annotationIDs = annotationIDsfiletered; // yeaahhh I'm relying on garbage collector here                         
                    } else {
                        List<Number> annotationIDsAccess = annotationDao.getAnnotationIDsPermissionAtLeast(inloggedPrincipalID, access);
                        List<Number> annotationIDsPublic = annotationDao.getAnnotationIDsPublicAtLeast(access);
                        List<Number> annotationIDsOwned = annotationDao.getFilteredAnnotationIDs(inloggedPrincipalID, text, namespace, after, before);
                        int check1 = this.addAllNoRepetitions(annotationIDsAccess, annotationIDsPublic);
                        int check2 = this.addAllNoRepetitions(annotationIDsAccess, annotationIDsOwned);
                        annotationIDs.retainAll(annotationIDsAccess);// intersection
                    }
                }
            }

            // filtering on reference        
            return this.filterAnnotationIDsOnReference(annotationIDs, link, matchMode);
        }

        return annotationIDs;

    }

    /// helpers ///
    private List<Number> filterAnnotationIDsOnReference(List<Number> annotationIDs, String link, MatchMode matchMode) {
        if (link != null) {
            if (!link.isEmpty()) {
                if (annotationIDs != null) {
                    String partiallyEncoded = this.encodeURLNoSlashEncoded(link);
                    String urlEncoded = null;
                    try {
                        urlEncoded = URLEncoder.encode(link, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        logger.debug(e.toString());
                    }

                    List<Number> result = new ArrayList();
                    for (Number annotationID : annotationIDs) {
                        List<Number> targets = targetDao.getTargetIDs(annotationID);
                        for (Number targetID : targets) {
                            if (!result.contains(annotationID)) {
                                String linkRunner = targetDao.getLink(targetID);
                                if (matchCriterium(linkRunner, link, matchMode) || matchCriterium(linkRunner, partiallyEncoded, matchMode)) {
                                    result.add(annotationID);
                                } else {
                                    if (urlEncoded != null) {
                                        if (matchCriterium(linkRunner, urlEncoded, matchMode)) {
                                            result.add(annotationID);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return result;
                }
            }
        }
        return annotationIDs;
    }

    private boolean matchCriterium(String currentString, String pattern, MatchMode matchMode) {
        switch (matchMode) {
            case EXACT:
                return currentString.equals(pattern);
            case STARTS_WITH:
                return currentString.startsWith(pattern);
            case ENDS_WITH:
                return currentString.endsWith(pattern);
            case CONTAINS:
                return currentString.contains(pattern);
            default:
                return false;
        }
    }

    public int addAllNoRepetitions(List<Number> list, List<Number> listToAdd) {
        int result = 0;
        if (list != null) {
            if (listToAdd != null) {
                for (Number element : listToAdd) {
                    if (!list.contains(element)) {
                        list.add(element);
                        result++;
                    }
                }
            }
        } else {
            if (listToAdd != null) {
                list = listToAdd;
                result = listToAdd.size();
            }
        }
        return result;
    }

    private String encodeURLNoSlashEncoded(String string) {
        String[] split = string.split("/");
        StringBuilder result = new StringBuilder(split[0]);
        for (int i = 1; i < split.length; i++) {
            try {
                result.append("/").append(URLEncoder.encode(split[i], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                result.append("/").append(split[i]);
                logger.debug(e.toString());
            }
        }
        return result.toString();
    }

    //////
    @Override
    public ReferenceList getAnnotationTargets(Number annotationID) {
        ReferenceList result = new ReferenceList();
        List<Number> targetIDs = targetDao.getTargetIDs(annotationID);
        for (Number targetID : targetIDs) {
            result.getHref().add(targetDao.getHrefFromInternalID(targetID));
        }
        return result;
    }

    @Override
    public List<String> getTargetsWithNoCachedRepresentation(Number annotationID) {

        List<String> result = new ArrayList<String>();
        List<Number> targetIDs = targetDao.getTargetIDs(annotationID);
        for (Number targetID : targetIDs) {
            List<Number> versions = cachedRepresentationDao.getCachedRepresentationsForTarget(targetID);
            if (versions.isEmpty()) {
                result.add(targetDao.getHrefFromInternalID(targetID));
            }
        }
        return result;
    }

    @Override
    public List<String> getPrincipalsWithNoInfo(Number annotationID) {
        List<String> result = new ArrayList<String>();
        List<Map<Number, String>> principalsWithAccesss = annotationDao.getPermissions(annotationID);
        for (Map<Number, String> permission : principalsWithAccesss) {
            Number[] principalID = new Number[1];
            permission.keySet().toArray(principalID);
            Principal principal = principalDao.getPrincipal(principalID[0]);
            if (principal.getDisplayName() == null || principal.getDisplayName().trim().isEmpty() || principal.getEMail() == null || principal.getEMail().trim().isEmpty()) {
                result.add(principalDao.getHrefFromInternalID(principalID[0]));

            }
        }
        return result;
    }

    @Override
    public AnnotationInfoList getFilteredAnnotationInfos(UUID ownerId, String link, MatchMode matchMode, String text, Number inloggedPrincipalID, String access, String namespace, String after, String before) throws NotInDataBaseException {
        List<Number> annotationIDs = this.getFilteredAnnotationIDs(ownerId, link, matchMode, text, inloggedPrincipalID, access, namespace, after, before);
        AnnotationInfoList result = new AnnotationInfoList();
        for (Number annotationID : annotationIDs) {
            AnnotationInfo annotationInfo = annotationDao.getAnnotationInfoWithoutTargetsAndOwner(annotationID);
            annotationInfo.setTargets(this.getAnnotationTargets(annotationID));
            annotationInfo.setOwnerHref(principalDao.getHrefFromInternalID(annotationDao.getOwner(annotationID)));
            result.getAnnotationInfo().add(annotationInfo);
        }
        return result;
    }

    @Override
    public AnnotationInfoList getAllAnnotationInfos() {
        List<Number> annotationIDs = annotationDao.getAllAnnotationIDs();
        AnnotationInfoList result = new AnnotationInfoList();
        for (Number annotationID : annotationIDs) {
            Number ownerID = annotationDao.getOwner(annotationID);
            ReferenceList targets = this.getAnnotationTargets(annotationID);
            AnnotationInfo annotationInfo = annotationDao.getAnnotationInfoWithoutTargetsAndOwner(annotationID);
            annotationInfo.setTargets(targets);
            annotationInfo.setOwnerHref(principalDao.getHrefFromInternalID(ownerID));
            result.getAnnotationInfo().add(annotationInfo);
        }
        return result;

    }

    // TODO unit test
    @Override
    public Target getTarget(Number internalID) {
        Target result = targetDao.getTarget(internalID);
        result.setSiblingTargets(this.getTargetsForTheSameLinkAs(internalID));
        Map<Number, String> cachedIDsFragments = targetDao.getCachedRepresentationFragmentPairs(internalID);
        CachedRepresentationFragmentList cachedRepresentationFragmentList = new CachedRepresentationFragmentList();
        for (Number key : cachedIDsFragments.keySet()) {
            CachedRepresentationFragment cachedRepresentationFragment = new CachedRepresentationFragment();
            cachedRepresentationFragment.setHref(cachedRepresentationDao.getHrefFromInternalID(key));
            cachedRepresentationFragment.setFragmentString(cachedIDsFragments.get(key));
            cachedRepresentationFragmentList.getCached().add(cachedRepresentationFragment);
        }
        result.setCachedRepresentations(cachedRepresentationFragmentList);
        return result;
    }

    
    @Override
    public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID) {
        return cachedRepresentationDao.getCachedRepresentationInfo(internalID);
    }

    
    @Override
    public InputStream getCachedRepresentationBlob(Number cachedID) {
        return cachedRepresentationDao.getCachedRepresentationBlob(cachedID);
    }

    @Override
    public ReferenceList getTargetsForTheSameLinkAs(Number targetID) {
        List<Number> targetIDs = targetDao.getTargetsForLink(targetDao.getLink(targetID));
        ReferenceList referenceList = new ReferenceList();
        for (Number siblingID : targetIDs) {
            if (!siblingID.equals(targetID)) {
                referenceList.getHref().add(targetDao.getHrefFromInternalID(siblingID));
            }
        }
        return referenceList;
    }

    @Override
    public Principal getPrincipal(Number principalID) {
        return principalDao.getPrincipal(principalID);
    }

    @Override
    public Principal getPrincipalByInfo(String eMail) throws NotInDataBaseException {
        return principalDao.getPrincipalByInfo(eMail);
    }

    @Override
    public String getPrincipalRemoteID(Number internalID) {
        return principalDao.getRemoteID(internalID);
    }

    @Override
    public Access getAccess(Number annotationID, Number principalID) {
        Access publicAttribute = annotationDao.getPublicAttribute(annotationID);
        Access access = annotationDao.getAccess(annotationID, principalID);
        if (publicAttribute.equals(Access.NONE)) {
            return access;
        } else {
            if (publicAttribute.equals(Access.READ)) {
                if (access.equals(Access.NONE)) {
                    return Access.READ;
                } else {
                    return access;
                }
            } else {
                if (publicAttribute.equals(Access.WRITE)) {
                    if (access.equals(Access.NONE) || access.equals(Access.READ)) {
                        return Access.WRITE;
                    } else {
                        return access;
                    }
                } else {
                    if (publicAttribute.equals(Access.ALL)) {
                        return Access.ALL;
                    } else {
                        logger.error("Database problem: the value of public attribute is not a proper Access value: " + publicAttribute.value());
                        return access;
                    }
                }
            }
        }
    }

    @Override
    public Access getPublicAttribute(Number annotationID) {
        return annotationDao.getPublicAttribute(annotationID);
    }

    @Override
    public Number getPrincipalInternalIDFromRemoteID(String remoteID) throws NotInDataBaseException {
        return principalDao.getPrincipalInternalIDFromRemoteID(remoteID);
    }

    @Override
    public UUID getPrincipalExternalIDFromRemoteID(String remoteID) throws NotInDataBaseException {
        return principalDao.getPrincipalExternalIDFromRemoteID(remoteID);
    }

    @Override
    public String getTypeOfPrincipalAccount(Number principalID) {
        return principalDao.getTypeOfPrincipalAccount(principalID);
    }

    @Override
    public Principal getDataBaseAdmin() {
        return principalDao.getPrincipal(principalDao.getDBAdminID());
    }

    // !!!so far implemented only for annotations!!!
    @Override
    public boolean canDo(Access action, Number principalID, Number resourceID, Resource resource) {

        switch (resource) {
            case ANNOTATION: {
                if (principalID.equals(annotationDao.getOwner(resourceID)) || principalDao.getTypeOfPrincipalAccount(principalID).equals(admin)) {
                    return true;
                }
                Access access = this.getAccess(resourceID, principalID);
                return this.greaterOrEqual(access, action);
            }
            case CACHED_REPRESENTATION: {
                return true;
            }
            case TARGET: {
                return true;
            }
            case PRINCIPAL: {
                return true;
            }
            default:
                return false;
        }

    }

    private boolean greaterOrEqual(Access access, Access action) {

        if (access.equals(Access.ALL)) {
            return true;
        }

        if (access.equals(Access.WRITE) && (action.equals(Access.READ) || action.equals(Access.WRITE))) {
            return true;
        }
        if (access.equals(Access.READ) && action.equals(Access.READ)) {
            return true;
        }
        return false;
    }
////// noetbooks ///////
/// TODO update for having attribute public!!! /////

    @Override
    public NotebookInfoList getNotebooks(Number principalID, Access access) {
        NotebookInfoList result = new NotebookInfoList();
        if (access.equals(Access.READ) || access.equals(Access.WRITE) || access.equals(Access.ALL)) {
            List<Number> notebookIDs = notebookDao.getNotebookIDs(principalID, access);
            for (Number notebookID : notebookIDs) {
                NotebookInfo notebookInfo = notebookDao.getNotebookInfoWithoutOwner(notebookID);
                Number ownerID = notebookDao.getOwner(notebookID);
                notebookInfo.setOwnerHref(principalDao.getHrefFromInternalID(ownerID));
                result.getNotebookInfo().add(notebookInfo);
            }
        }
        return result;
    }

    @Override
    public boolean hasAccess(Number notebookID, Number principalID, Access access) {
        List<Number> notebookIDs = notebookDao.getNotebookIDs(principalID, access);
        return notebookIDs.contains(notebookID);
    }

    @Override
    public ReferenceList getNotebooksOwnedBy(Number principalID) {
        ReferenceList result = new ReferenceList();
        List<Number> notebookIDs = notebookDao.getNotebookIDsOwnedBy(principalID);
        for (Number notebookID : notebookIDs) {
            String reference = notebookDao.getHrefFromInternalID(notebookID);
            result.getHref().add(reference);
        }
        return result;
    }

    @Override
    public ReferenceList getPrincipals(Number notebookID, String access) {
        ReferenceList result = new ReferenceList();
        List<Number> principalIDs = principalDao.getPrincipalIDsWithAccessForNotebook(notebookID, Access.fromValue(access));
        for (Number principalID : principalIDs) {
            String reference = principalDao.getHrefFromInternalID(principalID);
            result.getHref().add(reference);
        }
        return result;
    }

    @Override
    public Notebook getNotebook(Number notebookID) {
        Notebook result = notebookDao.getNotebookWithoutAnnotationsAndAccesssAndOwner(notebookID);

        result.setOwnerRef(principalDao.getHrefFromInternalID(notebookDao.getOwner(notebookID)));

        ReferenceList annotations = new ReferenceList();
        List<Number> annotationIDs = annotationDao.getAnnotations(notebookID);
        for (Number annotationID : annotationIDs) {
            annotations.getHref().add(annotationDao.getHrefFromInternalID(annotationID));
        }
        result.setAnnotations(annotations);

        PermissionList ups = new PermissionList();
        List<Access> accesss = new ArrayList<Access>();
        accesss.add(Access.READ);
        accesss.add(Access.WRITE);
        accesss.add(Access.ALL);
        for (Access access : accesss) {
            List<Number> principals = principalDao.getPrincipalIDsWithAccessForNotebook(notebookID, access);
            if (principals != null) {
                for (Number principal : principals) {
                    Permission up = new Permission();
                    up.setPrincipalHref(principalDao.getHrefFromInternalID(principal));
                    up.setLevel(access);
                    ups.getPermission().add(up);
                }
            }
        }
        result.setPermissions(ups);
        return result;
    }

    @Override
    public Number getNotebookOwner(Number notebookID) {
        return notebookDao.getOwner(notebookID);
    }

    /////////////////////////////////////////////////////////////
 
    @Override
    public ReferenceList getAnnotationsForNotebook(Number notebookID, int startAnnotation, int maximumAnnotations, String orderedBy, boolean desc) {
        List<Number> annotationIDs = annotationDao.getAnnotations(notebookID);

        if (startAnnotation < 0) {
            logger.info("Variable's startAnnotation value " + startAnnotation + " is invalid. I will return null.");
            return null;
        }

        if (maximumAnnotations < -1) {
            logger.info("Variable's maximumAnnotations value " + maximumAnnotations + " is invalid. I will return null.");
            return null;
        }

        int offset = startAnnotation - 1;
        String direction = desc ? "DESC" : "ASC";
        List<Number> selectedAnnotIDs = annotationDao.sublistOrderedAnnotationIDs(annotationIDs, offset, maximumAnnotations, orderedBy, direction);
        ReferenceList references = new ReferenceList();
        for (Number annotationID : selectedAnnotIDs) {
            references.getHref().add(annotationDao.getHrefFromInternalID(annotationID));
        }
        return references;
    }

    ///// UPDATERS /////////////////
    @Override
    public boolean updateResourceIdentifier(Resource resource, UUID oldIdentifier, UUID newIdentifier) {
        switch (resource) {
            case PRINCIPAL:
                return principalDao.updateResourceIdentifier(oldIdentifier, newIdentifier);
            case ANNOTATION:
                return annotationDao.updateResourceIdentifier(oldIdentifier, newIdentifier);
            case TARGET:
                return targetDao.updateResourceIdentifier(oldIdentifier, newIdentifier);
            case CACHED_REPRESENTATION:
                return cachedRepresentationDao.updateResourceIdentifier(oldIdentifier, newIdentifier);
            case NOTEBOOK:
                return notebookDao.updateResourceIdentifier(oldIdentifier, newIdentifier);
            default:
                return false;
        }
    }

    @Override
    public boolean updateAccount(UUID principalExternalID, String account) throws NotInDataBaseException {
        return principalDao.updateAccount(principalExternalID, account);
    }

    @Override
    public int updatePermission(Number annotationID, Number principalID, Access access) {
        int result;
        if (access != null) {
            Boolean checkAccess = annotationDao.hasExplicitAccess(annotationID, principalID);
            if (checkAccess) {
                result = annotationDao.updatePermission(annotationID, principalID, access);
            } else {
                result = annotationDao.addPermission(annotationID, principalID, access);
            }
        } else {
            result = annotationDao.deletePermission(annotationID, principalID);
        }
        return result;
    }

    @Override
    public int updatePublicAttribute(Number annotationID, Access publicAttribute) {
        return annotationDao.updatePublicAccess(annotationID, publicAttribute);
    }

    @Override
    public int updateOrAddPermissions(Number annotationID, PermissionList permissionList) throws NotInDataBaseException {
        annotationDao.updatePublicAccess(annotationID, permissionList.getPublic());
        List<Permission> permissions = permissionList.getPermission();
        int result = 0;
        for (Permission permission : permissions) {
            Number principalID = principalDao.getInternalIDFromHref(permission.getPrincipalHref());
            Access access = permission.getLevel();
            Boolean checkAccess = annotationDao.hasExplicitAccess(annotationID, principalID);
            if (checkAccess) {
                result = result + annotationDao.updatePermission(annotationID, principalID, access);
            } else {
                result = result + annotationDao.addPermission(annotationID, principalID, access);
            }
        }
        return result;
    }

// TODO: optimize (not chnanged targets should not be deleted)
    @Override
    public int updateAnnotation(Annotation annotation, String remoteUser) throws NotInDataBaseException, ForbiddenException {

        Number annotationID = annotationDao.getInternalID(UUID.fromString(annotation.getId()));
        Number ownerID = principalDao.getInternalIDFromHref(annotation.getOwnerHref());
        Number remoteUserID = principalDao.getPrincipalInternalIDFromRemoteID(remoteUser);
        
        boolean isOwner = ownerID.equals(remoteUserID);
        boolean hasAllAccess = annotationDao.getAccess(annotationID, remoteUserID).equals(Access.ALL);
        boolean isAdmin = remoteUserID.equals(principalDao.getDBAdminID());
        boolean weakPrincipal = (!isOwner && !hasAllAccess && !isAdmin);

        if (weakPrincipal) { // we need to check if permissions are intact
            if (!(annotation.getPermissions().getPublic()).equals(annotationDao.getPublicAttribute(annotationID))) {
                throw new ForbiddenException("The inlogged user does not have rights to update 'public' attribute in this annotation.");
            }
            List<Map<Number, String>> permissionsDB = annotationDao.getPermissions(annotationID);
            if (!this.permissionsIntact(annotation.getPermissions().getPermission(), permissionsDB))  {
                throw new ForbiddenException("The inlogged user does not have rights to update permissions in this annotation.");
            }
        }


        int updatedAnnotations = annotationDao.updateAnnotation(annotation, annotationID, ownerID);
        int deletedTargets = annotationDao.deleteAllAnnotationTarget(annotationID);
        int addedTargets = this.addTargets(annotation, annotationID);
        if (!weakPrincipal) { // if weak permissions reach this point then permissions are the same
            int changedPermissions = this.updateOrAddPermissions(annotationID, annotation.getPermissions());
        }
        return updatedAnnotations;
    }

    private boolean permissionsIntact(List<Permission> permissionsInput, List<Map<Number, String>> permissionsDB) throws NotInDataBaseException{
        if (permissionsInput == null || permissionsInput.isEmpty()) {
            return true;
        }
        
        if (permissionsDB == null || permissionsDB.isEmpty()) {
            return false;
        }
        
        for(Permission permission:permissionsInput) {
            Number principalID = principalDao.getInternalIDFromHref(permission.getPrincipalHref());
            String accessLevel = permission.getLevel().value();
            Map current = new HashMap<Number, String>();
            current.put(principalID, accessLevel);
            int index = permissionsDB.indexOf(current);
            if (index>-1) {
             if (!accessLevel.equals(permissionsDB.get(index).get(principalID)))   {
                 return false;
             } 
            } else {
                if (!accessLevel.equals(Access.NONE.value())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int updateAnnotationBody(Number internalID, AnnotationBody annotationBody) {
        String[] body = annotationDao.retrieveBodyComponents(annotationBody);
        return annotationDao.updateAnnotationBody(internalID, body[0], body[1], annotationBody.getXmlBody() != null);
    }

    @Override
    public int updateAnnotationHeadline(Number internalID, String newHeader) {
        return annotationDao.updateAnnotationHeadline(internalID, newHeader);
    }

    @Override
    public Number updatePrincipal(Principal principal) throws NotInDataBaseException {
        return principalDao.updatePrincipal(principal);
    }

    @Override
    public int updateTargetCachedFragment(Number targetID, Number cachedID, String fragmentDescriptor){
        return targetDao.updateTargetCachedRepresentationFragment(targetID, cachedID, fragmentDescriptor);
    }

    @Override
    public int updateCachedMetada(CachedRepresentationInfo cachedInfo) throws NotInDataBaseException {
        Number internalID = cachedRepresentationDao.getInternalID(UUID.fromString(cachedInfo.getId()));
        return cachedRepresentationDao.updateCachedRepresentationMetadata(internalID, cachedInfo);
    }

    @Override
    public int updateCachedBlob(Number internalID, InputStream cachedBlob) throws IOException {
        return cachedRepresentationDao.updateCachedRepresentationBlob(internalID, cachedBlob);
    }

    /// notebooks ///
    @Override
    public boolean updateNotebookMetadata(Number notebookID, NotebookInfo upToDateNotebookInfo) throws NotInDataBaseException {
        Number ownerID = principalDao.getInternalIDFromHref(upToDateNotebookInfo.getOwnerHref());
        return notebookDao.updateNotebookMetadata(notebookID, upToDateNotebookInfo.getTitle(), ownerID);
    }

    @Override
    public boolean addAnnotationToNotebook(Number notebookID, Number annotationID) {
        return notebookDao.addAnnotationToNotebook(notebookID, annotationID);
    }

    /////////////// ADDERS  /////////////////////////////////
    @Override
    public Number[] addCachedForTarget(Number targetID, String fragmentDescriptor, CachedRepresentationInfo cachedInfo, InputStream cachedBlob) throws NotInDataBaseException, IOException {
        Number[] result = new Number[2];
        try {
            result[1] = cachedRepresentationDao.addCachedRepresentation(cachedInfo, cachedBlob);
        } catch (NotInDataBaseException e1) {
            logger.info("Something wrong went while adding cached.");
            throw e1;
        }

        result[0] = targetDao.addTargetCachedRepresentation(targetID, result[1], fragmentDescriptor);
        return result;

    }

    @Override
    public Map<String, String> addTargetsForAnnotation(Number annotationID, List<TargetInfo> targets) throws NotInDataBaseException {
        Map<String, String> result = new HashMap<String, String>();
        for (TargetInfo targetInfo : targets) {
            try {
                Number targetIDRunner = targetDao.getInternalIDFromHref(targetInfo.getHref());
                int affectedRows = annotationDao.addAnnotationTarget(annotationID, targetIDRunner);
            } catch (NotInDataBaseException e) {
                Target newTarget = this.createFreshTarget(targetInfo);
                Number targetID = targetDao.addTarget(newTarget);
                String targetTemporaryId = targetInfo.getHref();
                result.put(targetTemporaryId, targetDao.getHrefFromInternalID(targetID));
                int affectedRows = annotationDao.addAnnotationTarget(annotationID, targetID);
            }
        }
        return result;
    }

    @Override
    public Number addPrincipalsAnnotation(Number ownerID, Annotation annotation) throws NotInDataBaseException {
        Number annotationID = annotationDao.addAnnotation(annotation, ownerID);
        int affectedAnnotRows = this.addTargets(annotation, annotationID);
        int addedPrincipalsAccesss = this.addPermissions(annotation.getPermissions().getPermission(), annotationID);
        return annotationID;
    }

    @Override
    public Number addPrincipal(Principal principal, String remoteID) throws NotInDataBaseException, PrincipalExists {
        if (principalDao.principalExists(remoteID)) {
            throw new PrincipalExists(remoteID);
        } else {
            return principalDao.addPrincipal(principal, remoteID);
        }
    }

    //////////// notebooks //////
    @Override
    public Number createNotebook(Notebook notebook, Number ownerID) throws NotInDataBaseException {
        Number notebookID = notebookDao.createNotebookWithoutAccesssAndAnnotations(notebook, ownerID);
        boolean updateOwner = notebookDao.setOwner(notebookID, ownerID);
        List<Permission> permissions = notebook.getPermissions().getPermission();
        for (Permission permission : permissions) {
            Number principalID = principalDao.getInternalIDFromHref(permission.getPrincipalHref());
            Access access = permission.getLevel();
            boolean updateAccesss = notebookDao.addAccessToNotebook(notebookID, principalID, access);
        }
        return notebookID;
    }

    @Override
    public boolean createAnnotationInNotebook(Number notebookID, Annotation annotation, Number ownerID) throws NotInDataBaseException {
        Number newAnnotationID = this.addPrincipalsAnnotation(ownerID, annotation);
        return notebookDao.addAnnotationToNotebook(notebookID, newAnnotationID);
    }

    @Override
    public int addSpringUser(String username, String password, int strength, String salt) {
        int users = principalDao.addSpringUser(username, password, strength, salt);
        int authorities = principalDao.addSpringAuthorities(username);
        return users + authorities;
    }

    ////////////// DELETERS //////////////////
    @Override
    public int deletePrincipal(Number principalID) throws PrincipalCannotBeDeleted {
        return principalDao.deletePrincipal(principalID);
    }

    @Override
    public int deleteCachedRepresentation(Number internalID) {

        if (targetDao.cachedIsInUse(internalID)) {
            logger.debug("Cached Repr. is in use, and cannot be deleted.");
            return 0;
        }

        return cachedRepresentationDao.deleteCachedRepresentation(internalID);
    }

    @Override
    public int[] deleteCachedRepresentationOfTarget(Number targetID, Number cachedID) {
        int[] result = new int[2];
        result[0] = targetDao.deleteTargetCachedRepresentation(targetID, cachedID);
        if (result[0] > 0) {
            result[1] = cachedRepresentationDao.deleteCachedRepresentation(cachedID);
        } else {
            result[1] = 0;

        }
        return result;
    }

    @Override
    public int[] deleteAllCachedRepresentationsOfTarget(Number targetID) {
        int[] result = new int[2];
        result[0] = 0;
        result[1] = 0;
        List<Number> cachedIDs = cachedRepresentationDao.getCachedRepresentationsForTarget(targetID);
        for (Number cachedID : cachedIDs) {
            int[] currentResult = this.deleteCachedRepresentationOfTarget(targetID, cachedID);
            result[0] = result[0] + currentResult[0];
            result[1] = result[1] + currentResult[1];
        }
        return result;
    }

    @Override
    public int[] deleteAnnotation(Number annotationID) {
        int[] result = new int[5];
        result[1] = annotationDao.deletePermissions(annotationID);
        List<Number> targetIDs = targetDao.getTargetIDs(annotationID);
        result[2] = annotationDao.deleteAllAnnotationTarget(annotationID);
        result[3] = 0;
        if (targetIDs != null) {
            for (Number targetID : targetIDs) {
                this.deleteAllCachedRepresentationsOfTarget(targetID);
                result[3] = result[3] + this.deleteTarget(targetID);

            }
        }

        result[4] = annotationDao.deleteAnnotationFromAllNotebooks(annotationID);

        result[0] = annotationDao.deleteAnnotation(annotationID);
        return result;
    }

    @Override
    public int deleteTarget(Number internalID) {
        if (annotationDao.targetIsInUse(internalID)) {
            logger.debug("The target is in use, and cannot be deleted.");
            return 0;
        }
        return targetDao.deleteTarget(internalID);

    }

    @Override
    public boolean deleteNotebook(Number notebookID) {
        if (notebookDao.deleteAllAccesssForNotebook(notebookID) || notebookDao.deleteAllAnnotationsFromNotebook(notebookID)) {
            return notebookDao.deleteNotebook(notebookID);
        } else {
            return false;
        }
    }

    @Override
    public int deleteAnnotationPrincipalAccess(Number annotationID, Number principalID) {
        return annotationDao.deletePermission(annotationID, principalID);
    }
////////////// HELPERS ////////////////////
    ////////////////////////////////////////

    @Override
    public ResponseBody makeAnnotationResponseEnvelope(Number annotationID) {
        ResponseBody result = new ResponseBody();
        Annotation annotation = this.getAnnotation(annotationID);
        result.setAnnotation(annotation);
        List<String> targetsNoCached = this.getTargetsWithNoCachedRepresentation(annotationID);
        ActionList actionList = new ActionList();
        result.setActionList(actionList);
        actionList.getAction().addAll(makeActionList(targetsNoCached, AnnotationActionName.CREATE_CACHED_REPRESENTATION.value()));
        return result;
    }

    @Override
    public ResponseBody makeNotebookResponseEnvelope(Number notebookID) {
        ResponseBody result = new ResponseBody();
        result.setPermissions(null);
        Notebook notebook = this.getNotebook(notebookID);
        result.setNotebook(notebook);
        return result;
    }

    @Override
    public ResponseBody makeAccessResponseEnvelope(Number resourceID, Resource resource) {
        ResponseBody result = new ResponseBody();
        PermissionList permissions = this.getPermissions(resourceID, resource);
        result.setPermissions(permissions);
        List<String> principalsWithNoInfo = this.getPrincipalsWithNoInfo(resourceID);
        ActionList actionList = new ActionList();
        result.setActionList(actionList);
        actionList.getAction().addAll(makeActionList(principalsWithNoInfo, PermissionActionName.PROVIDE_PRINCIPAL_INFO.value()));
        return result;
    }

    private List<Action> makeActionList(List<String> resourceURIs, String message) {
        if (resourceURIs != null) {
            if (resourceURIs.isEmpty()) {
                return (new ArrayList<Action>());
            } else {
                List<Action> result = new ArrayList<Action>();
                for (String resourceURI : resourceURIs) {
                    Action action = new Action();
                    result.add(action);
                    action.setMessage(message);
                    action.setObject(resourceURI);
                }
                return result;
            }
        } else {
            return null;
        }
    }

    @Override
    public UUID getPrincipalExternalIdFromName(String fullName) throws NotInDataBaseException {
        return principalDao.getExternalIdFromName(fullName);
    }

    @Override
    public List<UUID> getAnnotationExternalIdsFromHeadline(String headline) {
        return annotationDao.getExternalIdFromHeadline(headline);
    }

    @Override
    public List<Number> getAnnotationInternalIDsFromHeadline(String headline) {
        return annotationDao.getInternalIDsFromHeadline(headline);
    }

    //// privee ///
    private Target createFreshTarget(TargetInfo targetInfo) {
        Target target = new Target();
        target.setLink(targetInfo.getLink());
        target.setVersion(targetInfo.getVersion());
        return target;
    }

    private int addTargets(Annotation annotation, Number annotationID) throws NotInDataBaseException {
        List<TargetInfo> targets = annotation.getTargets().getTargetInfo();
        Map<String, String> targetIdPairs = this.addTargetsForAnnotation(annotationID, targets);
        AnnotationBody annotationBody = annotation.getBody();
        String bodyText;
        String newBodyText;
        String mimeType;
        if (annotationBody.getXmlBody() != null) {
            bodyText = Helpers.elementToString(annotation.getBody().getXmlBody().getAny());
            mimeType = annotationBody.getXmlBody().getMimeType();
        } else {
            if (annotation.getBody().getTextBody() != null) {
                bodyText = annotation.getBody().getTextBody().getBody();
                mimeType = annotationBody.getTextBody().getMimeType();
            } else {
                logger.error("The client has sent ill-formed annotation body.");
                return -1;
            }
        }
        newBodyText = Helpers.replace(bodyText, targetIdPairs);
        return annotationDao.updateAnnotationBody(annotationID, newBodyText, mimeType, annotationBody.getXmlBody() != null);
    }

    private int addPermissions(List<Permission> permissions, Number annotationID) throws NotInDataBaseException {
        if (permissions != null) {
            int addedPermissions = 0;
            for (Permission permission : permissions) {
                addedPermissions = addedPermissions + annotationDao.addPermission(annotationID, principalDao.getInternalIDFromHref(permission.getPrincipalHref()), permission.getLevel());
            }
            return addedPermissions;
        } else {
            return 0;
        }
    }

    private TargetInfo getTargetInfoFromTarget(Target target) {
        TargetInfo targetInfo = new TargetInfo();
        targetInfo.setHref(target.getHref());
        targetInfo.setLink(target.getLink());
        targetInfo.setVersion(target.getVersion());
        return targetInfo;
    }
}
