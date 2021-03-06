<%--

    Copyright (C) 2013 DASISH

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

--%>
<html>
    <body>
        <h2>DASISH REST API</h2>
        <p><a href="api/myresource">Jersey resource</a>
        <p><a href="https://trac.clarin.eu/wiki/DASISH/SpecificationDocument#RESTAPI">https://trac.clarin.eu/wiki/DASISH/SpecificationDocument#RESTAPI</a></p>
        <br>
        <h3>Services for which you do not need to be logged-in</h3>
        
        <br>
        <a href=<%=application.getInitParameter("eu.dasish.annotation.backend.schemaLocation")%> > Get DASISH schema</a> <br> 
        <br>
        <br>
        <a href="registerNonShibbolethPrincipal.html"> Register a user for basic authentication logging-in</a><br> 
        <a href="registerShibbolethPrincipal.html"> Register a shibboleth user</a> <br> 
        <br>
        <br>
        <a href="api/authentication/login"> login  </a> <br> 
         <br> 
         
        <br>
        <h3>Services for which you need to be logged in</h3>
        <br> 
        <a href="api/authentication/logout"> logout</a> <br> 
        <br>
        <br> 
        <a href="changePermissions.html"> Change access mode for a user and an annotation </a><br> 
        <a href="publicAccess.html"> Change public access mode for an annotation </a> <br> 
        <br>
        
        <b>Test URI-s</b><br>
        <br> 
        <a href="updatePrincipal.html"> Update logged-in user.</a> <br> 
        <br>
        GET <a href="api/projectinfo/version">api/projectinfo/version</a> <br> 
        <br>
        GET <a href="api/authentication/principal">api/authentication/principal</a> <br> 
        GET <a href="api/principals/admin">api/principals/admin</a><br> 
        GET <a href="api/principals/a0000000-0000-0000-0000-0000000000114">api/principals/a0000000-0000-0000-0000-0000000000114</a> <br> 
        GET <a href="api/principals/a0000000-0000-0000-0000-0000000000114/current">api/principals/a0000000-0000-0000-0000-0000000000114/current</a><br>  
<!--        !Problem: how to ask the servlet if the given user is logged in, may be by some other running somewhere client<br> -->
        GET <a href="api/principals/info?email=Twan.Goosen@mpi.nl">api/principals/info?email=twan.Goosen@mpi.nl</a>  <br>
        GET <a href="api/annotations?link=Sagrada&matchMode=contains">api/annotations?link=Sagrada&matchMode=contains</a>  <br>
        GET <a href="api/annotations?link=http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia&matchMode=exact">api/annotations?link=http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia&matchMode=exact</a>  <br>
        GET <a href="api/annotations?link=http://nl.wikipedia.org/wiki&matchMode=starts_with">api/annotations?link=http://nl.wikipedia.org/wiki&matchMode=starts_with</a>  <br>
        GET <a href="api/annotations?link=_Fam%C3%ADlia&matchMode=ends_with">api/annotations?link=_Fam%C3%ADlia&matchMode=ends_with</a>  <br>
        GET <a href="api/annotations?link=http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia">api/annotations?link=http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia</a>  <br>
        GET <a href="api/annotations?link=http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD">api/annotations?link=http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD</a>  <br>
        GET <a href="api/annotations?after=2013-02-04 15:57:58.046908&before=2014-12-31 10:08:16.213186">api/annotations?after=2014-02-04 15:57:58.046908&before=2014-12-31 10:08:16.213186</a><br> 
<!--        !Comment: What is "namespace" query parameter? Must be implemented and tested <br>-->
        GET <a href="api/annotations/a0000000-0000-0000-0000-000000000022">api/annotations/a0000000-0000-0000-0000-000000000022</a>  </br>
        GET <a href="api/annotations/a0000000-0000-0000-0000-000000000022/targets">api/annotations/a0000000-0000-0000-0000-000000000022/targets</a>  </br>
        GET <a href="api/annotations/a0000000-0000-0000-0000-000000000022/permissions">api/annotations/a0000000-0000-0000-0000-000000000022/permissions</a><br>
        GET <a href="api/targets/a0000000-0000-0000-0000-000000000032">api/targets/a0000000-0000-0000-0000-000000000032</a>  <br>
        GET <a href="api/targets/a0000000-0000-0000-0000-000000000032/versions">api/targets/a0000000-0000-0000-0000-000000000032/versions</a>   <br>
        GET <a href="api/cached/a0000000-0000-0000-0000-000000000051/metadata">api/cached/a0000000-0000-0000-0000-000000000051/metadata</a><br>
        GET <a href="api/cached/a0000000-0000-0000-0000-000000000511/content">api/cached/a0000000-0000-0000-0000-0000000000511/content</a><br> 
        GET <a href="api/cached/a0000000-0000-0000-0000-000000000051/stream">api/cached/a0000000-0000-0000-0000-000000000051/stream</a><br> 
        
        
        
        <br> 
        <br>
        <b>Debugging URI's (only for developers)</b><br>
        GET <a href="api/annotations/stressTest?n=33">api/annotations/stressTest?n=33</a> <br>
        GET <a href="api/debug/remoteID">api/debug/remoteID</a> <br>
        GET <a href="api/debug/uuid">api/debug/uuid</a> (random uuid) <br>
        GET <a href="api/debug/annotations">api/debug/annotations</a> <br>
        GET <a href="api/debug/logDatabase/32">api/debug/logDatabase/32</a> <br>
        GET <a href="api/debug/logServer/32">api/debug/logServer/32</a> <br>
    </body>
</html>
