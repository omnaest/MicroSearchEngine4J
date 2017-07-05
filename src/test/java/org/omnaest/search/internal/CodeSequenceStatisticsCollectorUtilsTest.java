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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.omnaest.search.internal.CodeSequenceStatisticsCollector;
import org.omnaest.search.internal.CodeSequenceStatisticsCollector.Matcher;

public class CodeSequenceStatisticsCollectorUtilsTest
{

	@Test
	public void testBuilder() throws Exception
	{
		CodeSequenceStatisticsCollector<Character, Void> codeSequenceStatisticsCollector = CodeSequenceStatisticsCollector	.builder()
																															.withSequenceLimit(50)
																															.withFromBackAnalysis()
																															.withSubSequenceAnalysis()
																															.withDeletions()
																															.build();

		List<Character> codeSequence = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h');
		codeSequenceStatisticsCollector.analyze(codeSequence, null);

		Matcher<Character, Void> matcher = codeSequenceStatisticsCollector.matcher();

		assertEquals(3, matcher.matchingDepth(Arrays.asList('a', 'b', 'c')));
		assertEquals(4, matcher.matchingDepth(Arrays.asList('a', 'b', 'c', 'd')));
		assertEquals(3, matcher.matchingDepth(Arrays.asList('a', 'b', 'c', 'x')));

		assertEquals(3, matcher.matchingDepth(Arrays.asList('b', 'c', 'd')));
		assertEquals(3, matcher.matchingDepth(Arrays.asList('f', 'g', 'h')));
		assertEquals(3, matcher.matchingDepth(Arrays.asList('d', 'f', 'g')));
	}

	@Test
	public void testBuilderInverse() throws Exception
	{
		CodeSequenceStatisticsCollector<Character, Void> codeSequenceStatisticsCollector = CodeSequenceStatisticsCollector	.builder()
																															.withSequenceLimit(50)
																															.withInverseSubSequenceAnalysis()
																															.build();

		List<Character> codeSequence = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h');
		codeSequenceStatisticsCollector.analyze(codeSequence, null);

		Matcher<Character, Void> matcher = codeSequenceStatisticsCollector.matcher();

		assertEquals(3, matcher.matchingDepth(Arrays.asList('c', 'b', 'a')));
		assertEquals(3, matcher.matchingDepth(Arrays.asList('d', 'c', 'b')));
		assertEquals(3, matcher.matchingDepth(Arrays.asList('h', 'g', 'f')));

	}

}
