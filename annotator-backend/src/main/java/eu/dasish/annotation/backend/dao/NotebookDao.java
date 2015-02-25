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
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import java.util.List;
import java.util.UUID;

/**
 * 
 *
 * @author olhsha@mpi.nl
 */

// TODO: not yet fully updated.

public interface NotebookDao extends ResourceDao {

    
    /**
     * 
     * GETTERS 
     * 
  **/
    
    /**
     * 
     * @param notebookID the internal ID of a notebook.
     * @return the internal database Id of the owner of the notebook.
     */
    Number getOwner(Number notebookID);
    
    
    /**
     * 
     * @param principalID the internal database id of some principal.
     * @param acessMode an {@link Access} object, representing one of the access modes: 
     * "all", "write", "read" or "none".
     * @return the list of internal database id's of the notebooks for which "principalID" has "accessMode" access.
     */
    List<Number> getNotebookIDs(Number principalID, Access accessMode);
    
   /** 
    * @param principaID the internal database id of some principal.
    * @return  the list of internal database id's of the notebooks for which "principalID" is the owner.
    */
    List<Number> getNotebookIDsOwnedBy(Number principaID);
    
   
    
    /**
     * 
     * @param notebookID the internal database id of a notebook.
     * @return the {@link NotebookInfo} object representing the notebook with notebookID, built on the corresponding row
     * in the "notebook" table.
     */
    NotebookInfo getNotebookInfoWithoutOwner(Number notebookID);
    
   
    /**
     * 
     * @param notebookID the internal database id of a notebook.
     * @return the {@link Notebook} object representing the notebook with notebookID.
     */
    Notebook getNotebookWithoutAnnotationsAndAccesssAndOwner(Number notebookID);
    
   
   
    
    /**
     * 
     * UPDATERS 
     * 
     * 
     */
    
    
    

    /**
     * 
     * @param notebookID the internal database id of a notebook.
     * @param title a new title for the notebook.
     * @param ownerID the internal database id of the owner principal (or possibly, a new owner principal) of the notebook.
     * @return true if the notebook metadata are updated, false otherwise.
     */
    boolean updateNotebookMetadata(Number notebookID, String title, Number ownerID);
    
    
    /**
     * 
     * @param notebookID the internal database id of a notebook.
     * @param ownerID the internal database id of a new owner for the notebook.
     * @return true if the owner_id is in "notebook" table is updated to "ownerID", false otherwise.
     */
    boolean setOwner(Number notebookID, Number ownerID);
    
    /**
     * 
     * @param notebookID the internal database id of a notebook.
     * @param principalID the internal database id of a principal.
     * @param access an {@link Access} object.
     * @return true iff the access mode to "notebookID" for "principalID" has been assigned to "access"'s value.
     */
    boolean updatePrincipalAccessForNotebook(Number notebookID, Number principalID, Access access);
    
     /**
     * 
     * ADDERS
     * 
     * 
     */
    
    /**
     * 
     * @param notebook the {@link Notebook} object representing a notebook to be created in the database.
     * @param ownerID the internal database id of the owner principal.
     * @return the internal id of the created notebook.
     * @throws NotInDataBaseException if creating the notebook fails. 
     */
    public Number createNotebookWithoutAccesssAndAnnotations(Notebook notebook, Number ownerID) throws NotInDataBaseException;
    
    /**
     * 
     * @param notebookID the internal database ID of a notebook.
     * @param annotationID the internal database ID of an annotation.
     * @return  true if the row for the (notebookID,annotationID) has been added to the junction table 
     * "notebooks_annotations".
     */
    boolean addAnnotationToNotebook(Number notebookID, Number annotationID);
    
    /**
     * 
     * @param notebookID the internal database ID of a notebook.
     * @param principalID the internal database ID of a principal.
     * @param access access an {@link Access} object.
     * @return true iff the "access" mode to "notebookID" has been assigned for "principalID" 
     * in the corresponding junction table.
     */
    boolean addAccessToNotebook(Number notebookID, Number principalID, Access access);
    
    
    
    /**
     * 
     * DELETERS 
     * 
     * 
     */
    
    /**
     * 
     * @param notebookID the internal database ID of a notebook.
     * @param annotationID the internal database ID of an annotation.
     * @return  true iff the row for the (notebookID,annotationID) has been deleted from the junction table 
     * "notebooks_annotations".
     */
    boolean deleteAnnotationFromNotebook(Number notebookID, Number annotationID);
    
    /**
     * 
     * @param notebookID the internal database ID of a notebook.
     * @return true iff all the rows with "notebookID" have been removed from "notebooks_annotations" table.
     */
    boolean deleteAllAnnotationsFromNotebook(Number notebookID);
    
    /**
     * 
     * @param notebookID the internal database ID of a notebook.
     * @param principalID the internal database ID of a principal.
     * @return true iff the row for the pair (notebookID, principlaID) has been removed from the "notebooks_principals_accesses" table.
     */
    boolean deleteNotebookPrincipalAccess(Number notebookID, Number principalID);
    
    /**
     * 
     * @param notebookID the internal database ID of a notebook.
     * @return true iff all the rows with "notebookID" have been removed from from the "notebooks_principals_accesses" table.
     */
    boolean deleteAllAccesssForNotebook(Number notebookID);
    
    /**
     * 
     * @param notebookID the internal database ID of a notebook.
     * @return true iff the notebook with "notebookID" has been removed from the "notebook" table.
     */
    boolean deleteNotebook(Number notebookID);
}
