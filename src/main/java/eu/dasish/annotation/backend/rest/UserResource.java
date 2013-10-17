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

import eu.dasish.annotation.backend.BackendConstants;
import eu.dasish.annotation.backend.dao.DBIntegrityService;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.User;
import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author olhsha
 */
@Component
@Path("/users")
public class UserResource {
    @Autowired
    private DBIntegrityService dbIntegrityService;
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private UriInfo uriInfo;

    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    public UserResource() {
    }
    
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{userid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<User> getUser(@PathParam("userid") String ExternalIdentifier) throws SQLException {
         dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        final Number userID = dbIntegrityService.getUserInternalIdentifier(UUID.fromString(ExternalIdentifier));
        final User user = dbIntegrityService.getUser(userID);
        return new ObjectFactory().createUser(user);
    }
    
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("/info")
    public JAXBElement<User> getUserByInfo(@QueryParam("email") String email) throws SQLException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        final User user = dbIntegrityService.getUserByInfo(email);
        return new ObjectFactory().createUser(user);
    }
}
