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
package org.omnaest.search.analyzer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.search.internal.CodeAndPosition;
import org.omnaest.search.internal.SequenceKey;
import org.omnaest.search.utils.ListUtils;

public class Token<C>
{
	private List<CodeAndPosition<C>>	codeAndPositionSequence;
	private boolean						start;
	private boolean						terminal;

	public Token(List<CodeAndPosition<C>> codeAndPositionSequence, boolean start, boolean terminal)
	{
		super();
		this.codeAndPositionSequence = codeAndPositionSequence;
		this.start = start;
		this.terminal = terminal;
	}

	public boolean isTerminal()
	{
		return this.terminal;
	}

	public boolean isStart()
	{
		return this.start;
	}

	public List<C> getCodeSequence()
	{
		return this.codeAndPositionSequence	.stream()
											.map(cp -> cp.getCode())
											.collect(Collectors.toList());
	}

	public Stream<CodeAndPosition<C>> asCodeAndPositionStream()
	{
		return this.codeAndPositionSequence.stream();
	}

	public SequenceKey<C> getCodeSequenceKey()
	{
		return new SequenceKey<>(this.getCodeSequence());
	}

	public int getStartPosition()
	{
		return ListUtils.first(this.codeAndPositionSequence, cp -> cp.getPosition())
						.orElse(-1);
	}

	public int getEndPosition()
	{
		return ListUtils.last(this.codeAndPositionSequence, cp -> cp.getPosition())
						.orElse(-1);
	}

	@Override
	public String toString()
	{
		return this	.getCodeSequence()
					.toString();
	}

}
