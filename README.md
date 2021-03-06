# dwan-backend
DASISH Task 5.6. Annotation framework backend.

This repository contains the mirror of the last committed to svn source code for the annotator backend, with some security-sensitive information censored, located at https://trac.clarin.eu/browser/DASISH/t5.6/backend/annotator-backend/tags/git-copy-censored.

Javadocs annotations are added to the interface package eu.dasish.annotation.backend.dao and to the package eu.dasish.annotation.backend.rest. 

In the context.xml, shhaa.xml, database scripts and testing packages: usernames, passwords, hashes, names of servers and e-mails are replaced with the generic ones. You will need to replace it with your own settings.

If you have not set up your annotation database yet, you will need to run the script https://github.com/DASISH/dwan-backend/blob/master/annotator-backend/src/main/sql/DashishAnnotatorCreate.sql. If you are up to basic authentication you will also run the script https://github.com/DASISH/dwan-backend/blob/master/annotator-backend/src/main/sql/principal_non-shibb.sql.  

The DASISH schema is located at https://github.com/DASISH/dwan-backend/blob/master/annotator-backend/src/main/webapp/SCHEMA/DASISH-schema.xsd

The scripts and schema are also searchable within this repository. They are located in the standard subdirectories. 

The complete documentation can be at https://github.com/DASISH/dwan-documentation.


