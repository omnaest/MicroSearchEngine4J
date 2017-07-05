/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.search.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.omnaest.search.internal.CodeSequenceStatisticsCollector.CodeSequence;
import org.omnaest.search.utils.ComparatorUtils;
import org.omnaest.search.utils.ListUtils;

public class SequenceKey<C> implements CodeSequence<C>
{
	private List<C> codeSequence = new ArrayList<>();

	public SequenceKey(List<C> codeSequence)
	{
		super();
		this.codeSequence.addAll(codeSequence);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.codeSequence == null) ? 0 : this.codeSequence.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		SequenceKey<C> other = (SequenceKey<C>) obj;
		if (this.codeSequence == null)
		{
			if (other.codeSequence != null)
				return false;
		}
		else if (!this.codeSequence.equals(other.codeSequence))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return this.codeSequence.toString();
	}

	@Override
	public int compareTo(CodeSequence<C> sequenceKey)
	{
		return ComparatorUtils.compare(this.codeSequence, sequenceKey.asList(), (o1, o2) -> this.compare(o1, o2));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int compare(Object o1, Object o2)
	{
		if (o1 == null && o2 != null)
		{
			return 1;
		}
		else if (o1 != null && o2 == null)
		{
			return -1;
		}
		else if (o1 == null && o2 == null)
		{
			return 0;
		}
		else if (o1 instanceof Comparable && o1 instanceof Comparable)
		{
			return ((Comparable) o1).compareTo(o2);
		}
		else
		{
			return String	.valueOf(ObjectUtils.defaultIfNull(o1, ""))
							.compareToIgnoreCase(String.valueOf(ObjectUtils.defaultIfNull(o2, "")));
		}
	}

	@Override
	public List<C> asList()
	{
		return new ArrayList<>(this.codeSequence);
	}

	@Override
	public int size()
	{
		return this.codeSequence.size();
	}

	/**
	 * Returns a new immutable {@link SequenceKey} having the given code appended to the code sequence of the current {@link SequenceKey}
	 *
	 * @param code
	 * @return
	 */
	public SequenceKey<C> append(C code)
	{
		List<C> codeSequence = new ArrayList<>(this.codeSequence);
		codeSequence.add(code);
		return new SequenceKey<>(codeSequence);
	}

	@Override
	public SequenceKey<C> clone()
	{
		return new SequenceKey<>(this.codeSequence);
	}

	public SequenceKey<C> inverse()
	{
		return new SequenceKey<>(ListUtils.inverse(this.codeSequence));
	}

	public List<C> getCodeSequence()
	{
		return new ArrayList<>(this.codeSequence);
	}
}