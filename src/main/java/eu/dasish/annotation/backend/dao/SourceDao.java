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

import eu.dasish.annotation.schema.NewOrExistingSourceInfos;
import eu.dasish.annotation.schema.SourceInfo;
import java.util.List;

/**
 *
 * @author olhsha
 */
public interface SourceDao extends ResourceDao{
    
    /**
     * 
     * @param annotationID
     * @return the Information about the target sources to which annotationId refers
     */
    public List<SourceInfo> getSourceInfos(Number annotationID);
    
   /**
    * 
    * @param sourceInfoList
    * @return the list of NewOrExistingSourceo objects in such a way  that an element of the sourceInfoList is injected into an element of the return list
    */
   
    public NewOrExistingSourceInfos contructNewOrExistingSourceInfo(List<SourceInfo> sourceInfoList);
        
}
