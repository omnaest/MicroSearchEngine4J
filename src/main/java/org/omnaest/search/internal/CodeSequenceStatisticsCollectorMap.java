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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.search.analyzer.AnalysisType;
import org.omnaest.search.analyzer.AnalysisTypeBasic;
import org.omnaest.search.analyzer.Analyzer;
import org.omnaest.search.analyzer.Token;
import org.omnaest.search.internal.SequenceIndex.IndexEntry;
import org.omnaest.search.internal.SequenceIndex.TokenAndReference;
import org.omnaest.search.utils.StreamModifier;
import org.omnaest.search.utils.StreamModifier.UnaryStreamModifier;
import org.omnaest.search.utils.StreamUtils;
import org.omnaest.utils.JSONHelper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * Simple {@link ConcurrentHashMap} based implementation of the {@link CodeSequenceStatisticsCollector} interface.<br>
 * <br>
 * This type allows exact matches and sub sequence matches beginning from the front of a sequence.
 *
 * @see AnalysisTypeBasic#EXACT
 * @author Omnaest
 * @param <C>
 * @param <R>
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class CodeSequenceStatisticsCollectorMap<C, R> extends AbstractCodeSequenceStatisticsCollector<C, R>
{
	protected AnalyzerAndSequenceIndex<C, R> analyzerAndSequenceIndex = new AnalyzerAndSequenceIndex<>();

	protected Predicate<List<C>>						codeSequenceMatcher		= StreamUtils.matchAllPredicate();
	protected UnaryStreamModifier<CodeAndPosition<C>>	codeSequenceModifier	= StreamModifier.UNMODIFYING();

	public CodeSequenceStatisticsCollectorMap<C, R> setCodeSequenceMatcher(Predicate<List<C>> codeSequenceMatcher)
	{
		this.codeSequenceMatcher = codeSequenceMatcher;
		return this;
	}

	@Override
	public CodeSequenceStatisticsCollectorMap<C, R> setCodeSequenceModifier(UnaryStreamModifier<CodeAndPosition<C>> codeSequenceModifier)
	{
		this.codeSequenceModifier = codeSequenceModifier;
		return this;
	}

	@Override
	public void analyze(Stream<? extends List<C>> codeSequences, R reference)
	{
		if (codeSequences != null)
		{
			codeSequences	.filter(this.codeSequenceMatcher)
							.forEach(codeSequence ->
							{
								this.analyzeFiltered(codeSequence, reference);
							});
		}
	}

	/**
	 * Analyzes the given code sequence. The sequence has already been filtered by the {@link #codeSequenceMatcher}
	 *
	 * @param codeSequence
	 * @param reference
	 */
	protected void analyzeFiltered(List<C> codeSequence, R reference)
	{
		if (codeSequence != null)
		{
			this.analyzeFiltered(codeSequence.stream(), codeSequence.size(), reference);
		}
	}

	protected void analyzeFiltered(Stream<C> codeSequenceStream, int codeSequenceSize, R reference)
	{
		List<C> codeSequenceList = codeSequenceStream.collect(Collectors.toList());
		for (Analyzer<C> analyzer : this.analyzerAndSequenceIndex.getAnalyzers())
		{
			Stream<CodeAndPosition<C>> codeAndPositionStream = this.generateCodeAndPositionStream(codeSequenceList.stream(), codeSequenceSize);
			analyzer.analyze(codeAndPositionStream)
					.forEach(token ->
					{

						this.analyzerAndSequenceIndex	.getIndex(analyzer)
														.getOrCreateEntry(token.getCodeSequenceKey())
														.addTokenAndReference(token, reference);

					});
		}
	}

	private Stream<CodeAndPosition<C>> generateCodeAndPositionStream(Stream<C> codeSequenceStream, int codeSequenceSize)
	{
		AtomicInteger position = new AtomicInteger(0);
		return StreamUtils.modify(	codeSequenceStream.peek(code -> position.incrementAndGet())
													.map(code -> new CodeAndPosition<>(	code, position.get(), position.get() == codeSequenceSize - 1,
																						position.get() == 0)),
									this.codeSequenceModifier);
	}

	@Override
	public String toString()
	{
		Stream<List<IndexEntry<C, R>>> indexEntryStream = this.analyzerAndSequenceIndex	.getAllIndexes()
																						.stream()
																						.map(index -> index.getEntries());
		Stream<IndexEntry<C, R>> flatMap = indexEntryStream.flatMap(entryList -> entryList.stream());

		Map<Comparable<IndexEntry<C, R>>, IndexEntry<C, R>> collect = flatMap.collect(Collectors.toMap(entry -> (Comparable<IndexEntry<C, R>>) new Comparable<IndexEntry<C, R>>()
		{
			@Override
			public int compareTo(IndexEntry<C, R> indexEntry)
			{
				return 0;
			}
		}, entry -> entry));

		return JSONHelper.prettyPrint(new TreeMap<>(collect));
	}

	@Override
	public Stream<MatchingTokenAndStatistics<C, R>> generateCodeSequenceAndMetaDataStream()
	{
		return this	.matcher()
					.matchAll(m -> m.getToken()
									.getStartPosition());
	}

	@Override
	public Matcher<C, R> matcher()
	{
		return new Matcher<C, R>()
		{
			@Override
			public int matchingDepth(List<C> codeSequence)
			{
				int retval = 0;
				SequenceKey<C> sequenceKey = new SequenceKey<>(Arrays.asList());
				for (C code : codeSequence)
				{
					sequenceKey = sequenceKey.append(code);

					SequenceKey<C> sequenceKeyClone = sequenceKey.clone();

					boolean anyMatch = CodeSequenceStatisticsCollectorMap.this.analyzerAndSequenceIndex	.getAllIndexes()
																										.stream()
																										.anyMatch(index -> index.contains(sequenceKeyClone));

					if (anyMatch)
					{
						retval = Math.max(retval, sequenceKey.size());
					}
				}
				return retval;
			}

			@Override
			public Stream<MatchingTokenAndStatistics<C, R>> match(List<C> codeSequence, ScoringFunction<C, R> scoringFunction)
			{
				AnalysisType analysisType = null;
				return this.match(codeSequence, scoringFunction, analysisType);
			}

			@Override
			public Stream<MatchingTokenAndStatistics<C, R>> match(List<C> codeSequence, ScoringFunction<C, R> scoringFunction, AnalysisType analysisType)
			{
				SequenceKey<C> sequenceKey = new SequenceKey<>(codeSequence);
				return this.match(index -> index.getEntries(sequenceKey), scoringFunction, analysisType);
			}

			@Override
			public Stream<MatchingTokenAndStatistics<C, R>> matchAll(ScoringFunction<C, R> scoringFunction)
			{
				Function<SequenceIndex<C, R>, Stream<IndexEntry<C, R>>> matchingFunction = new Function<SequenceIndex<C, R>, Stream<IndexEntry<C, R>>>()
				{
					@Override
					public Stream<IndexEntry<C, R>> apply(SequenceIndex<C, R> index)
					{
						return index.getEntries()
									.stream();
					}
				};
				return this.match(matchingFunction, scoringFunction);
			}

			protected Stream<MatchingTokenAndStatistics<C, R>> match(	Function<SequenceIndex<C, R>, Stream<IndexEntry<C, R>>> matchingFunction,
																		ScoringFunction<C, R> scoringFunction)
			{
				AnalysisType analysisTypeFilter = null;
				return this.match(matchingFunction, scoringFunction, analysisTypeFilter);
			}

			protected Stream<MatchingTokenAndStatistics<C, R>> match(	Function<SequenceIndex<C, R>, Stream<IndexEntry<C, R>>> matchingFunction,
																		ScoringFunction<C, R> scoringFunction, AnalysisType analysisTypeFilter)
			{
				return CodeSequenceStatisticsCollectorMap.this.analyzerAndSequenceIndex	.getIndexesFor(analysisTypeFilter)
																						.stream()
																						.flatMap(index ->
																						{
																							int indexSize = index.getCount();
																							AnalysisType analysisType = index.getAnalysisType();

																							Stream<IndexEntry<C, R>> indexEntries = matchingFunction.apply(index);

																							return indexEntries	.filter(entry -> entry != null)
																												.flatMap(indexEntry ->
																												{
																													int matchCount = indexEntry.getCount();
																													List<TokenAndReference<C, R>> tokenAndReferences = indexEntry.getTokenAndReferences();

																													return tokenAndReferences	.stream()
																																				.map(tokenAndReference ->
																																				{
																																					Token<C> token = tokenAndReference.getToken();

																																					R reference = tokenAndReference.getReference();
																																					double score = scoringFunction.score(new MatchTokenAndIndexStatistics<>(token,
																																																							matchCount,
																																																							indexSize,
																																																							analysisType));
																																					MatchingTokenAndStatistics<C, R> matchingTokenAndStatistics = new MatchingTokenAndStatistics<>(	token,
																																																													reference,
																																																													score,
																																																													analysisType);
																																					return matchingTokenAndStatistics;
																																				});
																												});

																						})
																						.sorted((m1, m2) -> -1 * Double.compare(m1.getScore(), m2.getScore()));
			}

		};
	}

	@Override
	public CodeSequenceStatisticsCollectorModifiable<C, R> addAnalyzer(Analyzer<C> analyzer)
	{
		this.analyzerAndSequenceIndex.addAnalyzer(analyzer);
		return this;
	}

	@Override
	public boolean hasAnalysisType(AnalysisType analysisType)
	{
		return this.analyzerAndSequenceIndex.hasAnalysisType(analysisType);
	}

}
