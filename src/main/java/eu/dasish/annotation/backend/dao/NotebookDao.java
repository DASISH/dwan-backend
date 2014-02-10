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


import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import java.util.List;

/**
 * Created on : Jun 12, 2013, 1:40:09 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */

// TODO: not yet fully updated.

public interface NotebookDao extends ResourceDao {

    
    /**
     * 
     * GETTERS 
     * 
     */
    // Returns a list of notebook Ids for the notebooks for which the given user "userID' has "permission" access.
    List<Number> getNotebookIDs(Number userID, Permission acessMode);
    
    // Returns a list of notebook Ids for the notebooks for which the given user "userID" is the owner.
    List<Number> getOwnerNotebookIDs(Number userID);
    
    public List<Number> getPrincipalIDsWithPermission(Number notebookID, Permission permission);
    
    
    
    /**
     * 
     * @param notebookID
     * @return the notebook info for the notebook with notebookID
     */
    NotebookInfo getNotebookInfo(Number notebookID);
    
   
    /**
     * 
     * @param notebookID
     * @return notebook metadata for the notebook with notebookID
     */
    Notebook getNotebookWithoutAnnotationsAndPermissions(Number notebookID);
    
    //? Which type shul be orderedby? 
    /**
     * 
     * @param maximumAnnotations
     * @param startannotation
     * @param orderedBy
     * @param orederingMode if true then descending, if falset hen ascending
     * @return 
     */
    List<Number> getAnnotations(int maximumAnnotations, int startannotation, String orderedBy, boolean orderingMode);
    
    /**
     * 
     * UPDATERS 
     * 
     * 
     */
    
    
    
    /**
     * 
     * @param notebookID
     * @return true if updated, false otherwise. Logs the reason if the notebook is not updated.
     */
    boolean updateNotebookMetadata(Number notebookID);
    
     /**
     * 
     * ADDERS
     * 
     * 
     */
    
    Number createNotebook(Notebook notebook);
    
    boolean addAnnotationToNotebook(Number noteboookId, Number AnnotationID);
    
    
    
    /**
     * 
     * DELETERS (ADDER)
     * 
     * 
     */
    
    boolean deleteannotationFromNotebook(Number notebookID, Number annotationID);
    
    boolean deleteNotebook(Number notebookID);
}
