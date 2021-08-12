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

import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public interface XmlElement
{
    Optional<XmlElement> getChild( String elementName );

    String getAttributeValue( String attribute );

    List<XmlElement> getChildren( String elementName );

    String getText();

    String getTextTrim();

    String getChildText( String elementName );

    String getName();

    void setAttribute( String name, String value );

    void detach();

    void removeContent();

    void removeAttribute( String attributeName );

    void addContent( XmlElement element );

    void addContent( List<XmlElement> elements );

    void addText( String text );

    void setComment( List<String> textLines );

    List<XmlElement> getChildren();

    XmlElement copy();

    XmlElement parent();

    class XmlElementJDOM implements XmlElement
    {
        private final Element element;

        XmlElementJDOM( final Element element )
        {
            this.element = element;
        }

        @Override
        public String getName()
        {
            return element.getName();
        }

        @Override
        public Optional<XmlElement> getChild( final String elementName )
        {
            final List<XmlElement> children = getChildren( elementName );
            if ( JavaHelper.isEmpty( children ) )
            {
                return Optional.empty();
            }
            return Optional.of( children.iterator().next() );
        }

        @Override
        public String getAttributeValue( final String attribute )
        {
            return element.getAttributeValue( attribute );
        }

        @Override
        public List<XmlElement> getChildren()
        {
            return getChildren( null );
        }

        @Override
        public List<XmlElement> getChildren( final String elementName )
        {

            final List<Element> children = elementName == null
                    ? element.getChildren()
                    : element.getChildren( elementName );
            if ( children == null )
            {
                return Collections.emptyList();
            }
            final List<XmlElement> xmlElements = new ArrayList<>();
            for ( final Element element : children )
            {
                xmlElements.add( new XmlElementJDOM( element ) );
            }
            return xmlElements;
        }

        @Override
        public String getText()
        {
            return element.getText();
        }

        @Override
        public String getTextTrim()
        {
            return element.getTextTrim();
        }

        @Override
        public String getChildText( final String elementName )
        {
            final Optional<XmlElement> child = getChild( elementName );
            return child.map( XmlElement::getText ).orElse( null );
        }

        @Override
        public void setAttribute( final String name, final String value )
        {
            element.setAttribute( name, value );
        }

        @Override
        public void detach()
        {
            element.detach();
        }

        @Override
        public void removeContent()
        {
            element.removeContent();
        }

        @Override
        public void removeAttribute( final String attributeName )
        {
            element.removeAttribute( attributeName );
        }

        @Override
        public void addContent( final XmlElement element )
        {
            this.element.addContent( ( ( XmlElementJDOM) element ).element );
        }

        @Override
        public void addContent( final List<XmlElement> elements )
        {
            for ( final XmlElement loopElement : elements )
            {
                final Element jdomElement = ( ( XmlElementJDOM ) loopElement ).element;
                this.element.addContent( jdomElement );
            }
        }

        @Override
        public void addText( final String text )
        {
            element.addContent( new Text( text ) );
        }

        @Override
        public void setComment( final List<String> textLines )
        {
            final List<Content> contentList = new ArrayList<>( element.getContent() );
            for ( final Content content : contentList )
            {
                if ( content instanceof Comment )
                {
                    content.detach();
                }
            }

            final List<String> reversedList = new ArrayList<>( textLines );
            Collections.reverse( reversedList );
            for ( final String text : textLines )
            {
                element.addContent( 0, new Comment( text ) );
            }
        }

        @Override
        public XmlElement copy()
        {
            return new XmlElementJDOM( this.element.clone() );
        }

        @Override
        public XmlElement parent()
        {
            return new XmlElementJDOM( this.element.getParentElement() );
        }
    }

    class XmlElementW3c implements XmlElement
    {
        private final org.w3c.dom.Element element;
        private volatile Lock lock;

        XmlElementW3c( final org.w3c.dom.Element element, final Lock lock )
        {
            this.element = element;
            this.lock = lock;
        }

        @Override
        public String getName()
        {
            lock.lock();
            try
            {
                return element.getTagName();
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public Optional<XmlElement> getChild( final String elementName )
        {
            final List<XmlElement> children = getChildren( elementName );
            if ( JavaHelper.isEmpty( children ) )
            {
                return Optional.empty();
            }
            return Optional.of( children.iterator().next() );
        }

        @Override
        public String getAttributeValue( final String attribute )
        {
            lock.lock();
            try
            {
                final String attrValue = element.getAttribute( attribute );
                return StringUtil.isEmpty( attrValue ) ? null : attrValue;
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public List<XmlElement> getChildren()
        {
            lock.lock();
            try
            {
                final NodeList nodeList = element.getChildNodes();
                return XmlFactory.XmlFactoryW3c.nodeListToElementList( nodeList, lock );
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public List<XmlElement> getChildren( final String elementName )
        {
            lock.lock();
            try
            {
                final NodeList nodeList = element.getElementsByTagName( elementName );
                return XmlFactory.XmlFactoryW3c.nodeListToElementList( nodeList, lock );
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public String getText()
        {
            lock.lock();
            try
            {
                final String value = element.getTextContent();
                return value == null ? "" : value;
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public String getTextTrim()
        {
            lock.lock();
            try
            {
                final String result = element.getTextContent();
                return result == null ? null : result.trim();
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public String getChildText( final String elementName )
        {
            final Optional<XmlElement> child = getChild( elementName );
            return child.map( XmlElement::getText ).orElse( null );
        }

        @Override
        public void setAttribute( final String name, final String value )
        {
            lock.lock();
            try
            {
                element.setAttribute( name, value );
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public void detach()
        {
            lock.lock();
            try
            {
                element.getParentNode().removeChild( element );
            }
            finally
            {
                lock.unlock();
            }
            lock = new ReentrantLock();
        }

        @Override
        public void removeContent()
        {
            lock.lock();
            try
            {
                final NodeList nodeList = element.getChildNodes();
                for ( final XmlElement child : XmlFactory.XmlFactoryW3c.nodeListToElementList( nodeList, lock ) )
                {
                    element.removeChild( ( (XmlElementW3c) child ).element );
                    ( ( XmlElementW3c ) child ).lock = new ReentrantLock();
                }
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public void removeAttribute( final String attributeName )
        {
            lock.lock();
            try
            {
                element.removeAttribute( attributeName );
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public void addContent( final XmlElement element )
        {
            addContent( Collections.singletonList( element ) );
        }

        @Override
        public void addContent( final List<XmlElement> elements )
        {
            lock.lock();
            try
            {
                for ( final XmlElement element : elements )
                {
                    final org.w3c.dom.Element w3cElement = ( ( XmlElementW3c ) element ).element;
                    this.element.getOwnerDocument().adoptNode( w3cElement );
                    this.element.appendChild( w3cElement );
                    ( ( XmlElementW3c ) element ).lock = lock;
                }
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public void addText( final String text )
        {
            lock.lock();
            try
            {
                final DocumentBuilder documentBuilder = XmlFactory.XmlFactoryW3c.getBuilder();
                final org.w3c.dom.Document document = documentBuilder.newDocument();
                final org.w3c.dom.Text textNode = document.createTextNode( text );
                this.element.getOwnerDocument().adoptNode( textNode );
                element.appendChild( textNode );
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public void setComment( final List<String> textLines )
        {
            lock.lock();
            try
            {
                final NodeList nodeList = element.getChildNodes();
                for ( int i = 0; i < nodeList.getLength(); i++ )
                {
                    final Node node = nodeList.item( i );
                    if ( node.getNodeType() == Node.COMMENT_NODE )
                    {
                        element.removeChild( node );
                    }
                }

                final DocumentBuilder documentBuilder = XmlFactory.XmlFactoryW3c.getBuilder();
                final org.w3c.dom.Document document = documentBuilder.newDocument();

                final List<String> reversedList = new ArrayList<>( textLines );
                Collections.reverse( reversedList );
                for ( final String text : reversedList )
                {
                    final org.w3c.dom.Comment textNode = document.createComment( text );
                    this.element.getOwnerDocument().adoptNode( textNode );

                    if ( element.hasChildNodes() )
                    {
                        element.insertBefore( textNode, element.getFirstChild() );
                    }
                    else
                    {
                        element.appendChild( textNode );
                    }

                }
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public XmlElement copy()
        {
            lock.lock();
            try
            {
                final Node newNode = this.element.cloneNode( true );
                this.element.getOwnerDocument().adoptNode( newNode );
                return new XmlElementW3c( (org.w3c.dom.Element ) newNode, lock );
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public XmlElement parent()
        {
            return new XmlElementW3c( ( org.w3c.dom.Element ) this.element.getParentNode(), lock );
        }
    }
}
