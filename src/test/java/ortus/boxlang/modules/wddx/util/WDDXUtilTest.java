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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.dynamic.casters.StructCaster;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.DateTime;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Query;
import ortus.boxlang.runtime.types.QueryColumnType;
import ortus.boxlang.runtime.types.Struct;

public class WDDXUtilTest {

	@DisplayName( "Test struct serialization" )
	@Test
	void testSerializeStruct() {
		IStruct	test	= Struct.of(
		    "foo", "bar",
		    "isWDDX", true
		);
		String	wddx	= WDDXUtil.serializeObject( test );
		assertEquals( "<struct><var name=\"isWDDX\"><boolean value=\"true\"/></var><var name=\"foo\"><string>bar</string></var></struct>", wddx );
	}

	@DisplayName( "Test array serialization" )
	@Test
	void testSerializeArray() {
		Array	test	= Array.of( "foo", "bar" );
		String	wddx	= WDDXUtil.serializeObject( test );
		assertEquals( "<array length=\"2\"><string>foo</string><string>bar</string></array>", wddx );
	}

	@DisplayName( "Test query serialization" )
	@Test
	void testSerializeQuery() {
		Query qry = new Query();
		qry.addColumn( Key.of( "foo" ), QueryColumnType.VARCHAR );
		qry.addRow( new Object[] { "bar" } );
		qry.addRow( Struct.of( Key.of( "foo" ), "brad" ) );
		qry.addColumn( Key.of( "col2" ), QueryColumnType.INTEGER );
		qry.addRow( Struct.of( Key.of( "foo" ), "luis", "col2", 42 ) );
		qry.setCell( Key.of( "col2" ), 0, 100 );
		qry.setCell( Key.of( "col2" ), 1, 101 );
		String wddx = WDDXUtil.serializeObject( qry );
		assertEquals(
		    "<recordset rowCount=\"3\" fieldNames=\"foo,col2\" type=\"ortus.boxlang.runtime.types.Query\"><field name=\"foo\"><string>bar</string><string>brad</string><string>luis</string></field><field name=\"col2\"><integer>100</integer><integer>101</integer><integer>42</integer></field></recordset>",
		    wddx );
	}

	@DisplayName( "Test datetime serialization" )
	@Test
	void testSerializeDateTime() {
		DateTime	test	= new DateTime( 2024, 1, 1, ZoneId.of( "UTC" ) );
		String		wddx	= WDDXUtil.serializeObject( test );
		assertEquals( "<dateTime>2024-01-01T00:00:00Z</dateTime>", wddx );
	}

	@DisplayName( "Test wddx parsing" )
	@Test
	void testParse() {
		String	wddx	= """
		                  <wddxPacket version=\"1.0\">
		                  	<header/>
		                  	<data>
		                  		<struct>
		                  			<var name=\"flea\">
		                  				<array length=\"4\">
		                  					<string>foo</string>
		                  					<string>bar</string>
		                  					<string>baz</string>
		                  					<dateTime>2024-06-02T16:31:57Z</dateTime>
		                  				</array>
		                  			</var>
		                  			<var name=\"flah\">
		                  				<struct>
		                  					<var name=\"bar\">
		                  						<string>bazz</string>
		                  					</var>
		                  				</struct>
		                  			</var>
		                  			<var name=\"foo\">
		                  				<string>bar</string>
		                  			</var>
		                  			<var name=\"isWDDX\">
		                  				<boolean value=\"true\"/>
		                  			</var>
		                  		</struct>
		                  	</data>
		                  </wddxPacket>
		                  """.trim();
		Object	result	= WDDXUtil.parse( wddx );
		assertTrue( result instanceof IStruct );
		IStruct deserialized = StructCaster.cast( result );
		assertTrue( deserialized.containsKey( Key.of( "flea" ) ) );
		assertTrue( deserialized.get( Key.of( "flea" ) ) instanceof Array );
		assertTrue( deserialized.containsKey( Key.of( "flah" ) ) );
		assertTrue( deserialized.get( Key.of( "flah" ) ) instanceof IStruct );
		assertTrue( deserialized.containsKey( Key.of( "foo" ) ) );
		assertTrue( deserialized.get( Key.of( "foo" ) ) instanceof String );
		assertTrue( deserialized.containsKey( Key.of( "isWDDX" ) ) );
		assertTrue( deserialized.get( Key.of( "isWDDX" ) ) instanceof Boolean );
	}

}