# ⚡︎ BoxLang Module: WDDX Module

The WDDX module provides the bridge between the WDDX exchange format and BoxLang. It involves reading and parsing XML, converting data types, handling errors, and ensuring performance and compatibility. The module enables the integration of legacy systems with new applications.

It provides the `wddx` component along with its actions of `bx2wddx`, `wddx2bx`, `bx2js` and `wddx2js`.   Note that if the compatibility module is installed in the BoxLang runtime, the  usage of `bx` in the action changes to `cfml`.

_*Important Note:* WDDX is, effectively, no longer supported for new development and its continued use as a data interchange format is is highly discouraged.  This module should only be used to maintain compatibility for legacy code and developers should be encouraged to sunset its usage._



## Contributed Functions

The compat module will contribute the following components globally:

* wddx ( e.g. `<cfwddx.../>` and `<bx:wddx.../>` depending on the template type, and `wddx...;` in script )


## Ortus Sponsors

BoxLang is a professional open-source project and it is completely funded by the [community](https://patreon.com/ortussolutions) and [Ortus Solutions, Corp](https://www.ortussolutions.com).  Ortus Patreons get many benefits like a cfcasts account, a FORGEBOX Pro account and so much more.  If you are interested in becoming a sponsor, please visits our patronage page: [https://patreon.com/ortussolutions](https://patreon.com/ortussolutions)