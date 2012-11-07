Deployment package for Fuseki as used by the Bricolage project
http://bricolage.ilrt.bris.ac.uk/

Build bundle:

> mvn clean
> mvn assembly:single [-Pprod]

Default profile: localhost

Configuration
-------------
- src/main/assembly/filter-<env>.properties
    Per-environment properties
    
- src/main/fuseki/config/config-tdb.ttl
    Fuseki settings to be distributed in bundle

