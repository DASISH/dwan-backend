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

So far a simple spring-security authentication (with hashed passwords, via database authentication 
provider) is implemented. The settings are in spring-config/applicationContext-security.xml. 
For hashing the class
org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder is used.

The setting are done based on the manual
 http://docs.spring.io/spring-security/site/docs/3.1.x/reference/springsecurity-single.html

=== License ===

GNU GPL