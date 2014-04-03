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
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;

/**
 *
 * @author olhsha
 */
public class VerboseOutput {

    private HttpServletResponse httpServletResponse;
    private Logger logger;

    

    public VerboseOutput(HttpServletResponse httpServletResponse, Logger logger) {
        this.httpServletResponse = httpServletResponse;
        this.logger = logger;
    }

  

    private String _FORBIDDEN_RESOURCE_ACTION(String identifier, String resource, String action, String ownerName, String ownerEMail) {
        return " The logged-in principal cannot " + action + " the " + resource + " with the identifier " + identifier + ". Contact the resource's owner " + ownerName + " via the e-mail " + ownerEMail;
    }



    private String _RESOURCE_NOT_FOUND(String id, String resourceType) {
        return "A(n) " + resourceType + " with the indentifier " + id + " is not found in the database, or its internal database identifier is corrupted by setting to null.";
    }

   

    ///////////////////////////////
    public void NOT_LOGGED_IN() throws IOException {
        logger.debug(" The principal is not logged-in");
    }

    public void FORBIDDEN_NOTEBOOK_READING(String identifier, String ownerName, String ownerEMail) throws IOException  {
        logger.debug(this._FORBIDDEN_RESOURCE_ACTION(identifier, "notebook", "read", ownerName, ownerEMail));
    }

    public void FORBIDDEN_NOTEBOOK_WRITING(String identifier, String ownerName, String ownerEMail) throws IOException  {
        logger.debug(this._FORBIDDEN_RESOURCE_ACTION(identifier, "notebook", "write", ownerName, ownerEMail));
    }

    public void FORBIDDEN_ANNOTATION_READING(String identifier, String ownerName, String ownerEMail) throws IOException {
        logger.debug(this._FORBIDDEN_RESOURCE_ACTION(identifier, "annotation", "read", ownerName, ownerEMail));;
    }
    
     public void FORBIDDEN_ANNOTATION_BODY_WRITING(String identifier, String ownerName, String ownerEMail) throws IOException {
        logger.debug(this._FORBIDDEN_RESOURCE_ACTION(identifier, "annotation", "write", ownerName, ownerEMail));;
    }


    public void FORBIDDEN_ANNOTATION_DELETING(String identifier, String ownerName, String ownerEMail) throws IOException {
        logger.debug(this._FORBIDDEN_RESOURCE_ACTION(identifier, "annotation", "delete", ownerName, ownerEMail));
    }

    public void FORBIDDEN_ACCESS_CHANGING(String identifier, String ownerName, String ownerEMail) throws IOException {
        logger.debug(this._FORBIDDEN_RESOURCE_ACTION(identifier, "resource", "access change", ownerName, ownerEMail));
    }



    public void REMOTE_PRINCIPAL_NOT_FOUND(String remoteID, String adminName, String adminEmail) throws IOException {
        logger.debug("The logged in principal with the remote ID " + remoteID + " is not found in the database or his/her information is cirrupted. Contact the database admin " + adminName + " by the e-mail " + adminEmail);
    }
    
    public void REMOTE_PRINCIPAL_NOT_FOUND(String remoteID) throws IOException {        
        logger.debug(this._RESOURCE_NOT_FOUND(remoteID, "principal"));
    }

    public void PRINCIPAL_NOT_FOUND(String externalIdentifier) throws IOException  {
        logger.debug(this._RESOURCE_NOT_FOUND(externalIdentifier, "principal"));
    }

    public void ANNOTATION_NOT_FOUND(String externalIdentifier) throws IOException {
        logger.debug(this._RESOURCE_NOT_FOUND(externalIdentifier, "annotation"));
    }

    public void NOTEBOOK_NOT_FOUND(String externalIdentifier) throws IOException {
        logger.debug(this._RESOURCE_NOT_FOUND(externalIdentifier, "notebook"));
    }

    public void TARGET_NOT_FOUND(String externalIdentifier) throws IOException  {
        logger.debug(this._RESOURCE_NOT_FOUND(externalIdentifier, "target"));
    }

    public void CACHED_REPRESENTATION_NOT_FOUND(String externalIdentifier) throws IOException{
        logger.debug(this._RESOURCE_NOT_FOUND(externalIdentifier, "cached representation"));
    }

    
    
    public void CACHED_REPRESENTATION_IS_NULL() throws IOException {
        logger.debug("The cached representation with the give ID exist in the DB, however its BLOB is null.");
    }

    public void INVALID_ACCESS_MODE(String accessMode) throws IOException {
        logger.debug(accessMode + " is an invalid access value, which must be either owner, or read, or write.");
    }

    public void IDENTIFIER_MISMATCH(String identifier) throws IOException {
        logger.debug("Wrong request: the annotation (notebook) identifier   " + identifier + " and the annotation (notebook) ID from the request body do not match.");
    }

    public void ADMIN_RIGHTS_EXPECTED(String adminName, String adminEmail) throws IOException {
        logger.debug("The request can be performed only by the principal with the admin rights. Contact the admin " + adminName + " via e-mail " + adminEmail);
    }
    
     public void ADMIN_RIGHTS_EXPECTED() throws IOException {
        logger.debug("The request can be performed only by the principal with the admin rights.");
    }

    public void DEVELOPER_RIGHTS_EXPECTED() throws IOException {
        logger.debug("The request can be performed only by the principal with the developer's or admin rights. The logged in principal does not have either developer's or admin rights.");
    }
    
    public void ADMIN_NOT_FOUND() throws IOException  {
        logger.debug("The admin is not foun in he DB or tadmin's Data are corrupted.");
    }

    public void PRINCIPAL_NOT_FOUND_BY_INFO(String email) throws IOException  {
        logger.debug("The principal with the info (e-mail) " + email + " is not found in the database.");
    }

    public void PRINCIPAL_IS_NOT_ADDED_TO_DB() throws IOException {
        logger.debug("The principal is not added the database (probably becuase another principal with the same e-mail already exists in the data base) or the data were corrupted while adding. ");
    }

    public void ACCOUNT_IS_NOT_UPDATED() throws IOException {
        logger.debug("The account is not updated");
    }

    public void LOGOUT() throws IOException  {
        logger.debug("You are logged out.");
    }

    public void ANONYMOUS_PRINCIPAL() throws IOException{
        logger.debug("Shibboleth fall-back.  Logged in as 'anonymous' with no rights.");
    }
    
    public void PRINCIPAL_CANNOT_BE_DELETED(String externalIdentifier){
        logger.debug("the principal with the ID "+externalIdentifier+" cannote be deleted because (s)he is referred to in jounction tables of the databse. ");
    }
  
}

