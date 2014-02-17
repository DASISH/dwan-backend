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
    private  Logger logger;

    final static public String REMOTE_PRINCIPAL_NOT_FOUND = "The logged-in principal is not found in the database";
    
    public VerboseOutput(HttpServletResponse httpServletResponse, Logger logger) {
        this.httpServletResponse = httpServletResponse;
        this.logger = logger;
    }
    
    static public String FORBIDDEN_NOTEBOOK_READING(String identifier) {
        return " The logged-in principal cannot read the notebook with the identifier " + identifier;
    }
    
    static public String FORBIDDEN_NOTEBOOK_WRITING(String identifier) {
        return " The logged-in principal cannot write in the notebook with the identifier " + identifier;
    }
    
     static public String FORBIDDEN_ANNOTATION_READING(String identifier) {
        return " The logged-in principal cannot read the annotation with the identifier " + identifier;
    }
    
    static public String FORBIDDEN_ANNOTATION_WRITING(String identifier) {
        return " The logged-in principal cannot write in the annotation with the identifier " + identifier;
    }
    
     static public String FORBIDDEN_PERMISSION_CHANGING(String identifier) {
        return " The logged-in principal cannot change the permission of the resource with with the identifier " + identifier+". Only the owner of the resource is allowed to chnange permissions.";
    }

    static public String ILLEGAL_UUID(String identifier) {
        return ("The string '" + identifier + "' is not a valid UUID.");
    }

    static private String resourceNotFound(String externalIdentifier, String resourceType) {
        return ("A(n) " + resourceType + " with the indentifier " + externalIdentifier + " is not found in the database.");
    }

    static public String PRINCIPAL_NOT_FOUND(String externalIdentifier) {
        return resourceNotFound(externalIdentifier, "principal");
    }

    static public String ANNOTATION_NOT_FOUND(String externalIdentifier) {
        return resourceNotFound(externalIdentifier, "annotation");
    }

    static public String NOTEBOOK_NOT_FOUND(String externalIdentifier) {
        return resourceNotFound(externalIdentifier, "notebook");
    }

    static public String TARGET_NOT_FOUND(String externalIdentifier) {
        return resourceNotFound(externalIdentifier, "target");
    }

    static public String CACHED_REPRESENTATION_NOT_FOUND(String externalIdentifier) {
        return resourceNotFound(externalIdentifier, "cached representation");
    }

    static public String INVALID_PERMISSION_MODE(String permissionMode) {
        return permissionMode + " is an invalid permission value, which must be either owner, or reader, or writer.";
    }

    public void sendFailureMessage(String message, int responseCode) throws IOException {
        logger.debug(responseCode + ": " + message);
        httpServletResponse.sendError(responseCode, message);
    }
}
