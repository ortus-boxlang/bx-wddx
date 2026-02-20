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

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.casters.ArrayCaster;
import ortus.boxlang.runtime.dynamic.casters.BooleanCaster;
import ortus.boxlang.runtime.dynamic.casters.DateTimeCaster;
import ortus.boxlang.runtime.dynamic.casters.GenericCaster;
import ortus.boxlang.runtime.dynamic.casters.IntegerCaster;
import ortus.boxlang.runtime.dynamic.casters.KeyCaster;
import ortus.boxlang.runtime.dynamic.casters.QueryCaster;
import ortus.boxlang.runtime.dynamic.casters.StringCaster;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.DateTime;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Query;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.XML;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;
import ortus.boxlang.runtime.types.util.BLCollector;
import ortus.boxlang.runtime.types.util.StringUtil;

public class WDDXUtil {

	private static BoxRuntime	runtime	= BoxRuntime.getInstance();
	private static IBoxContext	context	= runtime.getRuntimeContext();

	/**
	 * Escapes special XML characters for use in XML attribute values.
	 *
	 * @param value the string to escape
	 *
	 * @return the escaped string safe for use in XML attribute values
	 */
	public static String escapeXmlAttribute( String value ) {
		if ( value == null ) {
			return "";
		}
		return value
		    .replace( "&", "&amp;" )
		    .replace( "<", "&lt;" )
		    .replace( ">", "&gt;" )
		    .replace( "\"", "&quot;" )
		    .replace( "'", "&apos;" );
	}

	/**
	 * Escapes special XML characters for use in XML text content.
	 *
	 * @param value the string to escape
	 *
	 * @return the escaped string safe for use in XML text content
	 */
	public static String escapeXmlText( String value ) {
		if ( value == null ) {
			return "";
		}
		return value
		    .replace( "&", "&amp;" )
		    .replace( "<", "&lt;" )
		    .replace( ">", "&gt;" );
	}

	/**
	 * Parses a wddx packet in to its native BoxLang object
	 *
	 * @param wddx
	 *
	 * @return Object the packet representation
	 */
	public static Object parse( String wddx ) {
		XML xmlObject = new XML( wddx );
		if ( xmlObject.getFirstChildOfName( "wddxPacket" ) == null ) {
			throw new BoxRuntimeException( "The object provided is not a valid WDDX packet" );
		}
		XML data = xmlObject.getFirstChildOfName( "wddxPacket" ).getFirstChildOfName( "data" );
		return deserializeObject( data.getXMLChildrenAsList().get( 0 ) );
	}

	public static Object deserializeObject( XML obj ) {
		switch ( obj.getNode().getNodeName() ) {
			case "recordset" : {
				Array	columnNames	= obj.getXMLChildrenAsList().stream().map( child -> child.getXMLAttributes().get( Key._NAME ) )
				    .collect( BLCollector.toArray() );
				Array	columnTypes	= obj.getXMLChildrenAsList().stream()
				    .map( child -> child.getXMLAttributes().get( Key.type ) != null ? child.getXMLAttributes().get( Key.type ) : "string" )
				    .collect( BLCollector.toArray() );
				Query	result		= Query.fromArray( columnNames, columnTypes, null );
				Array	rowData		= new Array();
				Integer	rowCount	= IntegerCaster.cast( obj.getXMLAttributes().get( Key.of( "rowCount" ) ) );
				IntStream.range( 0, rowCount ).forEach( idx -> {
					IStruct row = new Struct();
					obj.getXMLChildrenAsList().forEach( ( field ) -> {
						String	fieldName	= StringCaster.cast( field.getXMLAttributes().get( Key._NAME ) );
						Object	fieldValue	= deserializeObject( field.getXMLChildrenAsList().get( idx ) );
						row.put( Key.of( fieldName ), fieldValue );
					} );
					rowData.add( row );
				} );
				result.addData( rowData );
				return result;
			}
			case "struct" : {
				IStruct structResult = new Struct();
				obj.getXMLChildrenAsList().forEach( ( child ) -> {
					structResult.put( KeyCaster.cast( child.getXMLAttributes().get( Key._NAME ) ), deserializeObject( child.getXMLChildrenAsList().get( 0 ) ) );
				} );
				return structResult;
			}
			case "array" : {
				return obj.getXMLChildrenAsList().stream().map( WDDXUtil::deserializeObject ).collect( BLCollector.toArray() );
			}
			case "boolean" : {
				return BooleanCaster.cast( obj.getXMLAttributes().get( Key.value ) );
			}
			default : {
				return GenericCaster.cast( context, obj.getXMLText(), obj.getNode().getNodeName() );
			}
		}
	}

	/**
	 * Serializes an object to a WDDX packet
	 *
	 * @param obj the object to serialize
	 *
	 * @return String the WDDX xml
	 */
	public static String serialize( Object obj ) {
		String wddx = "<wddxPacket version=\"1.0\"><header/><data>";
		wddx	+= serializeObject( obj );
		wddx	+= "</data></wddxPacket>";
		return wddx;
	}

	/**
	 * Serialize an object to WDDX
	 *
	 * @param obj The object to serialize
	 *
	 * @return The WDDX representation of the object
	 */
	public static String serializeObject( Object obj ) {
		if ( obj instanceof Query ) {
			return serializeQuery( QueryCaster.cast( obj ) );
		}
		// Booleans have a different pattern, in that they do not have an outer wrapper
		if ( obj instanceof Boolean ) {
			return "<boolean value=\"" + obj.toString() + "\"/>";
		}

		Key		classKey		= Key.of( StringUtil.lcFirst( obj.getClass().getSimpleName() ) );
		String	serialization	= "<" + classKey.getName() + ( obj instanceof Array ? " length=\"" + ArrayCaster.cast( obj ).size() + "\"" : "" ) + ">";
		if ( obj instanceof IStruct ) {
			IStruct struct = ( IStruct ) obj;
			serialization += struct.entrySet().stream().map( ( entry ) -> {
				return "<var name=\"" + escapeXmlAttribute( entry.getKey().toString() ) + "\">" + serializeObject( entry.getValue() ) + "</var>";
			} ).collect( Collectors.joining() );
		} else if ( obj instanceof Array ) {
			serialization += ArrayCaster.cast( obj ).stream().map( WDDXUtil::serializeObject ).collect( Collectors.joining() );
		} else if ( obj instanceof DateTime ) {
			serialization += DateTimeCaster.cast( obj ).toISOString();
		} else {
			serialization += escapeXmlText( obj.toString() );
		}

		serialization += "</" + classKey.getName() + ">";

		return serialization;

	}

	/**
	 * Serializes a query object to its WDDX representation
	 *
	 * @param obj the Query object to be serialized
	 *
	 * @return the WDDX recordset representation of the query
	 */
	public static String serializeQuery( Query obj ) {
		Key		classKey		= Key.of( "recordset" );
		String	serialization	= "<"
		    + classKey.getName()
		    + " rowCount=\"" + obj.getData().size() + "\""
		    + " fieldNames=\"" + obj.getColumnList() + "\""
		    + " type=\"" + obj.getClass().getName() + "\""
		    + ">";

		serialization	+= obj.getColumnArray().stream().map( ( column ) -> {

							String field = "<field name=\"" + escapeXmlAttribute( column.toString() ) + "\">";

							field	+= Stream.of( obj.getColumnData( Key.of( column ) ) )
							    .map( WDDXUtil::serializeObject )
							    .collect( Collectors.joining() );

							field	+= "</field>";
							return field;

						} )
		    .collect( Collectors.joining() );

		serialization	+= "</" + classKey.getName() + ">";

		return serialization;
	}

	/**
	 *
	 * Serializes an object to a Javascript variable string
	 *
	 * @param obj
	 * @param variableName
	 *
	 * @return
	 */
	public static String serializeToJavascript( Object obj, String variableName ) {
		Key		serializeKey	= Key.of( "JSONSerialize" );
		IStruct	serializeArgs	= Struct.of(
		    Key.data, obj,
		    Key.queryFormat, "row",
		    Key.useSecureJSONPrefix, false,
		    Key.useCustomSerializer, false
		);

		return variableName + " = "
		    + StringCaster.cast( runtime.getFunctionService().getGlobalFunction( serializeKey ).invoke( context, serializeArgs, false, serializeKey ) ) + ";";
	}

	/**
	 *
	 * Translates a WDDX packet to a Javascript variable string
	 *
	 * @param wddx
	 * @param variableName
	 *
	 * @return
	 */
	public static String translateToJavascript( String wddx, String variableName ) {
		Object obj = parse( wddx );
		return serializeToJavascript( obj, variableName );
	}

}
