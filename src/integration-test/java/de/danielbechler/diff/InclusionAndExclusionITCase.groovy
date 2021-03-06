/*
 * Copyright 2014 Daniel Bechler
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

package de.danielbechler.diff

import de.danielbechler.diff.annotation.ObjectDiffProperty
import de.danielbechler.diff.collection.CollectionItemElement
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by dbechler.
 */
@Stepwise
class InclusionAndExclusionITCase extends Specification {

    def builder = ObjectDifferBuilder.startBuilding()
    def configurable = builder.configure()

    def base = new PhoneBook(name: "Jerry's Phone Book", revision: 1, contacts: [
            new Contact(id: "elaine", name: "Elaine", number: "917-555-0186"),
            new Contact(id: "george", name: "George", number: "917-555-0102"),
            new Contact(id: "kramer", name: "Kramer", number: "917-555-3456")
    ])

    // Jerry decided to give his phone book a more formal title
    def working = new PhoneBook(name: "Jerry Seinfeld's Phone Book", revision: 2, contacts: [
            new Contact(id: "elaine", name: "Elaine", number: "917-555-0186"),

            // George has a new nickname
            new Contact(id: "george", name: "Koko", number: "917-555-0102"),

            // Jerry always ended up at the Moviefone hotline until he realized Kramers new number actually ends with 5, not 6
            new Contact(id: "kramer", name: "Kramer", number: "917-555-3455")
    ])

    def "Sanity check"() {
        when:
        def node = ObjectDifferBuilder.buildDefault().compare(working, base)

        then:
        node.getChild('name').changed

        and: "only two contacts should have changed"
        node.getChild('contacts').changed
        node.getChild('contacts').childCount() == 2

        and: "only Georges name should have changed"
        node.getChild('contacts').getChild(new CollectionItemElement(new Contact(id: 'george'))).childCount() == 1
        node.getChild('contacts').getChild(new CollectionItemElement(new Contact(id: 'george'))).getChild('name').changed

        and: "only Kramers number should have changed"
        node.getChild('contacts').getChild(new CollectionItemElement(new Contact(id: 'kramer'))).childCount() == 1
        node.getChild('contacts').getChild(new CollectionItemElement(new Contact(id: 'kramer'))).getChild('number').changed
    }

    def "Property with specific name excluded via configuration"() {
        given:
        configurable.inclusion().toExclude().propertyNames("name")

        when:
        def node = builder.build().compare(working, base)

        then: "Georges name change should be ignored"
        node.getChild('contacts').getChild(new CollectionItemElement(new Contact(id: 'george'))) == null

        and: "The name change of the phone book should be ignored too"
        node.getChild('name') == null
    }

    def "Category excluded via configuration"() {
        given:
        configurable.inclusion().toExclude().categories('private')

        when:
        def node = builder.build().compare(working, base)

        then: "Kramers changed number should be ignored"
        node.getChild('contacts').getChild(new CollectionItemElement(new Contact(id: 'kramer'))) == null
    }

    def "Type excluded via configuration"() {
        given:
        configurable.inclusion().toExclude().types(Contact)

        when:
        def node = builder.build().compare(working, base)

        then: "all contact changes should be ignored"
        node.getChild('contacts') == null
    }

    def "Node at specific NodePath excluded via configuration"() {
        given:
        configurable.inclusion().toExclude().nodes(NodePath.createBuilder()
                .withRoot()
                .withPropertyName('contacts')
                .withCollectionItem(new Contact(id: 'george'))
                .build())

        when:
        def node = builder.build().compare(working, base)

        then:
        node.getChild('contacts').getChild(new CollectionItemElement(new Contact(id: 'george'))) == null
    }

    def "Property excluded via @ObjectDiffProperty annotation"() {
        given:
        configurable.inclusion().toExclude().nodes(NodePath.buildWith('name'))

        when:
        def node = builder.build().compare(working, base)

        then: "the name change of the phone book should be ignored"
        node.getChild('revision') == null
    }

    def "Inclusion via property name"() {
        given:
        configurable.inclusion().toInclude().propertyNames('name', 'contacts')
        when:
        def node = builder.build().compare(working, base)
        then:
        node.getChild("name").changed
        and:
        def element = new CollectionItemElement(new Contact(id: "george"))
        node.getChild("contacts").childCount() == 1
        node.getChild("contacts").getChild(element).getChild("name").changed
    }

    @EqualsAndHashCode
    @ToString(includePackage = false)
    class PhoneBook {
        def name
        def contacts = []
        def revision

        @SuppressWarnings("GroovyUnusedDeclaration")
        @ObjectDiffProperty(excluded = true)
        def getRevision() {
            return revision
        }

        void setRevision(revision) {
            this.revision = revision
        }
    }

    @EqualsAndHashCode(includes = ["id"])
    @ToString(includePackage = false)
    class Contact {
        def id
        def name
        def number

        @SuppressWarnings("GroovyUnusedDeclaration")
        @ObjectDiffProperty(categories = ['private'])
        def getNumber() {
            return number
        }

        void setNumber(number) {
            this.number = number
        }
    }
}
