Deployment package for Fuseki as used by the Bricolage project
http://bricolage.ilrt.bris.ac.uk/

Build bundle:

> mvn clean
> mvn assembly:single [-Pprod]

Default profile: localhost

Configuration
-------------
- src/main/filters/filter-<env>.properties
    Per-environment properties
    
- src/main/webapp/specs/bricolage.ttl
    Linked Data API settings.

