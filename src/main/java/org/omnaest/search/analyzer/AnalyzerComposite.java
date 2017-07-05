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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.search.internal.CodeAndPosition;
import org.omnaest.search.internal.SequenceKey;

public class AnalyzerComposite<C> implements Analyzer<C>
{
	private List<Analyzer<C>> analyzers = new ArrayList<>();

	protected static class AnalysisTypeForComposite implements AnalysisType
	{
		private List<AnalysisType> analysisTypes;

		public AnalysisTypeForComposite(List<AnalysisType> analysisTypes)
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
			AnalysisTypeForComposite other = (AnalysisTypeForComposite) obj;
			if (this.analysisTypes == null)
			{
				if (other.analysisTypes != null)
					return false;
			} else if (!this.analysisTypes.equals(other.analysisTypes))
				return false;
			return true;
		}

	}

	public AnalyzerComposite(Collection<Analyzer<C>> analyzers)
	{
		super();
		this.analyzers.addAll(analyzers);
	}

	@Override
	public Stream<Token<C>> analyze(Stream<CodeAndPosition<C>> codeAndPositionStream)
	{
		List<CodeAndPosition<C>> codeAndPositionSequence = codeAndPositionStream.collect(Collectors.toList());
		return this.analyzers	.stream()
								.map(analzyer -> analzyer.analyze(codeAndPositionSequence.stream()))
								.flatMap(stream -> stream);
	}

	@Override
	public AnalysisType getAnalysisType()
	{
		return new AnalysisTypeForComposite(this.analyzers	.stream()
															.map(analyzer -> analyzer.getAnalysisType())
															.collect(Collectors.toList()));
	}

	@Override
	public Stream<SequenceKey<C>> analyzeQuery(SequenceKey<C> sequenceKey)
	{
		return this.analyzers	.stream()
								.map(analyzer -> analyzer.analyzeQuery(sequenceKey))
								.flatMap(keys -> keys);
	}

}
