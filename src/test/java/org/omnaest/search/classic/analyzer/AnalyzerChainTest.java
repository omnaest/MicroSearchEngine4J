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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.omnaest.search.classic.analyzer.AnalyzerBackNGram;
import org.omnaest.search.classic.analyzer.AnalyzerChain;
import org.omnaest.search.classic.analyzer.AnalyzerFrontNGram;
import org.omnaest.search.classic.analyzer.Token;
import org.omnaest.search.classic.internal.CodeAndPosition;
import org.omnaest.search.classic.utils.StringUtils;

public class AnalyzerChainTest
{
	@Test
	public void testAnalyze() throws Exception
	{
		Stream<CodeAndPosition<String>> codeAndPositionStream = StringUtils	.toCharacterList("abcdefg")
																			.stream()
																			.map(e -> new CodeAndPosition<>(e.toString(), 0, true, true));
		AnalyzerChain<String> analyzer = new AnalyzerChain<>(Arrays.asList(new AnalyzerFrontNGram<>(), new AnalyzerBackNGram<>()));
		Stream<Token<String>> tokens = analyzer.analyze(codeAndPositionStream);

		Iterator<String> iterator = tokens	.map(t -> t	.getCodeSequence()
														.stream()
														.collect(Collectors.joining()))
											.iterator();
		assertTrue(iterator.hasNext());
		assertEquals("abcdefg", iterator.next());
		assertEquals("bcdefg", iterator.next());
		assertEquals("cdefg", iterator.next());
		assertEquals("defg", iterator.next());
		assertEquals("efg", iterator.next());
		assertEquals("fg", iterator.next());
		assertEquals("g", iterator.next());

		assertEquals("abcdef", iterator.next());
		assertEquals("bcdef", iterator.next());
		assertEquals("cdef", iterator.next());
		assertEquals("def", iterator.next());
		assertEquals("ef", iterator.next());
		assertEquals("f", iterator.next());

		assertEquals("abcde", iterator.next());
		assertEquals("bcde", iterator.next());
		assertEquals("cde", iterator.next());
		assertEquals("de", iterator.next());
		assertEquals("e", iterator.next());

		assertEquals("abcd", iterator.next());
		assertEquals("bcd", iterator.next());
		assertEquals("cd", iterator.next());
		assertEquals("d", iterator.next());

		assertEquals("abc", iterator.next());
		assertEquals("bc", iterator.next());
		assertEquals("c", iterator.next());

		assertEquals("ab", iterator.next());
		assertEquals("b", iterator.next());

		assertEquals("a", iterator.next());

		assertFalse(iterator.hasNext());
	}

}
