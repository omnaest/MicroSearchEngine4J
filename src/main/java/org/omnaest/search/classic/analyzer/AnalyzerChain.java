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
package org.omnaest.search.classic.analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.search.classic.internal.CodeAndPosition;
import org.omnaest.search.classic.internal.SequenceKey;

public class AnalyzerChain<C> implements Analyzer<C>
{
	private List<Analyzer<C>> analyzers = new ArrayList<>();

	private static class AnalysisTypeForChain implements AnalysisType
	{
		private List<AnalysisType> analysisTypes;

		public AnalysisTypeForChain(List<AnalysisType> analysisTypes)
		{
			this.analysisTypes = analysisTypes;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.analysisTypes == null) ? 0 : this.analysisTypes.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (this.getClass() != obj.getClass())
				return false;
			AnalysisTypeForChain other = (AnalysisTypeForChain) obj;
			if (this.analysisTypes == null)
			{
				if (other.analysisTypes != null)
					return false;
			} else if (!this.analysisTypes.equals(other.analysisTypes))
				return false;
			return true;
		}

	}

	public AnalyzerChain(Collection<Analyzer<C>> analyzers)
	{
		super();
		this.analyzers.addAll(analyzers);
	}

	@SafeVarargs
	public AnalyzerChain(Analyzer<C>... analyzers)
	{
		this(Arrays.asList(analyzers));
	}

	@Override
	public Stream<Token<C>> analyze(Stream<CodeAndPosition<C>> codeAndPositionStream)
	{
		Stream<Token<C>> retval = null;

		Stream<CodeAndPosition<C>> currentCodeAndPositionStream = codeAndPositionStream;
		for (Analyzer<C> analyzer : this.analyzers)
		{
			if (retval == null)
			{
				retval = analyzer.analyze(currentCodeAndPositionStream);
			} else
			{
				retval = retval	.map(token -> token.asCodeAndPositionStream())
								.map(s -> analyzer.analyze(s))
								.flatMap(m -> m);
			}
		}

		return retval != null ? retval : Stream.empty();
	}

	@Override
	public AnalysisType getAnalysisType()
	{
		return new AnalysisTypeForChain(this.analyzers	.stream()
														.map(analyzer -> analyzer.getAnalysisType())
														.collect(Collectors.toList()));
	}

	@Override
	public Stream<SequenceKey<C>> analyzeQuery(SequenceKey<C> sequenceKey)
	{
		List<SequenceKey<C>> sequeneKeys = new ArrayList<>(Arrays.asList(sequenceKey));
		this.analyzers	.stream()
						.forEach(analyzer ->
						{
							List<SequenceKey<C>> expandedSequenceKeys = sequeneKeys	.stream()
																					.map(sequence -> analyzer.analyzeQuery(sequenceKey))
																					.flatMap(stream -> stream)
																					.collect(Collectors.toList());
							sequeneKeys.clear();
							sequeneKeys.addAll(expandedSequenceKeys);
						});
		return sequeneKeys.stream();
	}

}
