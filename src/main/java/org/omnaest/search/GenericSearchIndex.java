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
package org.omnaest.search;

import java.util.List;
import java.util.stream.Stream;

import org.omnaest.search.analyzer.AnalysisType;
import org.omnaest.search.analyzer.Analyzer;
import org.omnaest.search.analyzer.AnalyzerChain;
import org.omnaest.search.analyzer.Token;
import org.omnaest.search.domain.Word;
import org.omnaest.search.internal.CodeSequenceStatisticsCollector.MatchingTokenAndStatistics;
import org.omnaest.search.internal.CodeSequenceStatisticsCollector.MatchingTokenStreamModifier;
import org.omnaest.search.internal.CodeSequenceStatisticsCollector.Metrics;
import org.omnaest.search.internal.CodeSequenceStatisticsCollector.ScoringFunction;
import org.omnaest.search.internal.CodeSequenceStatisticsCollectorMap;
import org.omnaest.search.internal.CodeSequenceStatisticsCollectorModifiable;

public class GenericSearchIndex<W extends Word<C>, C, R>
{
	private CodeSequenceStatisticsCollectorModifiable<C, R> collector = new CodeSequenceStatisticsCollectorMap<>();

	public static interface Matcher<W extends Word<C>, C, R>
	{
		public Matcher<W, C, R> withScoringFunction(ScoringFunction<C, R> scoringFunction);

		public Matcher<W, C, R> withAnalysisType(AnalysisType analysisType);

		public Stream<MatchingTokenAndStatistics<C, R>> match(W word);

		public Stream<MatchingTokenAndStatistics<C, R>> match(List<C> codeSequence);

	}

	public GenericSearchIndex<W, C, R> addAnalyzer(Analyzer<C> analyzer)
	{
		this.collector.addAnalyzer(analyzer);
		return this;
	}

	@SuppressWarnings("unchecked")
	public GenericSearchIndex<W, C, R> addAnalyzerChain(Analyzer<C>... analyzers)
	{
		return this.addAnalyzer(new AnalyzerChain<>(analyzers));
	}

	public void analyze(Stream<W> words, R reference)
	{
		this.collector.analyze(words.map(word -> word.getCodeSequence()), reference);
	}

	public Matcher<W, C, R> matcher()
	{
		return new Matcher<W, C, R>()
		{
			private ScoringFunction<C, R>	scoringFunction	= (stats) -> 1.0;
			private AnalysisType			analysisType	= null;

			@Override
			public Stream<MatchingTokenAndStatistics<C, R>> match(W word)
			{
				return this.match(word.getCodeSequence());
			}

			@Override
			public Stream<MatchingTokenAndStatistics<C, R>> match(List<C> codeSequence)
			{
				return GenericSearchIndex.this.collector.matcher()
														.match(codeSequence, this.scoringFunction, this.analysisType);
			}

			@Override
			public Matcher<W, C, R> withScoringFunction(ScoringFunction<C, R> scoringFunction)
			{
				this.scoringFunction = scoringFunction;
				return this;
			}

			@Override
			public Matcher<W, C, R> withAnalysisType(AnalysisType analysisType)
			{
				this.analysisType = analysisType;
				return this;
			}
		};
	}

	public Metrics<C, R> extractMetrics()
	{
		return this.collector.extractMetrics(stream -> stream);
	}

	public Metrics<C, R> extractMetrics(AnalysisType analysisType)
	{
		return this.collector.extractMetrics(analysisType);
	}

	public Metrics<C, R> extractMetrics(MatchingTokenStreamModifier<C, R> modifier)
	{
		return this.collector.extractMetrics(modifier);
	}

	public static class JoinMatch<C, R>
	{
		private MatchingTokenAndStatistics<C, R>	leftMatchingTokenAndStatistics;
		private MatchingTokenAndStatistics<C, R>	rightMatchingTokenAndStatistics;

		public JoinMatch(MatchingTokenAndStatistics<C, R> leftMatchingTokenAndStatistics, MatchingTokenAndStatistics<C, R> rightMatchingTokenAndStatistics)
		{
			super();
			this.leftMatchingTokenAndStatistics = leftMatchingTokenAndStatistics;
			this.rightMatchingTokenAndStatistics = rightMatchingTokenAndStatistics;
		}

		public MatchingTokenAndStatistics<C, R> getLeftMatchingTokenAndStatistics()
		{
			return this.leftMatchingTokenAndStatistics;
		}

		public MatchingTokenAndStatistics<C, R> getRightMatchingTokenAndStatistics()
		{
			return this.rightMatchingTokenAndStatistics;
		}

		public Token<C> getToken()
		{
			return this.leftMatchingTokenAndStatistics.getToken();
		}

		@Override
		public String toString()
		{
			return "[" + this.leftMatchingTokenAndStatistics + "," + this.rightMatchingTokenAndStatistics + "]";
		}

	}

	public Stream<JoinMatch<C, R>> join(GenericSearchIndex<W, C, R> searchIndex, AnalysisType analysisType)
	{
		Stream<JoinMatch<C, R>> retstream = Stream.empty();

		Metrics<C, R> rightMetrics = searchIndex.extractMetrics(analysisType);
		Metrics<C, R> leftMetrics = this.extractMetrics(analysisType);

		int rightSize = rightMetrics.size();
		int leftSize = leftMetrics.size();

		Matcher<W, C, R> leftMatcher = this.matcher();
		Matcher<W, C, R> rightMatcher = searchIndex.matcher();

		if (rightSize < leftSize)
		{
			retstream = this.generateJoinMatch(analysisType, rightMetrics, leftMatcher);
		}
		else
		{
			retstream = this.generateJoinMatch(analysisType, leftMetrics, rightMatcher);
		}

		return retstream;
	}

	private Stream<JoinMatch<C, R>> generateJoinMatch(AnalysisType analysisType, Metrics<C, R> rightMetrics, Matcher<W, C, R> matcher)
	{
		return rightMetrics	.getTokenAndStatistics()
							.flatMap(leftMatchingTokenAndStatistics ->
							{
								return matcher	.withAnalysisType(analysisType)
												.match(leftMatchingTokenAndStatistics	.getToken()
																						.getCodeSequence())
												.map(rightMatchingTokenAndStatistics -> new JoinMatch<>(leftMatchingTokenAndStatistics,
																										rightMatchingTokenAndStatistics));

							});
	}

}
