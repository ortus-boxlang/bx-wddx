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
package ortus.boxlang.modules.wddx.components;

import java.util.Set;

import ortus.boxlang.modules.wddx.util.WDDXKeys;
import ortus.boxlang.modules.wddx.util.WDDXUtil;
import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.components.Attribute;
import ortus.boxlang.runtime.components.BoxComponent;
import ortus.boxlang.runtime.components.Component;
import ortus.boxlang.runtime.components.Component.BodyResult;
import ortus.boxlang.runtime.components.Component.ComponentBody;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.ExpressionInterpreter;
import ortus.boxlang.runtime.dynamic.casters.StringCaster;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.validation.Validator;

@BoxComponent( allowsBody = false )
public class WDDX extends Component {

	private static final Boolean		isCompatMode	= BoxRuntime.getInstance().getModuleService().getModuleNames().contains( Key.of( "compat-cfml" ) );
	private static final String			languageTag		= isCompatMode ? "cfml" : "bx";
	private static final BoxLangLogger	logger			= BoxRuntime.getInstance().getLoggingService().getRuntimeLogger();

	public static final Key				toWDDXKey		= Key.of( languageTag + "2wddx" );
	public static final Key				toCFMLKey		= Key.of( "wddx2" + languageTag );
	public static final Key				toJSKey			= Key.of( languageTag + "2js" );
	public static final Key				XtoJSKey		= Key.of( "wddx2js" );

	public WDDX() {
		super();
		declaredAttributes = new Attribute[] {
		    new Attribute( Key.action, "string", languageTag + "2wddx",
		        Set.of( Validator.REQUIRED, Validator.valueOneOf( languageTag + "2wddx", "wddx2" + languageTag, languageTag + "2js", "wddx2js" ) ) ),
		    new Attribute( Key.input, "any", Set.of( Validator.REQUIRED ) ),
		    new Attribute( Key.output, "string", Set.of( Validator.REQUIRED ) ),
		    new Attribute( WDDXKeys.toplevelvariable, "string" ),
		    new Attribute( WDDXKeys.usetimezoneinfo, "boolean", true ),
		    // TODO: we warn that these are not supported, for now. Deprecate in a future release
		    new Attribute( WDDXKeys.validate, "boolean", false ),
		    new Attribute( WDDXKeys.xmlconform, "boolean", true ),
		};
	}

	/**
	 * Serializes and de-serializes CFML data structures to the XML-based WDDX format.
	 *
	 * Generates JavaScript statements to instantiate JavaScript objects equivalent to the contents of a WDDX packet or some CFML data structures.
	 *
	 * This tag cannot have a body.
	 *
	 * Note: If the [compatibility module](https://forgebox.io/view/bx-compat) is installed, the use of `bx` in the action attribute changes to `cfml` instead ( e.g. `cfml2wddx` )
	 *
	 * @param context        The context in which the Component is being invoked
	 * @param attributes     The attributes to the Component
	 * @param body           The body of the Component
	 * @param executionState The execution state of the Component
	 *
	 * @attribute.input The input data to be converted
	 *
	 * @attribute.output The variable to which the converted data will be assigned
	 *
	 * @attribute.action The action to be performed on the input data. One of: bx2wddx, wddx2bx, bx2js, wddx2js
	 *
	 * @attribute.toplevelvariable The name of the top-level variable to be used in the generated JavaScript code
	 *
	 * @attribute.usetimezoneinfo Whether to use timezone information in the generated JavaScript code
	 *
	 * @attribute.validate Whether to validate the input XML
	 *
	 * @attribute.xmlconform Whether the WDDX input shoud conform to the WDDX DTD
	 */
	public BodyResult _invoke( IBoxContext context, IStruct attributes, ComponentBody body, IStruct executionState ) {
		Key		actionKey			= Key.of( attributes.get( Key.action ) );
		Object	input				= attributes.get( Key.input );
		String	variable			= attributes.getAsString( Key.output );
		String	toplevelvariable	= attributes.getAsString( WDDXKeys.toplevelvariable );
		if ( toplevelvariable == null ) {
			toplevelvariable = actionKey + "_JS";
		}

		if ( attributes.getAsBoolean( WDDXKeys.validate ) ) {
			logger.warn( "WDDX DTDs are no longer published nor available. Validation cannot not be performed" );
		}

		if ( !attributes.getAsBoolean( WDDXKeys.xmlconform ) ) {
			logger.warn( "The WDDX component only allows valid XML.  All input must be valid xml. The argument `xmlConform` will be ignored." );
		}

		if ( actionKey.equals( toWDDXKey ) ) {
			ExpressionInterpreter.setVariable(
			    context,
			    variable,
			    WDDXUtil.serialize( input )
			);
		} else if ( actionKey.equals( toCFMLKey ) ) {
			ExpressionInterpreter.setVariable(
			    context,
			    variable,
			    WDDXUtil.parse( StringCaster.cast( input ) )
			);
		} else if ( actionKey.equals( toJSKey ) ) {
			ExpressionInterpreter.setVariable(
			    context,
			    variable,
			    WDDXUtil.serializeToJavascript( input, toplevelvariable )
			);
		} else if ( actionKey.equals( XtoJSKey ) ) {
			ExpressionInterpreter.setVariable(
			    context,
			    variable,
			    WDDXUtil.translateToJavascript( StringCaster.cast( input ), toplevelvariable )
			);
		}

		return DEFAULT_RETURN;
	}
}
