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

import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Principal;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olhsha
 */
@Component
@Path("/authentication")
@Transactional(rollbackFor = {Exception.class, IOException.class, ParserConfigurationException.class})
public class AutheticationResource extends ResourceResource {
    
   
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("principal")
    @Transactional(readOnly = true)
    public JAXBElement<Principal> getCurrentPrincipal() throws IOException {
        Number principalID = this.getPrincipalID();
        if (principalID != null) {
            return new ObjectFactory().createPrincipal(dbDispatcher.getPrincipal(principalID));
        } else {
            return new ObjectFactory().createPrincipal(new Principal());
        }
    }

    /* the only request that redirects to the shibboleth login-page
     * 
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("login")
    @Transactional(readOnly = true)
    public String login() {
        try {
            Number principalID = this.getPrincipalID();
            String remoteID = dbDispatcher.getPrincipalRemoteID(principalID);
            return Helpers.welcomeString(httpServletRequest.getContextPath(), remoteID);
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("logout")
    @Transactional(readOnly = true)
    public void logout() throws IOException, ServletException {
        httpServletRequest.getSession().invalidate();
        boolean isShibboleth = Boolean.parseBoolean(context.getInitParameter("eu.dasish.annotation.backend.isShibbolethSession"));
        String redirect = isShibboleth ? context.getInitParameter("eu.dasish.annotation.backend.logout") : 
                httpServletRequest.getContextPath() + context.getInitParameter("eu.dasish.annotation.backend.logout");        
        httpServletResponse.sendRedirect(redirect);
    }
}