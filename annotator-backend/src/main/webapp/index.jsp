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
<!--        <b>Notebooks</b><br>
      api/notebooks<br>
        POST api/annotations and PUT /notebooks/_nid_?annotation=_aid_.<br>
        <br>
        GET <a href="api/notebooks">api/notebooks</a> Returns notebook-infos for the notebooks accessible to the current user.<br>
        GET <a href="api/notebooks/owned">api/notebooks/owned</a> Returns the list of all notebooks owned by the current logged user.<br>
        GET <a href="api/notebooks/_nid_/readers">api/notebooks/_nid_/readers</a> Returns the list of _uid_ who allowed to read the annotations from notebook.<br>
        GET <a href="api/notebooks/_nid_/writers">api/notebooks/_nid_/writers</a> Returns the list of _uid_ that can add annotations to the notebook.<br>
        GET <a href="api/notebooks/_nid_/metadata">api/notebooks/_nid_/metadata</a> Get all metadata about a specified notebook _nid_, including the information if it is private or not.<br>
        GET <a href="api/notebooks/_nid_?maximumAnnotations=100&startAnnotation=5&orderby=1&orderingMode=0">api/notebooks/_nid_?maximumAnnotations=limit&startAnnotation=offset&orderby=orderby&orderingMode=1|0</a> Get the list of all annotations _aid_-s contained within a Notebook with related metadata. Parameters: _nid_, optional maximumAnnotations specifies the maximum number of annotations to retrieve (default -1, all annotations), optional startAnnotation specifies the starting point from which the annotations will be retrieved (default: -1, start from the first annotation), optional orderby, specifies the RDF property used to order the annotations (default: dc:created ), optional orderingMode specifies if the results should be sorted using a descending order desc=1 or an ascending order desc=0 (default: 0 ).<br>
        PUT <form action="api/notebooks/_nid_" method="put"><input type="submit" /></form>/notebooks/_nid_	Modify metadata of _nid_. The new notebook?s name must be sent in request?s body.<br>
        PUT /notebooks/_nid_/_aid_	Adds an annotation _aid_ to the list of annotations of _nid_.<br>        
--        PUT <form action="api/notebooks/_nid_/setPrivate=true" method="put"><input type="submit" /></form>api/notebooks/_nid_/setPrivate=[true, false]	Sets the specified Notebook as private or not private.<br>--
        POST api/notebooks/	Creates a new notebook. This API returns the _nid_ of the created Notebook in response?s payload and the full URL of the notebook adding a Location header into the HTTP response. The name of the new notebook can be specified sending a specific payload.<br>
        POST api/notebooks/_nid_	Creates a new annotation in _nid_. The content of an annotation is given in the request body. In fact this is a short cut of two actions:<br>
        DELETE <form action="api/notebooks/_nid_" method="DELETE"><input type="submit" /></form>api/notebooks/_nid_	Delete _nid_. Annotations stay, they just lose connection to _nid_.<br> 
 -->
        
      <b>Test URI-s</b><br>
      <b>All output xml-s are valid w.r.t. the schema</b><br>
     GET <a href="api/users/00000000-0000-0000-0000-0000000000112">api/users/00000000-0000-0000-0000-0000000000112</a>  !Problem: how to ask the "servlet context" if this user is current? so far all are set to "true"<br> 
     GET <a href="api/users/info?email=twagoo@mpi.nl">api/users/info?email=twagoo@mpi.nl</a>  <br>
     GET <a href="api/annotations?link=Sagrada">api/annotations/info?link=Sagrada</a> !Comment: more tests are necessary to test on all query parameters  </br>
     GET <a href="api/annotations?link=Gaud">api/annotations/info?link=Gaud</a> !Comment: more tests are necessary to test on all query parameters  </br>
     GET <a href="api/annotations/00000000-0000-0000-0000-000000000021">api/annotations/00000000-0000-0000-0000-000000000021</a>  </br>
     GET <a href="api/annotations/00000000-0000-0000-0000-000000000021/targets">api/annotations/00000000-0000-0000-0000-000000000021/targets</a>  </br>
     GET <a href="api/annotations/00000000-0000-0000-0000-000000000021/permissions">api/annotations/00000000-0000-0000-0000-000000000021/permissions</a><br>
     GET <a href="api/targets/00000000-0000-0000-0000-000000000032">api/targets/00000000-0000-0000-0000-000000000032</a>  <br>
     GET <a href="api/targets/00000000-0000-0000-0000-000000000032/versions">api/targets/00000000-0000-0000-0000-000000000032/versions</a>   <br>
     GET <a href="api/cached/00000000-0000-0000-0000-000000000051/metadata">api/cached/00000000-0000-0000-0000-000000000051/metadata</a><br>
     GET <a href="api/cached/00000000-0000-0000-0000-000000000051/content">api/cached/00000000-0000-0000-0000-000000000051/content></a> !Problem: works only on image.jpeg, end maps png-blobs to jpegs.
    </body>
        </html>
