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
package eu.dasish.annotation.backend.rest;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * A help REST class so far containing only one method outputting the backend-version number.
 * @author olhsha
 */
@Component
@Path("/projectinfo")
@Transactional(rollbackFor = {Exception.class})
public class ProjectInfoResource extends ResourceResource{
    
    /**
     * 
     * @return a message string containing the number of the version of the backend.
     * @throws IOException if getting a principal or sending an error fails.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("version")
    @Transactional(readOnly = true)
    public String getVersion() throws IOException {
        Number principalID = this.getPrincipalID();
        String retVal = "?.?";
        ResourceBundle rb;
        try {
            rb = ResourceBundle.getBundle("projectinfo");
            retVal = rb.getString("application.version");
        } catch (MissingResourceException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
        return "DWAN backend "+retVal; 
    }
    
}
