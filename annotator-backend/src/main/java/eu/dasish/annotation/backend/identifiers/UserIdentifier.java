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
package eu.dasish.annotation.backend.identifiers;

import javax.servlet.http.HttpServletRequest;

/**
 * Created on : Jun 28, 2013, 2:06:32 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class UserIdentifier {

    final private String identifier;

    public UserIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public UserIdentifier(HttpServletRequest httpServletRequest) {
        // todo: sort out how the user id is obtained and how it is stored it the db
        this.identifier = httpServletRequest.getRemoteUser();
    }

    @Override
    public String toString() {
        return identifier;
    }
}
