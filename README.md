Bricolage
---------

EAD Linked Data management and publishing.

Software output of the 2012 JISC-funded Bricolage project, undertaken by the University of Bristol.
http://bricolage.ilrt.bris.ac.uk/

This repository hosts three components, each a separate maven project:

bricol: webapp to provide browser-based management of EAD Linked Data publishing workflow

fuseki: deployment wrapper around Fuseki SPARQL server: http://jena.apache.org/documentation/serving_data/index.html

elda: deployment wrapper around Elda - an implementation of the Linked Data API: https://code.google.com/p/elda/


Installation
------------

Follow the maven build instructions in each project.

Deploy the bricol and elda war files to your web application container. These webapps must be able to contact
fuseki. The simplest route to this is to deploy the fuseki bundle to the same host.

Assuming host and port, visit http://localhost:8080/bricol/control