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
import eu.dasish.annotation.backend.PrincipalCannotBeDeleted;
import eu.dasish.annotation.backend.PrincipalExists;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.dao.ILambda;
import eu.dasish.annotation.backend.dao.ILambdaPrincipal;
import eu.dasish.annotation.schema.CurrentPrincipalInfo;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Principal;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
@Path("/principals")
@Transactional(rollbackFor = {Exception.class, SQLException.class, IOException.class, ParserConfigurationException.class})
public class PrincipalResource extends ResourceResource {

    int shaStrength = 512;

    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    public PrincipalResource() {
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{principalid}")
    @Transactional(readOnly = true)
    public JAXBElement<Principal> getPrincipal(@PathParam("principalid") String externalIdentifier) throws IOException {
        Map params = new HashMap<String, String>();
        params.put("externalId", externalIdentifier);
        Principal result = (Principal) (new RequestWrappers(this)).wrapRequestResource(params, new GetPrincipal());
        return (result != null) ? (new ObjectFactory().createPrincipal(result)) : (new ObjectFactory().createPrincipal(new Principal()));
    }
    
      private class GetPrincipal implements ILambda<Map, Principal> {

        @Override
        public Principal apply(Map params) throws NotInDataBaseException {
            final Number principalID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString((String) params.get("externalId")), Resource.PRINCIPAL);
            return dbIntegrityService.getPrincipal(principalID);
        }
    }
      
     /////////////////////////////////

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("admin")
    @Transactional(readOnly = true)
    public String getAdmin() throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return " ";
        }
        return "The admin of the server database " + dbIntegrityService.getDataBaseAdmin().getDisplayName() + " is availiable via e-mail " + dbIntegrityService.getDataBaseAdmin().getEMail();
    }
    
   /////////////////////////////////////////
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("info")
    @Transactional(readOnly = true)
    public JAXBElement<Principal> getPrincipalByInfo(@QueryParam("email") String email) throws IOException {
        Map params = new HashMap<String, String>();
        params.put("email", email);
        Principal result = (Principal) (new RequestWrappers(this)).wrapRequestResource(params, new GetPrincipalByInfo());
        return (result != null) ? (new ObjectFactory().createPrincipal(result)) : (new ObjectFactory().createPrincipal(new Principal()));
    }


    private class GetPrincipalByInfo implements ILambda<Map, Principal> {

        @Override
        public Principal apply(Map params) throws NotInDataBaseException {
            return dbIntegrityService.getPrincipalByInfo((String) params.get("email"));
        }
    }

    ////////////////////////////////////////
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{principalid}/current")
    @Transactional(readOnly = true)
    public JAXBElement<CurrentPrincipalInfo> getCurrentPrincipalInfo(@PathParam("principalid") String externalIdentifier) throws IOException {
        Map params = new HashMap<String, String>();
        params.put("externalId", externalIdentifier);
        params.put("resource", this);
        CurrentPrincipalInfo result = (CurrentPrincipalInfo) (new RequestWrappers(this)).wrapRequestResource(params, new GetCurrentPrincipalInfo());
        return (result != null) ? (new ObjectFactory().createCurrentPrincipalInfo(result)) : (new ObjectFactory().createCurrentPrincipalInfo(new CurrentPrincipalInfo()));
    }
    
     private class GetCurrentPrincipalInfo implements ILambda<Map, CurrentPrincipalInfo> {

        @Override
        public CurrentPrincipalInfo apply(Map params) throws NotInDataBaseException {
            final Number principalID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString((String) params.get("externalId")), Resource.PRINCIPAL);
            final CurrentPrincipalInfo principalInfo = new CurrentPrincipalInfo();
            principalInfo.setRef(dbIntegrityService.getResourceURI(principalID, Resource.PRINCIPAL));
            principalInfo.setCurrentPrincipal(((PrincipalResource) params.get("resource")).ifLoggedIn(principalID));
            return principalInfo;
        }
    }
    ////////////////////////////// 

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("create/{remoteId}/{password}")
    public String createSpringAuthenticationRecord(@PathParam("remoteId") String remoteId, @PathParam("password") String password) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return "Logged in principal is null or anonymous.";
        }

        if (dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
            int result = dbIntegrityService.addSpringUser(remoteId, password, shaStrength, remoteId);
            return result + " record(s) has been added. Must be 2: 1 record for the principal, another for the authorities table.";
        } else {
            verboseOutput.ADMIN_RIGHTS_EXPECTED();
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return "Nothing is added.";
        }

    }
    
    ///////////////////////////////////////

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{remoteId}")
    public JAXBElement<Principal> addPrincipal(@PathParam("remoteId") String remoteId, Principal principal) throws IOException {

        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return new ObjectFactory().createPrincipal(new Principal());
        }

        Map params = new HashMap<String, Object>();
        params.put("remoteId", remoteId);
        params.put("newPrincipal", principal);

        if (dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
            return (new RequestWrappers(this)).wrapAddPrincipalRequest(params, new AddPrincipal());
        } else {
            verboseOutput.ADMIN_RIGHTS_EXPECTED();
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return new ObjectFactory().createPrincipal(new Principal());
        }

    }
    
      private class AddPrincipal implements ILambdaPrincipal<Map, Principal> {

        @Override
        public Principal apply(Map params) throws NotInDataBaseException, PrincipalExists {
            final Number principalID = dbIntegrityService.addPrincipal((Principal) params.get("newPrincipal"), (String) params.get("remoteId"));
            return dbIntegrityService.getPrincipal(principalID);
        }
    }

   
   /////////////////////////////////////

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    @Path("register/nonshibboleth")
    public JAXBElement<Principal> registerNonShibbolizedPrincipal(@FormParam("name") String name,
            @FormParam("remoteId") String remoteId, @FormParam("password") String password, @FormParam("email") String email)
            throws IOException {
        Principal newPrincipal = new Principal();
        newPrincipal.setDisplayName(name);
        newPrincipal.setEMail(email);

        Map params = new HashMap<String, Object>();
        params.put("remoteId", remoteId);
        params.put("password", password);
        params.put("shaStrength", shaStrength);
        params.put("newPrincipal", newPrincipal);

        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        return (new RequestWrappers(this)).wrapAddPrincipalRequest(params, new RegisterNonShibbolizedPrincipal());
    }
    
    private class RegisterNonShibbolizedPrincipal implements ILambdaPrincipal<Map, Principal> {

        @Override
        public Principal apply(Map params) throws NotInDataBaseException, PrincipalExists {
            final int updatedSpringTables = dbIntegrityService.addSpringUser((String) params.get("remoteId"), (String) params.get("password"), (Integer) params.get("shaStrength"), (String) params.get("remoteId"));
            final Number principalID = dbIntegrityService.addPrincipal((Principal) params.get("newPrincipal"), (String) params.get("remoteId"));
            return dbIntegrityService.getPrincipal(principalID);
        }
    }

  
   ///////////////////////////////////////////////

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    @Path("register/shibboleth")
    public JAXBElement<Principal> registerShibbolizedPrincipal(@FormParam("name") String name,
            @FormParam("remoteId") String remoteId, @FormParam("email") String email)
            throws IOException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        Principal newPrincipal = new Principal();
        newPrincipal.setDisplayName(name);
        newPrincipal.setEMail(email);
        Map params = new HashMap<String, Object>();
        params.put("remoteId", remoteId);
        params.put("newPrincipal", newPrincipal);

        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        return (new RequestWrappers(this)).wrapAddPrincipalRequest(params, new AddPrincipal());
    }
    
    ///////////////////////////////////////////////////

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("register/shibbolethasnonshibboleth")
    public String registerShibbolizedPrincipalAsNonShibb(@FormParam("remoteId") String remoteId, @FormParam("password") String password)
            throws IOException {
        int result = dbIntegrityService.addSpringUser(remoteId, password, shaStrength, remoteId);
        return result + " record(s) has been added. Must be 2: 1 record for the principal, another for the authorities table.";

    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    public JAXBElement<Principal> updatePrincipal(Principal principal) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return new ObjectFactory().createPrincipal(new Principal());
        }
        if (dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
            Map params = new HashMap<String, Object>();
            params.put("principal", principal);
            Principal result = (Principal) (new RequestWrappers(this)).wrapRequestResource(params, new UpdatePrincipal());
            return (result != null) ? (new ObjectFactory().createPrincipal(result)) : (new ObjectFactory().createPrincipal(new Principal()));

        } else {
            verboseOutput.ADMIN_RIGHTS_EXPECTED();
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return new ObjectFactory().createPrincipal(new Principal());
        }
    }
    ///////////////////////////////////

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    @Path("updateme")
    public JAXBElement<Principal> updatePrincipalFromForm(@FormParam("name") String name, @FormParam("email") String email)
            throws IOException {
        
        Principal newPrincipal = new Principal();
        newPrincipal.setDisplayName(name);
        newPrincipal.setEMail(email);
        Map params = new HashMap<String, Object>();
        params.put("newPrincipal", newPrincipal);
        Principal result = (Principal) (new RequestWrappers(this)).wrapRequestResource(params, new UpdatePrincipal());
        return (result != null) ? (new ObjectFactory().createPrincipal(result)) : (new ObjectFactory().createPrincipal(new Principal()));
    }
    
    
     private class UpdatePrincipal implements ILambda<Map, Principal> {
        @Override
        public Principal apply(Map params) throws NotInDataBaseException {
            Principal principal = (Principal) params.get("newPrincipal");
            Number principalID = (Number) params.get("principalID");            
            String uri = dbIntegrityService.getResourceURI(principalID, Resource.PRINCIPAL);
            principal.setURI(uri);
            Number principalIDupd = dbIntegrityService.updatePrincipal(principal);
            return dbIntegrityService.getPrincipal(principalID);
        }
    }
     
     //////////////////////////////////////

    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{externalId}/account/{accountType}")
    public String updatePrincipalAccount(@PathParam("externalId") String externalId, @PathParam("accountType") String accountType) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return "Nothing is updated.";
        }
        if (dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
            try {
                final boolean updated = dbIntegrityService.updateAccount(UUID.fromString(externalId), accountType);
                if (updated) {
                    return "The account was updated to " + dbIntegrityService.getTypeOfPrincipalAccount(dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalId), Resource.PRINCIPAL));
                } else {
                    loggerServer.debug("The account is not updated.");
                    httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Account is not updated.");
                    return "Account is not updated.";
                }
            } catch (NotInDataBaseException e) {
                loggerServer.debug(e.toString());;
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                return "Account is updated.";
            }
        } else {
            verboseOutput.ADMIN_RIGHTS_EXPECTED();
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return "Account is not updated.";
        }

    }

    @DELETE
    @Path("{principalId}")
    public String deletePrincipal(@PathParam("principalId") String externalIdentifier) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return "Nothings is deleted.";
        }
        if (dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
            try {
                final Number principalID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.PRINCIPAL);
                try {
                    final int result = dbIntegrityService.deletePrincipal(principalID);
                    return "There is " + result + " row deleted";
                } catch (PrincipalCannotBeDeleted e2) {
                    loggerServer.debug(e2.toString());;
                    httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e2.toString());
                    return "Nothing is deleted.";
                }
            } catch (NotInDataBaseException e) {
                loggerServer.debug(e.toString());;
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.toString());
                return "Nothing is deleted.";
            }
        } else {
            verboseOutput.ADMIN_RIGHTS_EXPECTED();
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return "Account is not updated.";
        }

    }

    // silly because it is trivial. harvest all logged in users via shibboleth!!
    private boolean ifLoggedIn(Number principalID) {
        return (httpServletRequest.getRemoteUser()).equals(dbIntegrityService.getPrincipalRemoteID(principalID));
    }

}
