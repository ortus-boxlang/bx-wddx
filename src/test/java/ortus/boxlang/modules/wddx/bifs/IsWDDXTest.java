package ortus.boxlang.modules.wddx.bifs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;

public class IsWDDXTest {

	static BoxRuntime	instance;
	IBoxContext			context;
	IScope				variables;
	static Key			result	= new Key( "result" );

	@BeforeAll
	public static void setUp() {
		instance = BoxRuntime.getInstance( true );
	}

	@BeforeEach
	public void setupEach() {
		context		= new ScriptingRequestBoxContext( instance.getRuntimeContext() );
		variables	= context.getScopeNearby( VariablesScope.name );
	}

	@DisplayName( "It tests BX2WDDX in CF Template" )
	@Test
	public void testIsWDDX() {
		//@formatter:off
		instance.executeSource( """
		resultString = isWddx( "foo" );
		resultWddxString = isWddx( "<wddxPacket version=""1.0""><header/><data></data></wddxPacket>" );
		resultWddxXML = isWddx( xmlParse( "<wddxPacket version=""1.0""><header/><data></data></wddxPacket>" ) );
		resultWddxDate = isWddx( now() );
		resultWddxQuery = isWddx( queryNew( "id,test","integer,varchar",[{id:1,test:"test"},{id:2,test:"name"}]) );
		resultWddxXMLIsh = isWddx( xmlParse( "<root><foo>bar</foo></root>" ) );
		""", context );
		//@formatter:on
		assertFalse( variables.getAsBoolean( Key.of( "resultString" ) ) );
		assertTrue( variables.getAsBoolean( Key.of( "resultWddxString" ) ) );
		assertTrue( variables.getAsBoolean( Key.of( "resultWddxXML" ) ) );
		assertFalse( variables.getAsBoolean( Key.of( "resultWddxDate" ) ) );
		assertFalse( variables.getAsBoolean( Key.of( "resultWddxQuery" ) ) );
		assertFalse( variables.getAsBoolean( Key.of( "resultWddxXMLIsh" ) ) );
	}

}
