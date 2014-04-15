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

import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.backend.PrincipalCannotBeDeleted;
import eu.dasish.annotation.backend.PrincipalExists;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.CachedRepresentationDao;
import eu.dasish.annotation.backend.dao.DBIntegrityService;
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
import java.lang.Number;
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
public class DBIntegrityServiceImlp implements DBIntegrityService {

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
    private static final Logger logger = LoggerFactory.getLogger(DBIntegrityServiceImlp.class);

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
    public void setServiceURI(String serviceURI) {
        principalDao.setServiceURI(serviceURI + "principals/");
        cachedRepresentationDao.setServiceURI(serviceURI + "cached/");
        targetDao.setServiceURI(serviceURI + "targets/");
        annotationDao.setServiceURI(serviceURI + "annotations/");
        notebookDao.setServiceURI(serviceURI + "notebooks/");
    }

    ///////////// GETTERS //////////////////////////
    @Override
    public Number getResourceInternalIdentifier(UUID externalID, Resource resource) throws NotInDataBaseException {
        return this.getDao(resource).getInternalID(externalID);
    }

    @Override
    public Number getResourceInternalIdentifierFromURI(String uri, Resource resource) throws NotInDataBaseException {
        return this.getDao(resource).getInternalIDFromURI(uri);
    }

    @Override
    public UUID getResourceExternalIdentifier(Number resourceID, Resource resource) {
        return this.getDao(resource).getExternalID(resourceID);
    }

    @Override
    public String getResourceURI(Number resourceID, Resource resource) {
        return this.getDao(resource).getURIFromInternalID(resourceID);
    }

    @Override
    public Annotation getAnnotation(Number annotationID) {
        Annotation result = annotationDao.getAnnotationWithoutTargetsAndPemissions(annotationID);
        result.setOwnerRef(principalDao.getURIFromInternalID(annotationDao.getOwner(annotationID)));
        List<Number> targetIDs = targetDao.retrieveTargetIDs(annotationID);
        TargetInfoList sis = new TargetInfoList();
        for (Number targetID : targetIDs) {
            TargetInfo targetInfo = getTargetInfoFromTarget(targetDao.getTarget(targetID));
            sis.getTargetInfo().add(targetInfo);
        }
        result.setTargets(sis);
        result.setPermissions(this.getPermissions(annotationID, Resource.ANNOTATION));
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
    // TODO UNIT tests
    @Override
    public PermissionList getPermissions(Number resourceID, Resource resource) {
        List<Map<Number, String>> principalsAccesss = this.getDao(resource).getPermissions(resourceID);
        PermissionList result = new PermissionList();
        result.setPublic(this.getDao(resource).getPublicAttribute(resourceID));
        List<Permission> list = result.getPermission();
        for (Map<Number, String> principalAccess : principalsAccesss) {
            Number[] principal = new Number[1];
            principalAccess.keySet().toArray(principal);
            Permission permission = new Permission();
            permission.setPrincipalRef(principalDao.getURIFromInternalID(principal[0]));
            permission.setLevel(Access.fromValue(principalAccess.get(principal[0])));
            list.add(permission);
        }
        return result;
    }

////////////////////////////////////////////////////////////////////////
    @Override
    public List<Number> getFilteredAnnotationIDs(UUID ownerId, String link, String text, Number inloggedPrincipalID, String accessMode, String namespace, String after, String before) throws NotInDataBaseException {

        Number ownerID;

        if (ownerId != null) {
            if (accessMode.equals("owner")) {
                if (!ownerId.equals(principalDao.getExternalID(inloggedPrincipalID))) {
                    logger.debug("The inlogged principal is demanded to be the owner of the annotations, however the expected owner is different and has the UUID " + ownerId.toString());
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



        List<Number> annotationIDs = annotationDao.getFilteredAnnotationIDs(ownerID, text, namespace, after, before);

        //filtering on tables "target" and "annotations_targets"
        if (link != null) {
            List<Number> targetIDs = targetDao.getTargetsReferringTo(link);
            List<Number> annotationIDsForTargets = annotationDao.getAnnotationIDsForTargets(targetIDs);
            annotationIDs.retainAll(annotationIDsForTargets);
        };

        if (!accessMode.equals("owner")) {
            Access access = Access.fromValue(accessMode);
            List<Number> annotationIDsAccess = annotationDao.getAnnotationIDsForPermission(inloggedPrincipalID, access);
            List<Number> annotationIDsPublic = annotationDao.getAnnotationIDsForPublicAccess(access);
            if (accessMode.equals("read")) {
                List<Number> writeIDs = annotationDao.getAnnotationIDsForPermission(inloggedPrincipalID, Access.WRITE);
                annotationIDsAccess.addAll(writeIDs);
                List<Number> writeIDsPublic = annotationDao.getAnnotationIDsForPublicAccess(Access.WRITE);
                annotationIDsPublic.addAll(writeIDsPublic);
            }
            int check = this.addAllNoRepetitions(annotationIDsAccess, annotationIDsPublic);
            List<Number> ownedAnnotIDs = annotationDao.getFilteredAnnotationIDs(inloggedPrincipalID, null, null, null, null);
            boolean checkTwo = annotationIDsAccess.addAll(ownedAnnotIDs);
            annotationIDs.retainAll(annotationIDsAccess);
        }

        return annotationIDs;
    }

    /// helper ///
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

    //////
    @Override
    public ReferenceList getAnnotationTargets(Number annotationID) {
        ReferenceList result = new ReferenceList();
        List<Number> targetIDs = targetDao.retrieveTargetIDs(annotationID);
        for (Number targetID : targetIDs) {
            result.getRef().add(targetDao.getURIFromInternalID(targetID));
        }
        return result;
    }

    @Override
    public List<String> getTargetsWithNoCachedRepresentation(Number annotationID) {

        List<String> result = new ArrayList<String>();
        List<Number> targetIDs = targetDao.retrieveTargetIDs(annotationID);
        for (Number targetID : targetIDs) {
            List<Number> versions = cachedRepresentationDao.getCachedRepresentationsForTarget(targetID);
            if (versions.isEmpty()) {
                result.add(targetDao.getURIFromInternalID(targetID));
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
                result.add(principalDao.getURIFromInternalID(principalID[0]));

            }
        }
        return result;
    }

    @Override
    public AnnotationInfoList getFilteredAnnotationInfos(UUID ownerId, String link, String text, Number inloggedPrincipalID, String access, String namespace, String after, String before) throws NotInDataBaseException {
        List<Number> annotationIDs = this.getFilteredAnnotationIDs(ownerId, link, text, inloggedPrincipalID, access, namespace, after, before);
        AnnotationInfoList result = new AnnotationInfoList();
        for (Number annotationID : annotationIDs) {
            AnnotationInfo annotationInfo = annotationDao.getAnnotationInfoWithoutTargetsAndOwner(annotationID);
            annotationInfo.setTargets(this.getAnnotationTargets(annotationID));
            annotationInfo.setOwnerRef(principalDao.getURIFromInternalID(annotationDao.getOwner(annotationID)));
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
            ReferenceList targets = getAnnotationTargets(annotationID);
            AnnotationInfo annotationInfo = annotationDao.getAnnotationInfoWithoutTargetsAndOwner(annotationID);
            annotationInfo.setTargets(targets);
            annotationInfo.setOwnerRef(principalDao.getURIFromInternalID(ownerID));
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
            cachedRepresentationFragment.setRef(cachedRepresentationDao.getURIFromInternalID(key));
            cachedRepresentationFragment.setFragmentString(cachedIDsFragments.get(key));
            cachedRepresentationFragmentList.getCached().add(cachedRepresentationFragment);
        }
        result.setCachedRepresentatinons(cachedRepresentationFragmentList);
        return result;
    }

    // TODO unit test
    @Override
    public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID) {
        return cachedRepresentationDao.getCachedRepresentationInfo(internalID);
    }

    //TODO unit test
    @Override
    public InputStream getCachedRepresentationBlob(Number cachedID) {
        return cachedRepresentationDao.getCachedRepresentationBlob(cachedID);
    }

    @Override
    public ReferenceList getTargetsForTheSameLinkAs(Number targetID) {
        List<Number> targetIDs = targetDao.getTargetsForLink(targetDao.getLink(targetID));
        ReferenceList referenceList = new ReferenceList();
        for (Number siblingID : targetIDs) {
            referenceList.getRef().add(targetDao.externalIDtoURI(targetDao.getExternalID(siblingID).toString()));
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
                return (access.equals(Access.NONE) ? Access.READ : access);
            } else {
                return Access.WRITE;
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
    public String getTypeOfPrincipalAccount(Number principalID) {
        return principalDao.getTypeOfPrincipalAccount(principalID);
    }

    @Override
    public Principal getDataBaseAdmin() {
        return principalDao.getPrincipal(principalDao.getDBAdminID());
    }

    @Override
    public boolean canDo(Access access, Number principalID, Number annotationID) {
        if (principalID.equals(annotationDao.getOwner(annotationID)) || principalDao.getTypeOfPrincipalAccount(principalID).equals(admin)) {
            return true;
        }
        return this.getAccess(annotationID, principalID).equals(access);
    }

    ////// noetbooks ///////
    /// TODO update for having attribute public!!! /////
    @Override
    public NotebookInfoList getNotebooks(Number principalID, Access access) {
        NotebookInfoList result = new NotebookInfoList();
        if (access.equals(Access.READ) || access.equals(Access.WRITE)) {
            List<Number> notebookIDs = notebookDao.getNotebookIDs(principalID, access);
            for (Number notebookID : notebookIDs) {
                NotebookInfo notebookInfo = notebookDao.getNotebookInfoWithoutOwner(notebookID);
                Number ownerID = notebookDao.getOwner(notebookID);
                notebookInfo.setOwnerRef(principalDao.getURIFromInternalID(ownerID));
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
            String reference = notebookDao.getURIFromInternalID(notebookID);
            result.getRef().add(reference);
        }
        return result;
    }

    @Override
    public ReferenceList getPrincipals(Number notebookID, String access) {
        ReferenceList result = new ReferenceList();
        List<Number> principalIDs = principalDao.getPrincipalIDsWithAccessForNotebook(notebookID, Access.fromValue(access));
        for (Number principalID : principalIDs) {
            String reference = principalDao.getURIFromInternalID(principalID);
            result.getRef().add(reference);
        }
        return result;
    }

    @Override
    public Notebook getNotebook(Number notebookID) {
        Notebook result = notebookDao.getNotebookWithoutAnnotationsAndAccesssAndOwner(notebookID);

        result.setOwnerRef(principalDao.getURIFromInternalID(notebookDao.getOwner(notebookID)));

        ReferenceList annotations = new ReferenceList();
        List<Number> annotationIDs = annotationDao.getAnnotations(notebookID);
        for (Number annotationID : annotationIDs) {
            annotations.getRef().add(annotationDao.getURIFromInternalID(annotationID));
        }
        result.setAnnotations(annotations);

        PermissionList ups = new PermissionList();
        List<Access> accesss = new ArrayList<Access>();
        accesss.add(Access.READ);
        accesss.add(Access.WRITE);
        for (Access access : accesss) {
            List<Number> principals = principalDao.getPrincipalIDsWithAccessForNotebook(notebookID, access);
            if (principals != null) {
                for (Number principal : principals) {
                    Permission up = new Permission();
                    up.setPrincipalRef(principalDao.getURIFromInternalID(principal));
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

        if (startAnnotation < -1) {
            logger.info("Variable's startAnnotation value " + startAnnotation + " is invalid. I will return null.");
            return null;
        }

        if (maximumAnnotations < -1) {
            logger.info("Variable's maximumAnnotations value " + maximumAnnotations + " is invalid. I will return null.");
            return null;
        }

        int offset = (startAnnotation > 0) ? startAnnotation - 1 : 0;
        String direction = desc ? "DESC" : "ASC";
        List<Number> selectedAnnotIDs = annotationDao.sublistOrderedAnnotationIDs(annotationIDs, offset, maximumAnnotations, orderedBy, direction);
        ReferenceList references = new ReferenceList();
        for (Number annotationID : selectedAnnotIDs) {
            references.getRef().add(annotationDao.getURIFromInternalID(annotationID));
        }
        return references;
    }

    ///// UPDATERS /////////////////
    @Override
    public boolean updateAccount(UUID principalExternalID, String account) throws NotInDataBaseException {
        return principalDao.updateAccount(principalExternalID, account);
    }

    @Override
    public int updateAnnotationPrincipalAccess(Number annotationID, Number principalID, Access access) {
        int result;
        Access currentAccess = annotationDao.getAccess(annotationID, principalID);
        if (currentAccess != Access.NONE) {
            result = annotationDao.updateAnnotationPrincipalAccess(annotationID, principalID, access);
        } else {
            if (!access.equals(Access.NONE)) {
                result = annotationDao.deleteAnnotationPrincipalAccess(annotationID, principalID);
                result = annotationDao.addAnnotationPrincipalAccess(annotationID, principalID, access);
            } else {
                result = 0;
            }
        }
        return result;
    }

    @Override
    public int updatePublicAttribute(Number annotationID, Access publicAttribute) {
        return annotationDao.updatePublicAttribute(annotationID, publicAttribute);
    }

    @Override
    public int updatePermissions(Number annotationID, PermissionList permissionList) throws NotInDataBaseException {
        annotationDao.updatePublicAttribute(annotationID, permissionList.getPublic());
        List<Permission> permissions = permissionList.getPermission();
        int result = 0;
        for (Permission permission : permissions) {
            Number principalID = principalDao.getInternalIDFromURI(permission.getPrincipalRef());
            Access access = permission.getLevel();
            Access currentAccess = annotationDao.getAccess(annotationID, principalID);
            if (!access.equals(currentAccess)) {
                // then we need to update or psossibly add for none
                if (!currentAccess.equals(Access.NONE)) {
                    result = result + annotationDao.updateAnnotationPrincipalAccess(annotationID, principalID, access);
                } else {
                    annotationDao.deleteAnnotationPrincipalAccess(annotationID, principalID);
                    result = result + annotationDao.addAnnotationPrincipalAccess(annotationID, principalID, access);
                }
            }
        }
        return result;
    }
// TODO: optimize (not chnanged targets should not be deleted)
// TODO: unit test

    @Override
    public int updateAnnotation(Annotation annotation) throws NotInDataBaseException {
        Number annotationID = annotationDao.getInternalIDFromURI(annotation.getURI());
        int updatedAnnotations = annotationDao.updateAnnotation(annotation, annotationID, principalDao.getInternalIDFromURI(annotation.getOwnerRef()));
        int deletedTargets = annotationDao.deleteAllAnnotationTarget(annotationID);
        int deletedPrinsipalsAccesss = annotationDao.deleteAnnotationPermissions(annotationID);
        int addedTargets = this.addTargets(annotation, annotationID);
        int addedPrincipalsAccesss = this.addPermissions(annotation.getPermissions().getPermission(), annotationID);
        int updatedPublicAttribute = annotationDao.updatePublicAttribute(annotationID, annotation.getPermissions().getPublic());
        return updatedAnnotations;
    }

    // TODO: unit test
    @Override
    public int updateAnnotationBody(Number internalID, AnnotationBody annotationBody) {
        String[] body = annotationDao.retrieveBodyComponents(annotationBody);
        return annotationDao.updateAnnotationBody(internalID, body[0], body[1], annotationBody.getXmlBody() != null);
    }

    @Override
    public int updatePrincipal(Principal principal) throws NotInDataBaseException {
        return principalDao.updatePrincipal(principal);
    }
    
    @Override
    public int updateTargetCachedFragment(Number targetID, Number cachedID, String fragmentDescriptor) throws NotInDataBaseException{
        return targetDao.updateTargetCachedRepresentationFragment(targetID, cachedID, fragmentDescriptor);
    }
    
    @Override
    public int updateCachedMetada(CachedRepresentationInfo cachedInfo) throws NotInDataBaseException{
        Number internalID = cachedRepresentationDao.getInternalIDFromURI(cachedInfo.getURI());
        return cachedRepresentationDao.updateCachedRepresentationMetadata(internalID, cachedInfo);
    }
    
    @Override
    public int updateCachedBlob(Number internalID, InputStream cachedBlob) throws IOException{
        return cachedRepresentationDao.updateCachedRepresentationBlob(internalID, cachedBlob);
    }
   
    /// notebooks ///

    @Override
    public boolean updateNotebookMetadata(Number notebookID, NotebookInfo upToDateNotebookInfo) throws NotInDataBaseException {
        Number ownerID = principalDao.getInternalIDFromURI(upToDateNotebookInfo.getOwnerRef());
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
                Number targetIDRunner = targetDao.getInternalIDFromURI(targetInfo.getRef());
                int affectedRows = annotationDao.addAnnotationTarget(annotationID, targetIDRunner);
            } catch (NotInDataBaseException e) {
                Target newTarget = this.createFreshTarget(targetInfo);
                Number targetID = targetDao.addTarget(newTarget);
                String targetTemporaryID = targetDao.stringURItoExternalID(targetInfo.getRef());
                result.put(targetTemporaryID, targetDao.getExternalID(targetID).toString());
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
        int updatedPublic = annotationDao.updatePublicAttribute(annotationID, annotation.getPermissions().getPublic());
        return annotationID;
    }

    @Override
    public Number addPrincipal(Principal principal, String remoteID) throws NotInDataBaseException, PrincipalExists {
        if (principalDao.principalExists(principal)) {
            throw new PrincipalExists(principal.getEMail());
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
            Number principalID = principalDao.getInternalIDFromURI(permission.getPrincipalRef());
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
        result[1] = annotationDao.deleteAnnotationPermissions(annotationID);
        List<Number> targetIDs = targetDao.retrieveTargetIDs(annotationID);
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
        return annotationDao.deleteAnnotationPrincipalAccess(annotationID, principalID);
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
    public Principal createPrincipalRecord(String remoteID) {
        return principalDao.createShibbolizedPrincipal(remoteID);
    }

    //// priveee ///
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
                addedPermissions = addedPermissions + annotationDao.addAnnotationPrincipalAccess(annotationID, principalDao.getInternalIDFromURI(permission.getPrincipalRef()), permission.getLevel());
            }
            return addedPermissions;
        } else {
            return 0;
        }
    }

    private TargetInfo getTargetInfoFromTarget(Target target) {
        TargetInfo targetInfo = new TargetInfo();
        targetInfo.setRef(target.getURI());
        targetInfo.setLink(target.getLink());
        targetInfo.setVersion(target.getVersion());
        return targetInfo;
    }
}
