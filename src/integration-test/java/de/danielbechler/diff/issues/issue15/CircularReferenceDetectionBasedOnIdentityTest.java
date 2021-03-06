/*
 * Copyright 2013 Daniel Bechler
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

package de.danielbechler.diff.issues.issue15;

import de.danielbechler.diff.*;
import de.danielbechler.diff.helper.*;
import de.danielbechler.diff.mock.*;
import org.testng.annotations.*;

import static de.danielbechler.diff.CircularReferenceMatchingMode.*;

/** @author Daniel Bechler */
public class CircularReferenceDetectionBasedOnIdentityTest
{
	private ObjectDiffer objectDiffer;

	@BeforeMethod
	public void setUp() throws Exception
	{
		final ObjectDifferBuilder configuration = ObjectDifferBuilder.startBuilding();
		configuration.configure()
					 .circularReferenceHandling()
					 .matchCircularReferencesUsing(EQUALITY_OPERATOR);
		objectDiffer = configuration.build();
	}

	@Test
	public void detectsCircularReference_whenEncounteringSameObjectTwice() throws Exception
	{
		final ObjectWithNestedObject object = new ObjectWithNestedObject("foo");
		object.setObject(object);
		final DiffNode node = objectDiffer.compare(object, null);
		NodeAssertions.assertThat(node).child("object").isCircular();
	}

	@Test
	public void detectsNoCircularReference_whenEncounteringDifferentButEqualObjectsTwice() throws Exception
	{
		final ObjectWithNestedObject object = new ObjectWithNestedObject("foo", new ObjectWithNestedObject("foo"));
		final DiffNode node = objectDiffer.compare(object, null);
		NodeAssertions.assertThat(node).child("object").hasState(DiffNode.State.ADDED);
	}
}
