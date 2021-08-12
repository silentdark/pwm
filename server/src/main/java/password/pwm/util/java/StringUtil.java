/*
 * Password Management Servlets (PWM)
 * http://www.pwm-project.org
 *
 * Copyright (c) 2006-2009 Novell, Inc.
 * Copyright (c) 2009-2021 The PWM Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package password.pwm.util.java;

import net.iharder.Base64;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import password.pwm.PwmConstants;
import password.pwm.error.PwmError;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.util.logging.PwmLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class StringUtil
{
    private static final PwmLogger LOGGER = PwmLogger.forClass( StringUtil.class );

    /**
     * Based on http://www.owasp.org/index.php/Preventing_LDAP_Injection_in_Java.
     *
     * @param input string to have escaped
     * @return ldap escaped script
     */
    public static String escapeLdapFilter( final String input )
    {
        final StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < input.length(); i++ )
        {
            final char curChar = input.charAt( i );
            switch ( curChar )
            {
                case '\\':
                    sb.append( "\\5c" );
                    break;
                case '*':
                    sb.append( "\\2a" );
                    break;
                case '(':
                    sb.append( "\\28" );
                    break;
                case ')':
                    sb.append( "\\29" );
                    break;
                case '\u0000':
                    sb.append( "\\00" );
                    break;
                default:
                    sb.append( curChar );
            }
        }
        return sb.toString();
    }

    /**
     * Based on http://www.owasp.org/index.php/Preventing_LDAP_Injection_in_Java.
     *
     * @param input string to have escaped
     * @return ldap escaped script
     */
    public static String escapeLdapDN( final String input )
    {
        final StringBuilder sb = new StringBuilder();
        if ( ( input.length() > 0 ) && ( ( input.charAt( 0 ) == ' ' ) || ( input.charAt( 0 ) == '#' ) ) )
        {
            // add the leading backslash if needed
            sb.append( '\\' );
        }
        for ( int i = 0; i < input.length(); i++ )
        {
            final char curChar = input.charAt( i );
            switch ( curChar )
            {
                case '\\':
                    sb.append( "\\\\" );
                    break;
                case ',':
                    sb.append( "\\," );
                    break;
                case '+':
                    sb.append( "\\+" );
                    break;
                case '"':
                    sb.append( "\\\"" );
                    break;
                case '<':
                    sb.append( "\\<" );
                    break;
                case '>':
                    sb.append( "\\>" );
                    break;
                case ';':
                    sb.append( "\\;" );
                    break;
                default:
                    sb.append( curChar );
            }
        }
        if ( ( input.length() > 1 ) && ( input.charAt( input.length() - 1 ) == ' ' ) )
        {
            // add the trailing backslash if needed
            sb.insert( sb.length() - 1, '\\' );
        }
        return sb.toString();
    }

    public static Map<String, String> convertStringListToNameValuePair( final Collection<String> input, final String separator )
    {
        if ( input == null || input.isEmpty() )
        {
            return Collections.emptyMap();
        }

        final Map<String, String> returnMap = new LinkedHashMap<>();
        for ( final String loopStr : input )
        {
            if ( loopStr != null && separator != null && loopStr.contains( separator ) )
            {
                final int separatorLocation = loopStr.indexOf( separator );
                final String key = loopStr.substring( 0, separatorLocation );
                if ( !key.trim().isEmpty() )
                {
                    final String value = loopStr.substring( separatorLocation + separator.length() );
                    returnMap.put( key, value );
                }
            }
            else
            {
                if ( loopStr != null && !loopStr.trim().isEmpty() )
                {
                    returnMap.put( loopStr, "" );
                }
            }
        }

        return returnMap;
    }

    public static String join( final Object[] inputs, final String separator )
    {
        return StringUtils.join( inputs, separator );
    }

    public static String join( final Collection inputs, final String separator )
    {
        if ( inputs != null )
        {
            return StringUtils.join( inputs, separator );
        }
        return "";
    }

    public static String formatDiskSizeforDebug( final long diskSize )
    {
        return diskSize == 0
                ? "0"
                : PwmNumberFormat.forDefaultLocale().format( diskSize ) + " (" + formatDiskSize( diskSize ) + ")";
    }

    public static String formatDiskSize( final long diskSize )
    {
        final float count = 1000;
        if ( diskSize < 0 )
        {
            return "n/a";
        }

        if ( diskSize == 0 )
        {
            return "0";
        }

        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits( 2 );

        if ( diskSize > count * count * count )
        {
            final StringBuilder sb = new StringBuilder();
            sb.append( nf.format( diskSize / count / count / count ) );
            sb.append( " GB" );
            return sb.toString();
        }

        if ( diskSize > count * count )
        {
            final StringBuilder sb = new StringBuilder();
            sb.append( nf.format( diskSize / count / count ) );
            sb.append( " MB" );
            return sb.toString();
        }

        return PwmNumberFormat.forDefaultLocale().format( diskSize ) + " bytes";
    }

    public static boolean nullSafeEqualsIgnoreCase( final String value1, final String value2 )
    {
        final String compare1 = value1 == null ? "" : value1;
        final String compare2 = value2 == null ? "" : value2;
        return compare1.equalsIgnoreCase( compare2 );
    }

    public static boolean nullSafeEquals( final String value1, final String value2 )
    {
        return Objects.equals( value1, value2 );
    }

    public enum Base64Options
    {
        GZIP,
        URL_SAFE,;

        private static int asBase64UtilOptions( final Base64Options... options )
        {
            int b64UtilOptions = 0;

            if ( JavaHelper.enumArrayContainsValue( options, Base64Options.GZIP ) )
            {
                b64UtilOptions = b64UtilOptions | Base64.GZIP;
            }
            if ( JavaHelper.enumArrayContainsValue( options, Base64Options.URL_SAFE ) )
            {
                b64UtilOptions = b64UtilOptions | Base64.URL_SAFE;
            }
            return b64UtilOptions;
        }
    }

    public static String escapeJS( final String input )
    {
        return StringEscapeUtils.escapeEcmaScript( input );
    }

    public static String escapeHtml( final String input )
    {
        return StringEscapeUtils.escapeHtml4( input );
    }

    public static String escapeCsv( final String input )
    {
        return StringEscapeUtils.escapeCsv( input );
    }

    public static String escapeJava( final String input )
    {
        return StringEscapeUtils.escapeJava( input );
    }

    public static String escapeXml( final String input )
    {
        return StringEscapeUtils.escapeXml11( input );
    }

    public static String urlEncode( final String input )
    {
        try
        {
            return URLEncoder.encode( input, PwmConstants.DEFAULT_CHARSET.toString() );
        }
        catch ( final UnsupportedEncodingException e )
        {
            LOGGER.error( () -> "unexpected error during url encoding: " + e.getMessage() );
            return input;
        }
    }

    public static String urlDecode( final String input )
    {
        try
        {
            return URLDecoder.decode( input, PwmConstants.DEFAULT_CHARSET.toString() );
        }
        catch ( final UnsupportedEncodingException e )
        {
            LOGGER.error( () -> "unexpected error during url decoding: " + e.getMessage() );
            return input;
        }
    }

    public static byte[] base64Decode( final String input )
            throws IOException
    {
        return Base64.decode( input );
    }

    public static String base32Encode( final byte[] input )
            throws IOException
    {
        final Base32 base32 = new Base32();
        return new String( base32.encode( input ), PwmConstants.DEFAULT_CHARSET );
    }

    public static byte[] base64Decode( final String input, final StringUtil.Base64Options... options )
            throws IOException
    {
        final int b64UtilOptions = Base64Options.asBase64UtilOptions( options );

        return Base64.decode( input, b64UtilOptions );
    }

    public static String base64Encode( final byte[] input )
    {
        return Base64.encodeBytes( input );
    }

    public static String base64Encode( final byte[] input, final StringUtil.Base64Options... options )
            throws IOException
    {
        final int b64UtilOptions = Base64Options.asBase64UtilOptions( options );

        if ( b64UtilOptions > 0 )
        {
            return Base64.encodeBytes( input, b64UtilOptions );
        }
        else
        {
            return Base64.encodeBytes( input );
        }
    }

    public static String padRight( final String input, final int length, final char appendChar )
    {
        return padImpl( input, length, appendChar, true );
    }

    public static String padLeft( final String input, final int length, final char appendChar )
    {
        return padImpl( input, length, appendChar, false );
    }

    private static String padImpl( final String input, final int length, final char appendChar, final boolean right )
    {
        if ( input == null )
        {
            return null;
        }

        if ( input.length() >= length )
        {
            return input;
        }

        final StringBuilder sb = new StringBuilder( input );
        while ( sb.length() < length )
        {
            if ( right )
            {
                sb.append( appendChar );
            }
            else
            {
                sb.insert( 0, appendChar );
            }
        }

        return sb.toString();
    }

    public static List<String> splitAndTrim( final String input, final String seperator )
    {
        if ( StringUtil.isEmpty( input ) )
        {
            return Collections.emptyList();
        }

        final String[] splitValues = StringUtils.split( input, seperator );

        return Arrays.stream( splitValues )
                .map( String::trim )
                .collect( Collectors.toList() );
    }

    public static Collection<String> whitespaceSplit( final String input )
    {
        if ( input == null )
        {
            return Collections.emptyList();
        }

        final String[] splitValues = input.trim().split( "\\s+" );
        return Arrays.asList( splitValues );
    }

    public static String[] createStringChunks( final String str, final int size )
    {
        if ( size <= 0 || str == null || str.length() <= size )
        {
            return new String[]
                    {
                            str,
                    };
        }

        final int numOfChunks = str.length() - size + 1;
        final Set<String> chunks = new HashSet<>( numOfChunks );

        for ( int i = 0; i < numOfChunks; i++ )
        {
            chunks.add( StringUtils.substring( str, i, i + size ) );
        }

        return chunks.toArray( new String[ numOfChunks ] );
    }

    public static String collectionToString( final Collection collection )
    {
        return collectionToString( collection, "," );
    }

    public static String collectionToString( final Collection collection, final String recordSeparator )
    {
        final StringBuilder sb = new StringBuilder();
        if ( collection != null )
        {
            for ( final Iterator iterator = collection.iterator(); iterator.hasNext(); )
            {
                final Object obj = iterator.next();
                if ( obj != null )
                {
                    sb.append( obj.toString() );
                    if ( iterator.hasNext() )
                    {
                        sb.append( recordSeparator );
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String mapToString( final Map map )
    {
        return mapToString( map, "=", "," );
    }

    public static String mapToString( final Map map, final String keyValueSeparator, final String recordSeparator )
    {
        final StringBuilder sb = new StringBuilder();
        for ( final Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); )
        {
            final Map.Entry entrySet = ( Map.Entry ) iterator.next();
            final String key = entrySet.getKey().toString();
            final String value = entrySet.getValue() == null ? "" : entrySet.getValue().toString();

            if ( key != null && value != null && !key.trim().isEmpty() && !value.trim().isEmpty() )
            {
                sb.append( key.trim() );
                sb.append( keyValueSeparator );
                sb.append( value.trim() );
                if ( iterator.hasNext() )
                {
                    sb.append( recordSeparator );
                }
            }
        }
        return sb.toString();
    }

    public static int[] toCodePointArray( final String str )
    {
        if ( str != null )
        {
            final int len = str.length();
            final int[] acp = new int[ str.codePointCount( 0, len ) ];

            for ( int i = 0, j = 0; i < len; i = str.offsetByCodePoints( i, 1 ) )
            {
                acp[ j++ ] = str.codePointAt( i );
            }

            return acp;
        }

        return new int[ 0 ];
    }

    public static boolean isEmpty( final CharSequence input )
    {
        return StringUtils.isEmpty( input );
    }

    public static boolean isTrimEmpty( final String input )
    {
        return isEmpty( input ) || input.trim().length() == 0;
    }

    public static String defaultString( final String input, final String defaultStr )
    {
        return StringUtils.defaultString( input, defaultStr );
    }

    public static boolean equals( final String input1, final String input2 )
    {
        return StringUtils.equals( input1, input2 );
    }

    public static String truncate( final String input, final int length )
    {
        return truncate( input, length, null );
    }

    public static String truncate( final String input, final int length, final String appendIfTruncated )
    {
        if ( input == null )
        {
            return "";
        }

        if ( input.length() > length )
        {
            return input.substring( 0, length ) + ( appendIfTruncated == null ? "" : appendIfTruncated );
        }

        return input;
    }

    public static int convertStrToInt( final String string, final int defaultValue )
    {
        if ( string == null )
        {
            return defaultValue;
        }

        try
        {
            return Integer.parseInt( string );
        }
        catch ( final NumberFormatException e )
        {
            return defaultValue;
        }
    }

    public static String stripAllWhitespace( final String input )
    {
        return stripAllChars( input, Character::isWhitespace );
    }

    public static String stripAllChars( final String input, final Predicate<Character> characterPredicate )
    {
        if ( isEmpty( input ) )
        {
            return "";
        }

        if ( characterPredicate == null )
        {
            return input;
        }

        // count of valid output chars
        int copiedChars = 0;

        // loop through input chars and stop if stripped char is found
        while ( copiedChars < input.length() )
        {
            if ( !characterPredicate.test( input.charAt( copiedChars ) ) )
            {
                copiedChars++;
            }
            else
            {
                break;
            }
        }

        // return input string if we made it through input without detecting stripped char
        if ( copiedChars >= input.length() )
        {
            return input;
        }

        // creating sb with input gives good length value and handles copy of chars so far...
        final StringBuilder sb = new StringBuilder( input );

        // loop through remaining chars and copy one by one
        for ( int loopIndex = copiedChars; loopIndex < input.length(); loopIndex++ )
        {
            final char loopChar = input.charAt( loopIndex );
            if ( !characterPredicate.test( loopChar ) )
            {
                sb.setCharAt( copiedChars, loopChar );
                copiedChars++;
            }
        }

        return sb.substring( 0, copiedChars );
    }

    public static String replaceAllChars( final String input, final Function<Character, Optional<String>> replacementFunction )
    {
        if ( isEmpty( input ) )
        {
            return "";
        }

        if ( replacementFunction == null )
        {
            return input;
        }

        // count of valid output chars
        int copiedChars = 0;

        // loop through input chars and stop if replacement char is needed
        while ( copiedChars < input.length() )
        {
            final Character indexChar = input.charAt( copiedChars );
            final Optional<String> replacementStr = replacementFunction.apply( indexChar );
            if ( replacementStr.isEmpty() )
            {
                copiedChars++;
            }
            else
            {
                break;
            }
        }

        // return input string if we made it through input without detecting replacement char
        if ( copiedChars >= input.length() )
        {
            return input;
        }

        final StringBuilder sb = new StringBuilder( input.substring( 0, copiedChars ) );

        // loop through remaining chars and copy one by one
        for ( int loopIndex = copiedChars; loopIndex < input.length(); loopIndex++ )
        {
            final char loopChar = input.charAt( loopIndex );
            final Optional<String> replacementStr = replacementFunction.apply( loopChar );
            if ( replacementStr.isPresent() )
            {
                sb.append( replacementStr.get() );
            }
            else
            {
                sb.append( loopChar );
            }
        }

        return sb.toString();
    }

    public static String insertRepeatedLineBreaks( final String input, final int periodicity )
    {
        final String lineSeparator = System.lineSeparator();
        return repeatedInsert( input, periodicity, lineSeparator );
    }

    public static String repeatedInsert( final String input, final int periodicity, final String insertValue )
    {
        if ( StringUtil.isEmpty( input ) )
        {
            return "";
        }

        if ( StringUtil.isEmpty( insertValue ) )
        {
            return input;
        }

        final int inputLength = input.length();
        final StringBuilder output = new StringBuilder( inputLength + ( periodicity * insertValue.length() ) );

        int index = 0;
        while ( index < inputLength )
        {
            final int endIndex = Math.min( index + periodicity, inputLength );
            output.append( input, index, endIndex );
            output.append( insertValue );
            index += periodicity;
        }
        return output.toString();
    }

    public static boolean caseIgnoreContains( final Collection<String> collection, final String value )
    {
        if ( value == null || collection == null )
        {
            return false;
        }

        if ( collection.contains( value ) )
        {
            return true;
        }

        final String lcaseValue = value.toLowerCase();
        for ( final String item : collection )
        {
            if ( item != null )
            {
                final String lcaseItem = item.toLowerCase();
                if ( lcaseItem.equalsIgnoreCase( lcaseValue ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static void validateLdapSearchFilter( final String filter )
            throws PwmUnrecoverableException
    {
        if ( filter == null || filter.isEmpty() )
        {
            return;
        }

        final int leftParens = StringUtils.countMatches( filter, "(" );
        final int rightParens = StringUtils.countMatches( filter, ")" );

        if ( leftParens != rightParens )
        {
            throw PwmUnrecoverableException.newException( PwmError.CONFIG_FORMAT_ERROR, "unbalanced parentheses in ldap filter" );
        }
    }

    public static InputStream stringToInputStream( final String input )
    {
        return new ByteArrayInputStream( input.getBytes( PwmConstants.DEFAULT_CHARSET ) );
    }
}
