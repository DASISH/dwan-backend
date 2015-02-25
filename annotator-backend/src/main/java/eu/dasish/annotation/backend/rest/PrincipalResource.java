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

import eu.dasish.annotation.backend.ForbiddenException;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * A REST class for GETting, POSTing, PUTting and DELETing principals or their substructures (child elements).
 * Every REST method in the case of successful completion produces an object of the declared output type
 * (a JAXB-element or a message string) or sends a HTTP-error with the corresponding diagnostics otherwise.
* @author olhsha
 */
@Component
@Path("/principals")
@Transactional(rollbackFor = {Exception.class, SQLException.class, IOException.class, ParserConfigurationException.class})
public class PrincipalResource extends ResourceResource {

    int shaStrength = 512;

 
    public PrincipalResource() {
    }

    /**
     * 
     * @param externalIdentifier the external UUID of a principal.
     * @return the {@link Principal} element representing the principal object with the "externalIdentifier".
     * @throws IOException is sending an error fails.
     */
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{principalid}")
    @Transactional(readOnly = true)
    public JAXBElement<Principal> getPrincipal(@PathParam("principalid") String externalIdentifier) throws IOException {
        Map params = new HashMap<String, String>();
        params.put("externalId", externalIdentifier);
        try {
            Principal result = (Principal) (new RequestWrappers(this)).wrapRequestResource(params, new GetPrincipal());
            return (result != null) ? (new ObjectFactory().createPrincipal(result)) : (new ObjectFactory().createPrincipal(new Principal()));
        } catch (NotInDataBaseException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return new ObjectFactory().createPrincipal(new Principal());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return new ObjectFactory().createPrincipal(new Principal());
        }
    }

    private class GetPrincipal implements ILambda<Map, Principal> {

        @Override
        public Principal apply(Map params) throws NotInDataBaseException {
            final Number principalID = dbDispatcher.getResourceInternalIdentifier(UUID.fromString((String) params.get("externalId")), Resource.PRINCIPAL);
            return dbDispatcher.getPrincipal(principalID);
        }
    }

    /**
     * 
     * @return a message containing the full name and the e-mail of the admin.
     * @throws IOException if sending an error in the call of "this.getPrincipalID()" fails.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("admin")
    @Transactional(readOnly = true)
    public String getAdmin() throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return " ";
        }
        return "The admin of the server database " + dbDispatcher.getDataBaseAdmin().getDisplayName() + " is availiable via e-mail " + dbDispatcher.getDataBaseAdmin().getEMail();
    }

   /**
    * 
    * @param email the e-mail of a principal.
    * @return a {@link Principal} representing the principal with the "email", 
    * if such a principal is found; otherwise "SC_NOT_FOUND" error is sent.
    * @throws IOException if sending an error fails.
    */
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("info")
    @Transactional(readOnly = true)
    public JAXBElement<Principal> getPrincipalByInfo(@QueryParam("email") String email) throws IOException {
        Map params = new HashMap<String, String>();
        params.put("email", email);
        try {
            Principal result = (Principal) (new RequestWrappers(this)).wrapRequestResource(params, new GetPrincipalByInfo());
            return (result != null) ? (new ObjectFactory().createPrincipal(result)) : (new ObjectFactory().createPrincipal(new Principal()));
        } catch (NotInDataBaseException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return new ObjectFactory().createPrincipal(new Principal());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return new ObjectFactory().createPrincipal(new Principal());
        }
    }

    private class GetPrincipalByInfo implements ILambda<Map, Principal> {

        @Override
        public Principal apply(Map params) throws NotInDataBaseException {
            return dbDispatcher.getPrincipalByInfo((String) params.get("email"));
        }
    }

 
    /**
     * 
     * @param externalIdentifier the external UUID of a principal.
     * @return a {@link CurrentPrincipalInfo} element containing "true" if the principal 
     * with externalIdentifier is logged-in in this session; 
     * with the current implementation does not make that much sense for me,
     * because the logged-in user knows his/her externalIDdentifier and knows that the other UUIDs
     * will be "not logged in", and therefore "false" is expected.
     * @throws IOException if sending an error fails.
     */
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{principalid}/current")
    @Transactional(readOnly = true)
    public JAXBElement<CurrentPrincipalInfo> getCurrentPrincipalInfo(@PathParam("principalid") String externalIdentifier) throws IOException {
        Map params = new HashMap<String, String>();
        params.put("externalId", externalIdentifier);
        try {
            CurrentPrincipalInfo result = (CurrentPrincipalInfo) (new RequestWrappers(this)).wrapRequestResource(params, new GetCurrentPrincipalInfo());
            return (result != null) ? (new ObjectFactory().createCurrentPrincipalInfo(result)) : (new ObjectFactory().createCurrentPrincipalInfo(new CurrentPrincipalInfo()));
        } catch (NotInDataBaseException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return new ObjectFactory().createCurrentPrincipalInfo(new CurrentPrincipalInfo());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return new ObjectFactory().createCurrentPrincipalInfo(new CurrentPrincipalInfo());
        }
    }

    private class GetCurrentPrincipalInfo implements ILambda<Map, CurrentPrincipalInfo> {

        @Override
        public CurrentPrincipalInfo apply(Map params) throws NotInDataBaseException {
            String externalId = (String) params.get("externalId");
            String loggedInExternalId = (dbDispatcher.getPrincipalExternalIDFromRemoteID(httpServletRequest.getRemoteUser())).toString();
            final CurrentPrincipalInfo principalInfo = new CurrentPrincipalInfo();
            principalInfo.setHref(getRelativeServiceURI() + "/principals/" + externalId);
            principalInfo.setCurrentPrincipal(externalId.equals(loggedInExternalId));
            return principalInfo;
        }
    }
    ////////////////////////////// 

//    @POST
//    @Produces(MediaType.TEXT_PLAIN)
//    @Path("create/{remoteId}/{password}")
//    public String createSpringAuthenticationRecord(@PathParam("remoteId") String remoteId, @PathParam("password") String password) throws IOException {
//        Number remotePrincipalID = this.getPrincipalID();
//        if (remotePrincipalID == null) {
//            return "Logged in principal is null or anonymous.";
//        }
//
//        if (dbDispatcher.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
//            try {
//                int result = dbDispatcher.addSpringUser(remoteId, password, shaStrength, remoteId);
//                return result + " record(s) has been added. Must be 2: 1 record for the principal, another for the authorities table.";
//            } catch (DuplicateKeyException e) {
//                loggerServer.error(e.toString());
//                httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
//                return e.toString();
//            }
//        } else {
//            this.ADMIN_RIGHTS_EXPECTED();
//            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
//            return "Nothing is added.";
//        }
//
//    }
    ///////////////////////////////////////
//    @POST
//    @Consumes(MediaType.APPLICATION_XML)
//    @Produces(MediaType.APPLICATION_XML)
//    @Path("{remoteId}")
//    public JAXBElement<Principal> addPrincipal(@PathParam("remoteId") String remoteId, Principal principal) throws IOException {
//
//        Number remotePrincipalID = this.getPrincipalID();
//        if (remotePrincipalID == null) {
//            return new ObjectFactory().createPrincipal(new Principal());
//        }
//
//        Map params = new HashMap<String, Object>();
//        params.put("remoteId", remoteId);
//        params.put("newPrincipal", principal);
//
//        if (dbDispatcher.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
//            return (new RequestWrappers(this)).wrapAddPrincipalRequest(params, new AddPrincipal());
//        } else {
//            this.ADMIN_RIGHTS_EXPECTED();
//            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
//            return new ObjectFactory().createPrincipal(new Principal());
//        }
//
//    }
    /**
     * 
     * @param name the name of the principal to be registered as a basic-authentication user.
     * @param remoteId the  remote id of the principal to be registered.
     * @param password the password selected by the principal to be registered.
     * @param email the email of the principal to be registered.
     * @return a {@link Principal} element representing the just registered principal.
     * @throws IOException if sending an error fails.
     */
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

        dbDispatcher.setResourcesPaths(this.getRelativeServiceURI());
        try {
            return (new RequestWrappers(this)).wrapAddPrincipalRequest(params, new RegisterNonShibbolizedPrincipal());
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return new ObjectFactory().createPrincipal(new Principal());
        } catch (PrincipalExists e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_CONFLICT, e2.getMessage());
            return new ObjectFactory().createPrincipal(new Principal());
        }
    }

    private class RegisterNonShibbolizedPrincipal implements ILambdaPrincipal<Map, Principal> {

        @Override
        public Principal apply(Map params) throws NotInDataBaseException, PrincipalExists {
            try {
                final int updatedSpringTables = dbDispatcher.addSpringUser((String) params.get("remoteId"), (String) params.get("password"), (Integer) params.get("shaStrength"), (String) params.get("remoteId"));
                final Number principalID = dbDispatcher.addPrincipal((Principal) params.get("newPrincipal"), (String) params.get("remoteId"));
                return dbDispatcher.getPrincipal(principalID);
            } catch (DuplicateKeyException e) {
                throw new PrincipalExists(e);
            }
        }
    }

    /**
     * 
     * @param name the name of the principal to be registered.
     * @param remoteId the remote shibboleth id.
     * @param email the email.
     * @return a {@link Principal} element representing the just registered principal.
     * @throws IOException if sending an error fails.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    @Path("register/shibboleth")
    public JAXBElement<Principal> registerShibbolizedPrincipal(@FormParam("name") String name,
            @FormParam("remoteId") String remoteId, @FormParam("email") String email)
            throws IOException {
        dbDispatcher.setResourcesPaths(this.getRelativeServiceURI());
        Principal newPrincipal = new Principal();
        newPrincipal.setDisplayName(name);
        newPrincipal.setEMail(email);
        Map params = new HashMap<String, Object>();
        params.put("remoteId", remoteId);
        params.put("newPrincipal", newPrincipal);

        dbDispatcher.setResourcesPaths(this.getRelativeServiceURI());
        try {
            return (new RequestWrappers(this)).wrapAddPrincipalRequest(params, new RegisterShibbolizedPrincipal());
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return new ObjectFactory().createPrincipal(new Principal());
        } catch (PrincipalExists e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_CONFLICT, e2.getMessage());
            return new ObjectFactory().createPrincipal(new Principal());
        }
    }

    private class RegisterShibbolizedPrincipal implements ILambdaPrincipal<Map, Principal> {

        @Override
        public Principal apply(Map params) throws NotInDataBaseException, PrincipalExists {
            final Number principalID = dbDispatcher.addPrincipal((Principal) params.get("newPrincipal"), (String) params.get("remoteId"));
            return dbDispatcher.getPrincipal(principalID);
        }
    }

    /**
     * It is a convenience method limiting proliferation of user profiles, however
     * the method is currently not used till security issues will be discussed with the LAT 
     * system administrators.
     * @param remoteId a shibboleth remote id, which will be set as a basic-authentication remote id as well.
     * @param password a shibboleth password which will be set as a basic-authentication password.
     * @return a message about amount of records; which must be 2 if everything goes well: 
     * 1 record for the "principal" table, 1is for the "authorities" tabl.
     * @throws IOException if sending an error fails.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("register/shibbolethasnonshibboleth")
    public String registerShibbolizedPrincipalAsNonShibb(@FormParam("remoteId") String remoteId, @FormParam("password") String password)
            throws IOException {
        try {
            int result = dbDispatcher.addSpringUser(remoteId, password, shaStrength, remoteId);
            return result + " record(s) has been added. Must be 2: 1 record for the principal, another for the authorities table.";
        } catch (DuplicateKeyException e) {
            loggerServer.error(e.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return e.toString();
        }
    }
    
    /**
     * 
     * @param principal a {@link Principal} object representing new information (name, e-mail) about the principal 
     * with the eternal UUID given in the object.
     * @return a {@link Principal} element representing the just updated principal.
     * @throws IOException if sending an error message fails.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    public JAXBElement<Principal> updatePrincipal(Principal principal) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return new ObjectFactory().createPrincipal(new Principal());
        }
        if (dbDispatcher.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
            Map params = new HashMap<String, Object>();
            params.put("newPrincipal", principal);
            try {
                Principal result = (Principal) (new RequestWrappers(this)).wrapRequestResource(params, new UpdatePrincipal());
                return (result != null) ? (new ObjectFactory().createPrincipal(result)) : (new ObjectFactory().createPrincipal(new Principal()));
            } catch (NotInDataBaseException e) {
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
                return new ObjectFactory().createPrincipal(new Principal());
            } catch (ForbiddenException e2) {
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
                return new ObjectFactory().createPrincipal(new Principal());
            }
        } else {
            this.ADMIN_RIGHTS_EXPECTED();
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return new ObjectFactory().createPrincipal(new Principal());
        }
    }
    /////////////////////////////////

    /**
     * 
     * @param name a new full name.
     * @param email a new email.
     * @return a {@link Principal} element representing the just updated principal.
     * @throws IOException if sending an error fails.
     * @throws NotInDataBaseException if the in-logged principal is not found in the database
     * or update fails.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    @Path("updateme")
    public JAXBElement<Principal> updatePrincipalFromForm(@FormParam("name") String name, @FormParam("email") String email)
            throws IOException, NotInDataBaseException {

        Principal newPrincipal = new Principal();
        newPrincipal.setDisplayName(name);
        newPrincipal.setEMail(email);
        String remoteId = httpServletRequest.getRemoteUser();
        String externalId = (dbDispatcher.getPrincipalExternalIDFromRemoteID(remoteId)).toString();
        String href = this.getRelativeServiceURI() + "/principals/" + externalId;
        newPrincipal.setId(externalId);
        newPrincipal.setHref(href);
        Map params = new HashMap<String, Object>();
        params.put("newPrincipal", newPrincipal);
        try {
            Principal result = (Principal) (new RequestWrappers(this)).wrapRequestResource(params, new UpdatePrincipal());
            return (result != null) ? (new ObjectFactory().createPrincipal(result)) : (new ObjectFactory().createPrincipal(new Principal()));
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return new ObjectFactory().createPrincipal(new Principal());
        }
    }

    private class UpdatePrincipal implements ILambda<Map, Principal> {

        @Override
        public Principal apply(Map params) throws NotInDataBaseException {
            Principal principal = (Principal) params.get("newPrincipal");
            Number principalIDupd = dbDispatcher.updatePrincipal(principal);
            return dbDispatcher.getPrincipal(principalIDupd);
        }
    }

   /**
    * 
    * @param externalId the external UUID of a principal.
    * @param accountType an account type: "user", "developer", "admin".
    * @return a message telling if and how the account is updated; this action can be performed only by admin.
    * @throws IOException 
    */
    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{externalId}/account/{accountType}")
    public String updatePrincipalAccount(@PathParam("externalId") String externalId, @PathParam("accountType") String accountType) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return "Nothing is updated.";
        }
        if (dbDispatcher.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
            try {
                final boolean updated = dbDispatcher.updateAccount(UUID.fromString(externalId), accountType);
                if (updated) {
                    return "The account was updated to " + dbDispatcher.getTypeOfPrincipalAccount(dbDispatcher.getResourceInternalIdentifier(UUID.fromString(externalId), Resource.PRINCIPAL));
                } else {
                    loggerServer.debug("The account is not updated.");
                    httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Account is not updated.");
                    return "Account is not updated.";
                }
            } catch (NotInDataBaseException e) {
                loggerServer.debug(e.toString());
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                return "Account is updated.";
            }
        } else {
            this.ADMIN_RIGHTS_EXPECTED();
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return "Account is not updated.";
        }

    }

    /**
     * 
     * @param externalIdentifier the external UUID of a principal to be deleted.
     * @return a message informing if deletion has been successful; see "error" messages if deletion 
     * has not taken place.
     * @throws IOException if sending error messages fails.
     */
    @DELETE
    @Path("{principalId}")
    public String deletePrincipal(@PathParam("principalId") String externalIdentifier) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return "Nothings is deleted.";
        }
        if (dbDispatcher.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
            try {
                final Number principalID = dbDispatcher.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.PRINCIPAL);
                try {
                    final int result = dbDispatcher.deletePrincipal(principalID);
                    return "There is " + result + " row deleted";
                } catch (PrincipalCannotBeDeleted e2) {
                    loggerServer.debug(e2.toString());;
                    httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e2.toString());
                    return "Nothing is deleted.";
                }
            } catch (NotInDataBaseException e) {
                loggerServer.debug(e.toString());
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.toString());
                return "Nothing is deleted.";
            }
        } else {
            this.ADMIN_RIGHTS_EXPECTED();
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return "Principal is not deleted.";
        }

    }
}
