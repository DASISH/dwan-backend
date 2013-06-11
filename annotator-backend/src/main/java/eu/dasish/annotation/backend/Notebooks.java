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
package eu.dasish.annotation.backend;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * Created on : Jun 11, 2013, 5:10:55 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@Path("/notebooks")
public class Notebooks {

    @GET
    @Produces("text/html")
    @Path("")
    // Returns notebook-infos for the notebooks accessible to the current user.
    public String getNotebookInfo() {
        return "notebook-info";
    }

    @GET
    @Produces("text/html")
    @Path("owned")
    // Returns the list of all notebooks owned by the current logged user.
    public String getUsersNotebooks() {
        return "UsersNotebooks";
    }

    @GET
    @Produces("text/html")
    @Path("{notebookid: [a-zA-Z0-9_]*}/readers")
    // Returns the list of _uid_ who allowed to read the annotations from notebook.
    public String getReaders(@PathParam("notebookid") String notebookId) {
        return "readers for " + notebookId;
    }

    @GET
    @Produces("text/html")
    @Path("{notebookid: [a-zA-Z0-9_]*}/writers")
    // Returns the list of _uid_ that can add annotations to the notebook.
    public String getWriters(@PathParam("notebookid") String notebookId) {
        return "writers for " + notebookId;
    }

    @GET
    @Produces("text/html")
    @Path("{notebookid: [a-zA-Z0-9_]*}/metadata")
    // Get all metadata about a specified notebook _nid_, including the information if it is private or not.
    public String getMetadata(@PathParam("notebookid") String notebookId) {
        return "metadata for " + notebookId;
    }
}
