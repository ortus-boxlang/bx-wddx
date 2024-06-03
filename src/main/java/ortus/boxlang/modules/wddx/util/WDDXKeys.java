/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ortus.boxlang.modules.wddx.util;

import ortus.boxlang.runtime.scopes.Key;

/**
 * Represents a case-insenstive key, while retaining the original case too.
 * Implements the Serializable interface in case duplication is requested within
 * a native HashMap or ArrayList
 */
public class WDDXKeys {

	public static final Key	_MODULE_NAME		= Key.of( "wddx" );

	public static final Key	toplevelvariable	= Key.of( "toplevelvariable" );
	public static final Key	usetimezoneinfo		= Key.of( "usetimezoneinfo" );
	public static final Key	validate			= Key.of( "validate" );
	public static final Key	xmlconform			= Key.of( "xmlconform" );

}