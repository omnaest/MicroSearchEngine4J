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

import java.util.List;
import java.util.stream.Stream;

import org.omnaest.search.classic.analyzer.AnalysisType;
import org.omnaest.search.classic.analyzer.Token;
import org.omnaest.search.classic.internal.CodeSequenceStatisticsCollectorUtils.CodeSequenceStatisticsCollectorBuilder;
import org.omnaest.search.classic.utils.StreamModifier.UnaryStreamModifier;

/**
 * A {@link CodeSequenceStatisticsCollector} analyzes sequences of codes and than allows to match given sequences or analyze the statistical distribution of
 * sequences.
 *
 * @see CodeSequenceStatisticsCollectorUtils
 * @see CodeSequenceStatisticsCollectorUtils#builder()
 * @author Omnaest
 * @param <C>
 * @param <R>
 */
public interface CodeSequenceStatisticsCollector<C, R>
{
	public static enum AnalysisOption
	{
		/**
		 * This includes the partial code sequences. <br>
		 * <br>
		 * E.g. if a code sequence 'abcd' is given, then the subsequences 'bcd' and 'cd' and 'd' are also processed
		 */
		INCLUDE_PARTIAL_SEQUENCES
	}

	public static interface Metrics<C, R>
	{

		Stream<MatchingTokenAndStatistics<C, R>> getTokenAndStatistics();

		int size();
	}

	public static interface CodeSequence<C> extends Comparable<CodeSequence<C>>
	{
		public List<C> asList();

		public int size();
	}

	public static class MatchingTokenAndStatistics<C, R>
	{
		private Token<C>		token;
		private R				reference;
		private double			score;
		private AnalysisType	analysisType;

		public MatchingTokenAndStatistics(Token<C> token, R reference, double score, AnalysisType analysisType)
		{
			super();
			this.token = token;
			this.reference = reference;
			this.score = score;
			this.analysisType = analysisType;
		}

		public boolean hasAnalysisType(AnalysisType analysisType)
		{
			return this.analysisType.equals(analysisType);
		}

		public Token<C> getToken()
		{
			return this.token;
		}

		public R getReference()
		{
			return reference;
		}

		public double getScore()
		{
			return score;
		}

		@Override
		public String toString()
		{
			return "[token=" + token + ", reference=" + reference + ", score=" + score + "]";
		}

	}

	public static interface MatchingTokenStreamModifier<C, R> extends UnaryStreamModifier<MatchingTokenAndStatistics<C, R>>
	{
	}

	public static class MatchTokenAndIndexStatistics<C>
	{
		private int				matchCount;
		private int				indexSize;
		private AnalysisType	analysisType;
		private Token<C>		token;

		public MatchTokenAndIndexStatistics(Token<C> token, int matchCount, int indexSize, AnalysisType analysisType)
		{
			super();
			this.token = token;
			this.matchCount = matchCount;
			this.indexSize = indexSize;
			this.analysisType = analysisType;
		}

		public Token<C> getToken()
		{
			return token;
		}

		public int getMatchCount()
		{
			return matchCount;
		}

		public int getIndexSize()
		{
			return indexSize;
		}

		public AnalysisType getAnalysisType()
		{
			return analysisType;
		}

		@Override
		public String toString()
		{
			return "[matchCount=" + matchCount + ", indexSize=" + indexSize + ", analysisType=" + analysisType + "]";
		}

	}

	public static interface ScoringFunction<C, R>
	{
		public double score(MatchTokenAndIndexStatistics<C> matchAndIndexStatistics);
	}

	public static interface Matcher<C, R>
	{
		public int matchingDepth(List<C> codeSequence);

		public Stream<MatchingTokenAndStatistics<C, R>> match(List<C> codeSequence, ScoringFunction<C, R> scoringFunction);

		public Stream<MatchingTokenAndStatistics<C, R>> matchAll(ScoringFunction<C, R> scoringFunction);

		Stream<MatchingTokenAndStatistics<C, R>> match(List<C> codeSequence, ScoringFunction<C, R> scoringFunction, AnalysisType analysisType);
	}

	public void analyze(List<C> codeSequence, R reference);

	public Metrics<C, R> extractMetrics(MatchingTokenStreamModifier<C, R> modifier);

	public Stream<MatchingTokenAndStatistics<C, R>> generateCodeSequenceAndMetaDataStream();

	public void analyze(Stream<? extends List<C>> codeSequences, R reference);

	public Matcher<C, R> matcher();

	public Metrics<C, R> extractMetrics();

	public boolean hasAnalysisType(AnalysisType analysisType);

	Metrics<C, R> extractMetrics(AnalysisType analysisType);

	public static <C, R> CodeSequenceStatisticsCollectorBuilder<C, R> builder()
	{
		return CodeSequenceStatisticsCollectorUtils.builder();
	}

}