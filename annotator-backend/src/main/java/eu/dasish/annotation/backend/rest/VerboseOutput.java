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
import org.slf4j.Logger;

/**
 *
 * @author olhsha
 */
public class VerboseOutput {

    private Logger logger;

    public VerboseOutput(Logger logger) {
        this.logger = logger;
    }

  

    public String _FORBIDDEN_RESOURCE_ACTION(String identifier, String resource, String action, String ownerName, String ownerEMail) {
        return " The logged-in principal cannot " + action + " the " + resource + " with the identifier " + identifier + ". Contact the resource's owner " + ownerName + " via the e-mail " + ownerEMail;
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
 
}

