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

/**
 *
 * @author olhsha
 */

import java.security.Principal;
//import java.util.List;
//
//import org.apache.commons.codec.digest.DigestUtils;
//
//import de.mpg.aai.shhaa.model.AuthAttribute;
//import de.mpg.aai.shhaa.model.AuthAttributes;
//import de.mpg.aai.shhaa.model.AuthPrincipal;

/**
 * Wrapper class to hold the principalPrincipal and a displayName
 * 
 */
public class PrincipalCredentials {

    private final Principal principalPrincipal;

    public PrincipalCredentials(Principal principalPrincipal) {
        this.principalPrincipal = principalPrincipal;
    }

    public Principal getPrincipal() {
        return principalPrincipal;
    }

    public String getPrincipalName() {
        return principalPrincipal.getName();
    }

//    public String getPrincipalNameMD5Hex() {
//        return getPrincipalNameMD5Hex(principalPrincipal.getName());
//    }
//
//    public static String getPrincipalNameMD5Hex(String name){
//	return DigestUtils.md5Hex(name);
//    }

    public String getDisplayName() {
        String result = null;
//        if (principalPrincipal instanceof AuthPrincipal) {
//            List<String> displayNamesAttributes = Configuration.getInstance().getDisplayNameShibbolethKeys();
//            AuthPrincipal authPrincipal = (AuthPrincipal) principalPrincipal;
//            for (String key : displayNamesAttributes) {
//                result = getValue(authPrincipal, key);
//                if (result != null) {
//                    break;
//                }
//            }
//        }
        if (result == null) {
            result = getPrincipalName();
        }
        return result;
    }

//    private String getValue(AuthPrincipal authPrincipal, String key) {
//        String result = null;
//        AuthAttributes attributes = authPrincipal.getAttribues();
//        if (attributes != null) {
//            AuthAttribute<String> authAttribute = (AuthAttribute<String>) attributes.get(key);
//            if (authAttribute != null) {
//                result = authAttribute.getValue();
//            }
//        }
//        return result;
//    }

    @Override
    public String toString() {
	return getPrincipal().toString();
    }
}

