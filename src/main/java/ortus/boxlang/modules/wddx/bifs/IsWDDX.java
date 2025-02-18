/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package ortus.boxlang.modules.wddx.bifs;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.casters.GenericCaster;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.XML;

@BoxBIF

public class IsWDDX extends BIF {

	/**
	 * Constructor
	 */
	public IsWDDX() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "any", Key.value )
		};
	}

	/**
	 * Test whether a string or XML object is a valid WDDX packet.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.value The value to test for WDDX-ness
	 */
	public Object _invoke( IBoxContext context, ArgumentsScope arguments ) {
		Object val = arguments.get( Key.value );
		if ( val instanceof XML xmlObject ) {
			return xmlObject.getFirstChildOfName( "wddxPacket" ) != null;
		} else if ( GenericCaster.attempt( context, arguments.get( Key.value ), "xml" ).wasSuccessful()
		    &&
		    val instanceof String str ) {
			XML xmlObject = new XML( str );
			return xmlObject.getFirstChildOfName( "wddxPacket" ) != null;
		} else {
			return false;
		}

	}

}
