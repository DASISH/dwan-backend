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
package eu.dasish.annotation.backend.dao;

import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.PrincipalCannotBeDeleted;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.Principal;
import java.util.List;
import java.util.UUID;


/**
 *
 * @author olhsha
 */
public interface PrincipalDao extends ResourceDao{
    
     public Principal getPrincipal(Number internalID);
     
     public Principal getPrincipalByInfo(String  eMail) throws NotInDataBaseException;
     
     public Number getDBAdminID();
     
     // where is it used?
     public boolean principalIsInUse(Number principalID);
     
     // where is it used??
     public boolean principalExists(String remoteID);
     
     public String getTypeOfPrincipalAccount(Number internalID);
     
     public String getRemoteID(Number internalID);
     
     
     public Number getPrincipalInternalIDFromRemoteID(String remoteID) throws NotInDataBaseException;
     
     public UUID getPrincipalExternalIDFromRemoteID(String remoteID) throws NotInDataBaseException;
     
     
     public List<Number> getPrincipalIDsWithAccessForNotebook(Number notebookID, Access access);
     
     public boolean updateAccount(UUID externalID, String account) throws NotInDataBaseException;
     
     public Number updatePrincipal(Principal principal) throws NotInDataBaseException;
    
     public Number addPrincipal(Principal principal, String remoteID) throws NotInDataBaseException;
     
     public int addSpringUser(String username, String password, int strength, String salt);
     
     public int addSpringAuthorities(String username);
     
     public int deletePrincipal(Number intenralID) throws PrincipalCannotBeDeleted;
     
     
}
