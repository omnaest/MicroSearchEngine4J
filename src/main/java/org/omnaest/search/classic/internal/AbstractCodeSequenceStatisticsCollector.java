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
package org.omnaest.search.classic.internal;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.search.classic.analyzer.AnalysisType;
import org.omnaest.utils.JSONHelper;

public abstract class AbstractCodeSequenceStatisticsCollector<C, R> implements CodeSequenceStatisticsCollectorModifiable<C, R>
{

	protected static class MetricsImpl<C, R> implements Metrics<C, R>
	{
		private List<MatchingTokenAndStatistics<C, R>> tokenAndStatistics;

		public MetricsImpl(List<MatchingTokenAndStatistics<C, R>> tokenAndStatistics)
		{
			this.tokenAndStatistics = tokenAndStatistics;
		}

		@Override
		public String toString()
		{
			return JSONHelper.prettyPrint(this.tokenAndStatistics);
		}

		@Override
		public Stream<MatchingTokenAndStatistics<C, R>> getTokenAndStatistics()
		{
			return this.tokenAndStatistics.stream();
		}

		@Override
		public int size()
		{
			return this.tokenAndStatistics.size();
		}

	}

	@Override
	public Metrics<C, R> extractMetrics()
	{
		return this.extractMetrics(stream -> stream);
	}

	@Override
	public Metrics<C, R> extractMetrics(AnalysisType analysisType)
	{
		return this.extractMetrics(stream -> stream.filter(matchingTokenAndStatistics -> matchingTokenAndStatistics.hasAnalysisType(analysisType)));
	}

	@Override
	public Metrics<C, R> extractMetrics(MatchingTokenStreamModifier<C, R> modifier)
	{
		Stream<MatchingTokenAndStatistics<C, R>> codeSequenceAndMetaDataStream = this.generateCodeSequenceAndMetaDataStream();

		if (modifier != null)
		{
			codeSequenceAndMetaDataStream = modifier.modify(codeSequenceAndMetaDataStream);
		}

		return new MetricsImpl<>(codeSequenceAndMetaDataStream.collect(Collectors.toList()));
	}

	@Override
	public void analyze(List<C> codeSequence, R reference)
	{
		this.analyze(	Arrays.asList(codeSequence)
							.stream(),
						reference);
	}

}