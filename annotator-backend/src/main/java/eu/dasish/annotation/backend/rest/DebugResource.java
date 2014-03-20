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

import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.ObjectFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olhsha
 */
@Component
@Path("/debug")
public class DebugResource extends ResourceResource {

    private final String developer = "developer";

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("annotations")
    @Transactional(readOnly = true)
    public JAXBElement<AnnotationInfoList> getAllAnnotations() throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID != null) {
            String typeOfAccount = dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID);
            if (typeOfAccount.equals(admin) || typeOfAccount.equals(developer)) {
                final AnnotationInfoList annotationInfoList = dbIntegrityService.getAllAnnotationInfos();
                return new ObjectFactory().createAnnotationInfoList(annotationInfoList);
            } else {
                verboseOutput.DEVELOPER_RIGHTS_EXPECTED();
            }
        }
        return new ObjectFactory().createAnnotationInfoList(new AnnotationInfoList());
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/logDatabase/{n}")
    @Transactional(readOnly = true)
    public String getDasishBackendLog(@PathParam("n") int n) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID != null) {
            String typeOfAccount = dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID);
            if (typeOfAccount.equals(admin) || typeOfAccount.equals(developer)) {
                return logFile("eu.dasish.annotation.backend.logDatabaseLocation", n);
            } else {
                verboseOutput.DEVELOPER_RIGHTS_EXPECTED();
            }
        }
        return " ";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/remoteID")
    @Transactional(readOnly = true)
    public String getLoggedInRemoteID(){
        return httpServletRequest.getRemoteUser();
    }

    /////
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/logServer/{n}")
    @Transactional(readOnly = true)
    public String getDasishServerLog(@PathParam("n") int n) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID != null) {
            String typeOfAccount = dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID);
            if (typeOfAccount.equals(admin) || typeOfAccount.equals(developer)) {
                return logFile("eu.dasish.annotation.backend.logServerLocation", n);
            } else {
                verboseOutput.DEVELOPER_RIGHTS_EXPECTED();
            }
        }
        return " ";

    }

    //////////////////////////////////
    @PUT
    @Produces(MediaType.TEXT_XML)
    @Path("account/{principalId}/make/{account}")
    @Transactional(readOnly = true)
    public String updatePrincipalsAccount(@PathParam("principalId") String principalId, @PathParam("account") String account) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID != null) {
            String typeOfAccount = dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID);
            if (typeOfAccount.equals(admin)) {
                final boolean update = dbIntegrityService.updateAccount(UUID.fromString(principalId), account);
                return (update ? "The account is updated" : "The account is not updated, see the log.");
            } else {
                verboseOutput.ADMIN_RIGHTS_EXPECTED(dbIntegrityService.getDataBaseAdmin().getDisplayName(), dbIntegrityService.getDataBaseAdmin().getEMail());
            }
        } 
        return " ";
    }

    ///////////////////////////////////////////////////
    private String logFile(String location, int n) throws IOException {
        BufferedReader read = new BufferedReader(new FileReader(context.getInitParameter(location)));
        List<String> lines = new ArrayList<String>();
        StringBuilder result = new StringBuilder();
        int i = 0;
        String line;
        while ((line = read.readLine()) != null) {
            lines.add(line);
            i++;
        }
        // want to read the last n rows, i.e. the rows (i-1), (i-1-1),...,(i-1-(n-1))
        int last = (i > n) ? (i - n) : 0;
        for (int j = i - 1; j >= last; j--) {
            result.append(lines.get(j)).append("\n");
        }
        return result.toString();
    }
}
