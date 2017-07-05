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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.search.internal.CodeAndPosition;
import org.omnaest.search.internal.SequenceKey;

public class AnalyzerSingleInsertions<C> implements Analyzer<C>
{
	@Override
	public Stream<Token<C>> analyze(Stream<CodeAndPosition<C>> codeAndPositionStream)
	{
		boolean start = true;
		boolean terminal = true;
		return Stream.of(new Token<>(codeAndPositionStream.collect(Collectors.toList()), start, terminal));
	}

	@Override
	public AnalysisType getAnalysisType()
	{
		return AnalysisTypeBasic.INSERTION;
	}

	@Override
	public Stream<SequenceKey<C>> analyzeQuery(SequenceKey<C> sequenceKey)
	{
		List<SequenceKey<C>> sequeneKeys = new ArrayList<>();
		for (int ii = 0; ii < sequenceKey.size(); ii++)
		{
			List<C> reducedCodeAndPositions = sequenceKey.getCodeSequence();
			reducedCodeAndPositions.remove(ii);
			sequeneKeys.add(new SequenceKey<>(reducedCodeAndPositions));
		}
		return sequeneKeys.stream();
	}
}
