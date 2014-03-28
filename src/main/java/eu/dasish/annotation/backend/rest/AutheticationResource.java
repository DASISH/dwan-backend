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

import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.NotLoggedInException;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Principal;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.http.HTTPException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olhsha
 */
@Component
@Path("/authentication")
@Transactional(rollbackFor = {Exception.class, SQLException.class, IOException.class, ParserConfigurationException.class})
public class AutheticationResource extends ResourceResource {

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("principal")
    @Transactional(readOnly = true)
    public JAXBElement<Principal> getCurrentPrincipal() throws IOException{
        Number principalID = this.getPrincipalID();
        return new ObjectFactory().createPrincipal(dbIntegrityService.getPrincipal(principalID));
    }

    /* the only request that redirects to the shibboleth login-page
     * 
     */
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("login")
    @Transactional(readOnly = true)
    public JAXBElement<Principal> loginAndGet() throws IOException{
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        String remotePrincipal = httpServletRequest.getRemoteUser();
        verboseOutput = new VerboseOutput(httpServletResponse, loggerServer);
        if (!remotePrincipal.equals("anonymous")) {
            try {
            final Number remotePrincipalID = dbIntegrityService.getPrincipalInternalIDFromRemoteID(remotePrincipal);
            return new ObjectFactory().createPrincipal(dbIntegrityService.getPrincipal(remotePrincipalID));
            } catch (NotInDataBaseException e) {
                verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remotePrincipal);
                throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            verboseOutput.ANONYMOUS_PRINCIPAL();
            return new ObjectFactory().createPrincipal(new Principal());
        }
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("logout")
    @Transactional(readOnly = true)
    public void logout() throws IOException {
        httpServletResponse.sendRedirect(context.getInitParameter("eu.dasish.annotation.backend.logout"));
    }
}
