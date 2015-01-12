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
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.HttpHeaders;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.core.util.Base64;

/**
 * Dummy security filter, very handy for unit testing.
 *
 */
public class DummySecurityFilter implements Filter {

    private final List<String> ALLOWED_PRINCIPALS = Arrays.asList(DummyPrincipal.DUMMY_PRINCIPAL.getName());

    /**
     * Dummy validation for unit tests
     *
     * @param principalname
     * @param password
     * @return
     */
    private boolean isValid(String principalname, String password) {
        return ALLOWED_PRINCIPALS.contains(principalname);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Principal principalResult = null;
        HttpServletRequest req = (HttpServletRequest) request;
        String authentication = req.getHeader(HttpHeaders.AUTHORIZATION);

        if (authentication != null) { //if no authentication then do nothing
            if (!authentication.startsWith("Basic ")) {
                throw new MappableContainerException(new AuthenticationException("Only HTTP Basic authentication is supported"));
            }
            authentication = authentication.substring("Basic ".length());
            String base64Decode = new String(Base64.decode(authentication.getBytes()));
            String[] values = base64Decode.split(":");
            if (values.length < 2) {
                throw new MappableContainerException(new AuthenticationException("Invalid syntax for principalname and password"));
            }
            final String principalname = values[0];
            String password = values[1];
            if ((principalname == null) || (password == null)) {
                throw new MappableContainerException(new AuthenticationException("Missing principalname or password"));
            }
            if (!isValid(principalname, password)) {
                throw new MappableContainerException(new AuthenticationException("Invalid principal/password"));
            }
            principalResult = new DummyPrincipal(principalname);
        }
        final Principal principal = principalResult;
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(req) {
            public boolean isPrincipalInRole(String role) {
                return true;
            }

            public boolean isSecure() {
                return false;
            }

            public Principal getPrincipalPrincipal() {
                return principal;
            }

            @Override
            public String getAuthType() {
                return HttpServletRequest.BASIC_AUTH;
            }

            @Override
            public String getRemoteUser() {
                return principal.getName();
            }
        };

        chain.doFilter(wrapper, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}