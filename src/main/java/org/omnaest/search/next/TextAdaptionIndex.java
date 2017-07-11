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
package org.omnaest.search.next;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.omnaest.search.next.AdaptionIndex.MatchGroupImpl.MatchGroupCodeSequenceImpl;

public class TextAdaptionIndex<R> extends AdaptionIndex<Character, R>
{
	public void analyze(String text, R reference)
	{
		super.analyze(	Arrays.asList(ArrayUtils.toObject(text.toCharArray()))
							.stream(),
						reference);
	}

	public static interface TextMatchGroup<C, R> extends MatchGroup<C, R>
	{

	}

	protected static class TextMatchGroupImpl<C, R> extends MatchGroupImpl<C, R> implements TextMatchGroup<C, R>
	{
		public TextMatchGroupImpl(Group<C, R> group)
		{
			super(group);
		}

		@Override
		public MatchGroupCodeSequence<C, R> asCodeSequenceGroup()
		{
			// TODO Auto-generated method stub
			return super.asCodeSequenceGroup();
		}

	}

	public static interface TextMatchCodeSequenceGroup<C, R> extends MatchGroupCodeSequence<C, R>
	{
		public String getText();
	}

	protected static class TextMatchCodeSequenceGroupImpl<C, R> extends MatchGroupCodeSequenceImpl<C, R>
	{

		public TextMatchCodeSequenceGroupImpl(CodeSequenceGroup<C, R> group)
		{
			super(group);
		}

	}

	@Override
	public Stream<TextMatchGroup<Character, R>> extractGroups()
	{
		return this.<TextMatchGroup<Character, R>>extractGroups(group -> new TextMatchGroupImpl<>(group));
	}

}
