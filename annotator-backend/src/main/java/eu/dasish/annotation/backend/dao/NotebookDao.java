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
  **/
    
    Number getOwner(Number notebookID);
    
    
    
    List<Number> getNotebookIDs(Number principalID, Access acessMode);
    
    // Returns a list of notebook Ids for the notebooks for which the given principal "principalID" is the owner.
    List<Number> getNotebookIDsOwnedBy(Number principaID);
    
   
    
    /**
     * 
     * @param notebookID
     * @return the notebook info for the notebook with notebookID
     */
    NotebookInfo getNotebookInfoWithoutOwner(Number notebookID);
    
   
    /**
     * 
     * @param notebookID
     * @return notebook metadata for the notebook with notebookID
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
     * @param notebookID
     * @return true if updated, false otherwise. Logs the reason if the notebook is not updated.
     */
    boolean updateNotebookMetadata(Number notebookID, String title, Number ownerID);
    
    boolean setOwner(Number notebookID, Number ownerID);
    
    boolean updatePrincipalAccessForNotebook(Number notebookID, Number principalID, Access access);
    
     /**
     * 
     * ADDERS
     * 
     * 
     */
    
    public Number createNotebookWithoutAccesssAndAnnotations(Notebook notebook, Number ownerID) throws NotInDataBaseException;
    
    boolean addAnnotationToNotebook(Number notebookID, Number annotationID);
    
    boolean addAccessToNotebook(Number notebookID, Number principalID, Access access);
    
    
    
    /**
     * 
     * DELETERS 
     * 
     * 
     */
    
    boolean deleteAnnotationFromNotebook(Number notebookID, Number annotationID);
    
    boolean deleteAllAnnotationsFromNotebook(Number notebookID);
    
    boolean deleteNotebookPrincipalAccess(Number notebookID, Number principalID);
    
    boolean deleteAllAccesssForNotebook(Number notebookID);
    
    boolean deleteNotebook(Number notebookID);
}
