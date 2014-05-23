DASISH web-annotator back-end 

The DASISH web-annotator back-end is a part of the DAISH annotating framework announced in task 5.6
of  DASISH project.   The information about the whole project can be found at 
http://dasish.eu/. The deatiled technical information about DASISH annotating framework can be 
found at https://trac.clarin.eu/wiki/DASISH. For this you need a clarin wiki account.

The DASISH web-annotator back-end consists of two parts: the database, where the 
annotations are stored, and the Jerseys web-application that by means of REST requests 
performs  authentified read/write  access to the database.

=== Requests ===

The requests are specified in

https://trac.clarin.eu/wiki/DASISH/SpecificationDocument#RESTApplicationProgrammingInterface

and examplified in

https://trac.clarin.eu/wiki/DASISH/XSD%20and%20XML

=== Logging in ===

Authentication is possible for lux16 and lux17 for 2 settings: vi ashibboleth and as spring basic.


=== License ===

GNU GPL