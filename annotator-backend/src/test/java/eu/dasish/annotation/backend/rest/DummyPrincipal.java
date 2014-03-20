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


public final class DummyPrincipal implements Principal {

    public static final DummyPrincipal DUMMY_ADMIN_PRINCIPAL = new DummyPrincipal("JUnit.Admin@test.com");
    public static final DummyPrincipal DUMMY_PRINCIPAL = new DummyPrincipal("olhsha@mpi.nl");
    public static final PrincipalCredentials DUMMY_CREDENTIALS = new PrincipalCredentials(DUMMY_PRINCIPAL) {

	@Override
	public String getDisplayName() {
	    return "J.Unit";
	}
    };
    
    public static final PrincipalCredentials DUMMY_ADMIN_CREDENTIALS = new PrincipalCredentials(DUMMY_ADMIN_PRINCIPAL);
    
    private final String principalname;

    public DummyPrincipal(String principalname) {
	this.principalname = principalname;
    }

    @Override
    public String getName() {
	return principalname;
    }

    public PrincipalCredentials getCredentials() {
	return new PrincipalCredentials(this);
    }
}