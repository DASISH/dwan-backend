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
    
    
    /**
     * 
     * @param internalID the internal database ID of a principal.
     * @return the {@link Principal} object that represents the principal with the internal database id "internalID".
     */
     public Principal getPrincipal(Number internalID);
     
     /**
      * 
      * @param eMail an e-mail.
      * @return the {@link Principal} object representing the principal with the e-mail "eMail".
      * @throws NotInDataBaseException if such a principal is not found.
      */
     public Principal getPrincipalByInfo(String  eMail) throws NotInDataBaseException;
     
     /**
      * 
      * @return  the internalID of a principal who is the admin of the data base (the first admin in the list of admins).
      */
     public Number getDBAdminID();
     
    /**
     * 
     * @param principalID the internal database id of a principal.
     * @return true iff "principalID" occurs in one of the junction tables.
     */
     public boolean principalIsInUse(Number principalID);
     
     /**
      * 
      * @param remoteID the remote id of a principal.
      * @return true iff a principal with "remoteID" exists in the table "principal".
      */
     public boolean principalExists(String remoteID);
     
     /**
      * 
      * @param internalID the internal database id of a principal.
      * @return the type of a principal account: admin, developer, user.
      */
     public String getTypeOfPrincipalAccount(Number internalID);
     
     /**
      * 
      * @param internalID the internal database id of a principal.
      * @return the remote id of the principal.
      */
     public String getRemoteID(Number internalID);
     
     
     /**
      * 
      * @param remoteID the remote id of a principal.
      * @return the internal database id of the principal with "remotedID".
      * @throws NotInDataBaseException if there is no principal with "remoteID".
      */
     public Number getPrincipalInternalIDFromRemoteID(String remoteID) throws NotInDataBaseException;
     
     /**
      * 
      * @param remoteID the remote id of a principal.
      * @return the external UUID of the principal with "remotedID".
      * @throws NotInDataBaseException  if there is no principal with "remoteID".
      */
     public UUID getPrincipalExternalIDFromRemoteID(String remoteID) throws NotInDataBaseException;
     
     /**
      * 
      * @param fullName the full name of a principal.
      * @return the external UUID of the principal with "fullName.
      * @throws NotInDataBaseException if there is no principal with "fullName".
      */
     public UUID getExternalIdFromName(String fullName) throws NotInDataBaseException;
     
     /**
      * 
      * @param notebookID the internal database id of a notebook.
      * @param access an {@link Access} object. 
      * @return  the list of internal database ids of the principals which have access "access"-value
      * for the "notebookID".
      */
     public List<Number> getPrincipalIDsWithAccessForNotebook(Number notebookID, Access access);
     
  /**
   * 
   * @param externalID the external UUID of a principal.
   * @param account the type of an account: admin, developer, user.
   * @return true, iff the account of "externalId" has been updated to "account".
   * @throws NotInDataBaseException iff no principal with "externalId" is found.
   */
     public boolean updateAccount(UUID externalID, String account) throws NotInDataBaseException;
     
     /**
      * 
      * @param principal a {@link Principal} object which must update the principal referred by "externalId" given in this object.
      * @return the internal id of the principal which has been updated.
      * @throws NotInDataBaseException if a principal with the externalId given in the "principal" parameter is not found.
      */
     public Number updatePrincipal(Principal principal) throws NotInDataBaseException;
    
     /**
      * 
      * @param principal a {@link Principal} object to be added to the database.
      * @param remoteID the remote of of the principal to be added.
      * @return the internal id of the just added principal.
      * @throws NotInDataBaseException if adding the principal fails.
      */
     public Number addPrincipal(Principal principal, String remoteID) throws NotInDataBaseException;
     
     /**
      * 
      * @param username the name of a user for spring basic authentication.
      * @param password a password of a user for spring authentication.
      * @param strength the strength of a password.
      * @param salt 
      * @return the number of added rows in "users" table; must be 1 if the user is added.
      */
     public int addSpringUser(String username, String password, int strength, String salt);
     
     /**
      * 
      * @param username the name of a user for spring basic authentication.
      * @return the amount of rows added to "authorities" table; "1" if "username" is added with the default "ROLE_USER"; 0 otherwise.
      */
     public int addSpringAuthorities(String username);
     
     /**
      * 
      * @param intenralID the internal ID of a principal to be deleted,
      * @return the amount of deleted rows; "1" if deleted, "0" otherwise.
      * @throws PrincipalCannotBeDeleted if the principal is in use, i.e. mentioned in one of the junction tables.
      */
     public int deletePrincipal(Number intenralID) throws PrincipalCannotBeDeleted;
     
     
}
