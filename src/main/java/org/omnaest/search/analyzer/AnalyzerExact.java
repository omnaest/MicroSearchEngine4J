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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.search.internal.CodeAndPosition;

public class AnalyzerExact<C> extends AbstractAnalyzer<C>
{
	@Override
	public Stream<Token<C>> analyze(Stream<CodeAndPosition<C>> codeAndPositionStream)
	{
		List<CodeAndPosition<C>> codeAndPositionSequence = codeAndPositionStream.collect(Collectors.toList());
		boolean start = true;
		boolean terminal = true;
		return Arrays	.<Token<C>>asList(new Token<>(codeAndPositionSequence, start, terminal))
						.stream();
	}

	@Override
	public AnalysisType getAnalysisType()
	{
		return AnalysisTypeBasic.EXACT;
	}
}
