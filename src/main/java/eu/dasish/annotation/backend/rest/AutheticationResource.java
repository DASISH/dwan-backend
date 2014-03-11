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

import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.User;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
@Transactional(rollbackFor = {Exception.class, SQLException.class, IOException.class, ParserConfigurationException.class})
public class AutheticationResource extends ResourceResource {

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("user")
    @Transactional(readOnly = true)
    public JAXBElement<User> getCurrentUser() throws IOException {
        Number userID = this.getUserID();
        if (userID != null) {
            return new ObjectFactory().createUser(dbIntegrityService.getUser(userID));
        }
        return new ObjectFactory().createUser(new User());
    }

    /* the only request that redirects to the shibboleth login-page
     * 
     */
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("login")
    @Transactional(readOnly = true)
    public JAXBElement<User> loginAndGet() throws IOException {
        String remoteUser = httpServletRequest.getRemoteUser();
        verboseOutput = new VerboseOutput(httpServletResponse, loggerServer);
        if (remoteUser != null) {
            if (!remoteUser.equals("anonymous")) {
                dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
                final Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
                if (remoteUserID != null) {
                    return new ObjectFactory().createUser(dbIntegrityService.getUser(remoteUserID));
                } else {
                    verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser, dbIntegrityService.getDataBaseAdmin().getDisplayName(), dbIntegrityService.getDataBaseAdmin().getEMail());
                }
            } else {
                verboseOutput.ANONYMOUS_PRINCIPAL();
            }
        }
        return new ObjectFactory().createUser(new User());
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("logout")
    @Transactional(readOnly = true)
    public void logout() throws IOException, ServletException {
        httpServletResponse.sendRedirect("eu.dasish.annotation.backend.logout");
    }
}
