package ortus.boxlang.modules.wddx.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.compiler.parser.BoxSourceType;
import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.dynamic.casters.QueryCaster;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;

public class WDDXTest {

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
	public void testBX2WDDXCF() {
		instance.executeSource( """
		                        <cfset qry = queryNew("id,test","integer,varchar",[{id:1,test:"test"},{id:2,test:"name"}])>
		                        <cfwddx action="bx2wddx" input="#qry#" output="result">
		                                          """, context, BoxSourceType.CFTEMPLATE );
		assertEquals(
		    variables.getAsString( result ),
		    "<wddxPacket version=\"1.0\"><header/><data><recordset rowCount=\"2\" fieldNames=\"id,test\" type=\"ortus.boxlang.runtime.types.Query\"><field name=\"id\"><integer>1</integer><integer>2</integer></field><field name=\"test\"><string>test</string><string>name</string></field></recordset></data></wddxPacket>"
		);
	}

	@DisplayName( "It tests BX2WDDX in BX Template" )
	@Test
	public void testBX2WDDXBX() {
		instance.executeSource( """
		                        <bx:set qry = queryNew("id,test","integer,varchar",[{id:1,test:"test"},{id:2,test:"name"}])>
		                        <bx:wddx action="bx2wddx" input="#qry#" output="result">
		                                          """, context, BoxSourceType.BOXTEMPLATE );
		assertEquals(
		    variables.getAsString( result ),
		    "<wddxPacket version=\"1.0\"><header/><data><recordset rowCount=\"2\" fieldNames=\"id,test\" type=\"ortus.boxlang.runtime.types.Query\"><field name=\"id\"><integer>1</integer><integer>2</integer></field><field name=\"test\"><string>test</string><string>name</string></field></recordset></data></wddxPacket>"
		);
	}

	@DisplayName( "It tests BX2WDDX in BX Script" )
	@Test
	public void testBX2WDDXBXS() {
		// @formatter:off
		instance.executeSource( """
			qry = queryNew("id,test","integer,varchar",[{id:1,test:"test"},{id:2,test:"name"}]);
			bx:wddx action="bx2wddx" input=qry output="result";
		""", context, BoxSourceType.BOXSCRIPT );
		// @formatter:on
		assertEquals(
		    variables.getAsString( result ),
		    "<wddxPacket version=\"1.0\"><header/><data><recordset rowCount=\"2\" fieldNames=\"id,test\" type=\"ortus.boxlang.runtime.types.Query\"><field name=\"id\"><integer>1</integer><integer>2</integer></field><field name=\"test\"><string>test</string><string>name</string></field></recordset></data></wddxPacket>"
		);
	}

	@DisplayName( "It tests WDDX2BX in BX Script" )
	@Test
	public void testWDDX2BX() {
		variables.put( Key.of( "packet" ),
		    "<wddxPacket version=\"1.0\"><header/><data><recordset rowCount=\"2\" fieldNames=\"id,test\" type=\"ortus.boxlang.runtime.types.Query\"><field name=\"id\"><integer>1</integer><integer>2</integer></field><field name=\"test\"><string>test</string><string>name</string></field></recordset></data></wddxPacket>" );
		// @formatter:off
		instance.executeSource( """
			bx:wddx action="wddx2bx" input=packet output="result";
		""", context, BoxSourceType.BOXSCRIPT );
		// @formatter:on
		assertTrue( variables.get( result ) instanceof ortus.boxlang.runtime.types.Query );
		assertEquals( 2, QueryCaster.cast( variables.get( result ) ).getData().size() );

	}

	@DisplayName( "It tests WDDX2JS in BX Script" )
	@Test
	public void testWDDX2JS() {
		variables.put( Key.of( "packet" ),
		    "<wddxPacket version=\"1.0\"><header/><data><recordset rowCount=\"2\" fieldNames=\"id,test\" type=\"ortus.boxlang.runtime.types.Query\"><field name=\"id\"><integer>1</integer><integer>2</integer></field><field name=\"test\"><string>test</string><string>name</string></field></recordset></data></wddxPacket>" );
		// @formatter:off
		instance.executeSource( """
		    bx:wddx action="wddx2js" input=packet output="result" topLevelVariable="myData";
		""", context, BoxSourceType.BOXSCRIPT );

		// @formatter:on
		assertTrue( variables.get( result ) instanceof String );
		assertTrue( variables.getAsString( result ).contains( "myData" ) );
		assertTrue( variables.getAsString( result ).contains( "\"columns\"" ) );
		assertTrue( variables.getAsString( result ).contains( "\"data\"" ) );

	}

	@DisplayName( "It tests CFML2JS in BX Script" )
	@Test
	public void testCFMLToJS() {
		// @formatter:off
		instance.executeSource( """
			qry = queryNew("id,test","integer,varchar",[{id:1,test:"test"},{id:2,test:"name"}]);
			bx:wddx action="bx2js" input=qry output="result" topLevelVariable="myData";
		""", context, BoxSourceType.BOXSCRIPT );

		// @formatter:on
		System.out.println( variables.getAsString( result ) );
		assertTrue( variables.get( result ) instanceof String );
		assertTrue( variables.getAsString( result ).contains( "myData" ) );
		assertTrue( variables.getAsString( result ).contains( "\"columns\"" ) );
		assertTrue( variables.getAsString( result ).contains( "\"data\"" ) );

	}

}
