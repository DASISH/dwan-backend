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

import java.util.UUID;

/**
 *
 * @author olhsha
 */
public interface ResourceDao {
    
    
    public void setServiceURI(String serviceURI);
    
     /**
     * 
     * @param externalID
     * @return internal identifier of the resource with externalID, or null if there is no resource with this identifier
     */
    public Number getInternalID(UUID externalId);
    
    /**
     * 
     * @param internalId
     * @return the UUID (external ID) of the resource with the "internalID".
     */
    public UUID getExternalID(Number internalId);
}
