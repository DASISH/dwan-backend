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
import eu.dasish.annotation.schema.Access;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author olhsha
 */
public interface ResourceDao {

    void setResourcePath(String relResourcePath);

    Number getInternalID(UUID externalId) throws NotInDataBaseException;

    UUID getExternalID(Number internalId);

  
  
    /**
     *
     * @param internalID
     * @return URI string of the resource )of the type set in resourceTableName)
     * with internalID
     */
    String getHrefFromInternalID(Number internalID);
    
    Number getInternalIDFromHref(String href)  throws NotInDataBaseException;

    List<Map<Number, String>> getPermissions(Number resourceID);

    Access getPublicAttribute(Number resourceID);
}
