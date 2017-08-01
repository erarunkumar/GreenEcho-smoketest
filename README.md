GreenEchoService Project
=========================
Release 1.0.0

* Contains the following
    * App Dynamics Hook
	* User Provided Service for Configurations
	* User Provided Service for Other services to call it via Cloud Foundry
	* Added Security tests to verify the Secuerity Checklist for the Cloud Foundry Platform 


Echo Test:
* Verify you can get a good message back from the Echo service
https://greenecho-int.cf.prod.tvlport.com/rest/echo/hello/My%20Name


Security Tests:

* Verify that you can reach the an external URL via the STP Proxy as a User Provided Service:

	* https://greenecho-int.cf.prod.tvlport.com/rest/echo/ExternalCallSTPProxy/http://www.google.com

* Verify that you can NOT reach the an external URL without the STP Proxy as a User Provided Service:

	* https://greenecho-int.cf.prod.tvlport.com/rest/echo/ExternalCallDirect/http://www.google.com

* Verify that you can reach the an internal Cloud URL using an User Provided Service to limit/audit access:

	* https://greenecho-int.cf.prod.tvlport.com/rest/echo/InternalCall/http://greenecho-int.stackato.tvlport.com/rest/db/ExternalDbService/

* Verify that you can NOT reach the an internal Cloud URL without an User Provided Service to limit/audit access:

	* https://greenecho-int.cf.prod.tvlport.com/rest/echo/InternalCall/http://greenecho-db.cf.prod.tvlport.com/rest/db/ExternalDbService/

Database Test URL:

* https://greenecho-int.cf.prod.tvlport.com/rest/db/ExternalDbService/ 

