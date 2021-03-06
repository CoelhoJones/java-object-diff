/*
 * Copyright 2012 Daniel Bechler
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

package de.danielbechler.diff;

/**
 * This is the entry point for all comparisons. It determines the type of the given objects and passes them to
 * the appropriate {@link Differ}.
 *
 * @author Daniel Bechler
 */
public class ObjectDiffer
{
	private final DifferDispatcher dispatcher;

	ObjectDiffer(final DifferDispatcher differDispatcher)
	{
		this.dispatcher = differDispatcher;
	}

	/**
	 * Recursively inspects the given objects and returns a node representing their differences. Both objects
	 * have be have the same type.
	 *
	 * @param working This object will be treated as the successor of the <code>base</code> object.
	 * @param base    This object will be treated as the predecessor of the <code>working</code> object.
	 * @param <T>     The type of the objects to compare.
	 *
	 * @return A node representing the differences between the given objects.
	 */
	public <T> DiffNode compare(final T working, final T base)
	{
		return dispatcher.dispatch(DiffNode.ROOT, Instances.of(working, base), RootAccessor.getInstance());
	}
}
