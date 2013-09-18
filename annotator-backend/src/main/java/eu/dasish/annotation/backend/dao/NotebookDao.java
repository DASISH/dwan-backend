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


import eu.dasish.annotation.schema.AnnotationList;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.ResourceREF;
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
     */
    // Returns a list of notebook-info for the notebooks accessible to the current user.
    List<NotebookInfo> getNotebookInfos(UUID userID);

    // Returns the list of all notebooks owned by the current logged user.
    List<Notebook> getUsersNotebooks(UUID userID);

    
      // Returns the list of annotation Id-s  for the notebook id.
    public List<Number> getAnnotationIDs(Number notebookID);
    
      /*Returns the list of annotation info-s  for the notebook id.
    /List<AnnotationInfo> getAnnotationInfosOfNotebook(Number notebookID); */   
    
      // Returns the list of annotations Id-s  for the notebook id.
    public List<ResourceREF> getAnnotationREFsOfNotebook(Number notebookID);
    
    // Returns the Annotations object for the notebook id.
    public AnnotationList getAnnotations(Number notebookID);
    
    /**
     * 
     * @param notebookID
     * @return the Notebook information (title, owner (?), time_stamp(?), amount of annotations(?), their headlines (?) // TODO: discuss changing in the schema
     * user in getting the metadata of a notebook
     */
    NotebookInfo getNotebookInfo(Number notebookID);
    
  
    
     /**
     * @param notebookId
     * @return returns the externalIds of the annotations contained in the notebookId
     */
    List<UUID> getAnnotationExternalIDs(UUID notebookId);
    
    
    /**
     * 
     *  ADDERS 
     */
    
    // Creates a new notebook and returns the _nid_ of the created Notebook
    UUID addNotebook(UUID userID, String title);

      // Adds an annotation _aid_ to the list of annotations of _nid_.
    public int addAnnotation(UUID notebookId, UUID annotationId);
    
   
    
   /**
    * 
    * DELETE 
    * 
    */
    
    // Delete _nid_. Annotations stay, they just lose connection to _nid_.<br>
    // returns the number of records deleted
    public int deleteNotebook(UUID notebookId);
    /**
     * 
     * @param annotationID
     * @return removes the rows with annotationID from notebooks_annotations table
     */
    int removeAnnotation(Number annotationID);
}
