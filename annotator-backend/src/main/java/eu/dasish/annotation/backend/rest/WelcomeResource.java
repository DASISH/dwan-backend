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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.ws.http.HTTPException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olhsha
 */
@Component
@Path("")
@Transactional(rollbackFor = {Exception.class})
public class WelcomeResource extends ResourceResource{
    
    @GET
    @Produces({"text/html"})
    @Path("")
    @Transactional(readOnly = true)
    public String welcome() throws IOException, HTTPException {
        Number remotePrincipalID = this.getPrincipalID();
         if (remotePrincipalID == null) {
            return "You are not logged in properly.";
        }
        String baseUri = uriInfo.getBaseUri().toString() + "..";
        String welcome = "<!DOCTYPE html><body>"
                + "<h3>Welcome to DASISH Webannotator (DWAN)</h3><br>"
                + "<a href=\"" + baseUri + "\"> to DWAN's test jsp page</a>"
                + "</body>";
        return welcome;
    }
    
}
