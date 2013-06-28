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

import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.NotebookIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import java.util.List;

/**
 * Created on : Jun 12, 2013, 1:40:09 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public interface NotebookDao {

    // Returns a list of notebook-info for the notebooks accessible to the current user.
    List<NotebookInfo> getNotebookInfos(UserIdentifier userID);

    // Returns the list of all notebooks owned by the current logged user.
    List<Notebook> getUsersNotebooks(UserIdentifier userID);

    // Creates a new notebook and returns the _nid_ of the created Notebook
    NotebookIdentifier addNotebook(UserIdentifier userID, NotebookIdentifier notebookUri, String title);

    // Delete _nid_. Annotations stay, they just lose connection to _nid_.<br>
    // returns the number of records deleted
    public int deleteNotebook(NotebookIdentifier notebookId);

    // Adds an annotation _aid_ to the list of annotations of _nid_.
    public int addAnnotation(NotebookIdentifier notebookId, AnnotationIdentifier annotationId);
}
