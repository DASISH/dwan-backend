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

import eu.dasish.annotation.backend.ForbiddenException;
import eu.dasish.annotation.backend.NotInDataBaseException;

/**
 * This is a help interface used in wrapper methods for REST requests, see {@link RequestWrappers}.
 * @author olhsha
 */
public interface ILambda<Map, R> {
    
    /**
     * 
     * @param params list of pairs parameter->value; e.g. such map can contain "externalId"->"aaaaaaaaaa".
     * @return an instance of "R" corresponding to these parameter values,e.g. an annotation if R is instantiated as "Annotation".
     * @throws NotInDataBaseException see various implementations in REST resource classes.
     * @throws ForbiddenException see various implementations in REST resource classes.
     */
    public R apply(Map params)   throws NotInDataBaseException, ForbiddenException;
    
}