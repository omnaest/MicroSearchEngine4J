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
package org.omnaest.search.classic;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Test;
import org.omnaest.search.classic.analyzer.AnalysisTypeBasic;
import org.omnaest.search.classic.analyzer.AnalyzerBackNGram;
import org.omnaest.search.classic.analyzer.AnalyzerExact;
import org.omnaest.search.classic.analyzer.AnalyzerFrontNGram;
import org.omnaest.search.classic.analyzer.text.AnalyzerLowerCase;
import org.omnaest.search.classic.domain.StringWord;
import org.omnaest.search.classic.internal.CodeSequenceStatisticsCollector.MatchingTokenAndStatistics;

public class TextSearchIndexTest
{

	@SuppressWarnings("unchecked")
	@Test
	public void testAnalyze() throws Exception
	{
		TextSearchIndex<Integer> searchIndex = new TextSearchIndex<Integer>()	.addAnalyzer(new AnalyzerExact<>())
																				.addAnalyzerChain(new AnalyzerBackNGram<>(), new AnalyzerFrontNGram<>());

		int reference = 1;
		Stream<StringWord> words = Arrays	.asList("abc", "adefg")
											.stream()
											.map(text -> new StringWord(text));
		searchIndex.analyze(words, reference);

		StringWord matchWord = new StringWord("def");
		Stream<MatchingTokenAndStatistics<Character, Integer>> matches = searchIndex.matcher()
																					.match(matchWord);

		boolean anyMatch = matches	.map(match -> new StringWord(match	.getToken()
																		.getCodeSequence()))
									.anyMatch(word -> word	.toString()
															.equals("def"));

		assertTrue(anyMatch);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAnalyzeText() throws Exception
	{
		TextSearchIndex<Integer> searchIndex = new TextSearchIndex<Integer>().addAnalyzerChain(	new AnalyzerLowerCase(), new AnalyzerBackNGram<>(),
																								new AnalyzerFrontNGram<>());

		int reference = 1;
		Stream<StringWord> words = Arrays	.asList("abc", "aDeFg")
											.stream()
											.map(text -> new StringWord(text));
		searchIndex.analyze(words, reference);

		StringWord matchWord = new StringWord("def");
		Stream<MatchingTokenAndStatistics<Character, Integer>> matches = searchIndex.matcher()
																					.match(matchWord);

		boolean anyMatch = matches	.map(match -> new StringWord(match	.getToken()
																		.getCodeSequence()))
									.anyMatch(word -> word	.toString()
															.equals("def"));

		assertTrue(anyMatch);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testJoin() throws Exception
	{
		TextSearchIndex<Integer> searchIndexLeft = new TextSearchIndex<Integer>()	.addAnalyzer(new AnalyzerExact<>())
																					.addAnalyzerChain(new AnalyzerBackNGram<>(), new AnalyzerFrontNGram<>());

		TextSearchIndex<Integer> searchIndexRight = new TextSearchIndex<Integer>()	.addAnalyzer(new AnalyzerExact<>())
																					.addAnalyzerChain(new AnalyzerBackNGram<>(), new AnalyzerFrontNGram<>());

		int reference = 1;
		Stream<StringWord> wordsLeft = Arrays	.asList("abc", "def")
												.stream()
												.map(text -> new StringWord(text));
		Stream<StringWord> wordsRight = Arrays	.asList("klm", "def")
												.stream()
												.map(text -> new StringWord(text));
		searchIndexLeft.analyze(wordsLeft, reference);
		searchIndexRight.analyze(wordsRight, reference);

		searchIndexLeft	.join(searchIndexRight, AnalysisTypeBasic.EXACT)
						.anyMatch(joinMatch -> new StringWord(joinMatch	.getLeftMatchingTokenAndStatistics()
																		.getToken()
																		.getCodeSequence()).equals("def"));

	}

}
