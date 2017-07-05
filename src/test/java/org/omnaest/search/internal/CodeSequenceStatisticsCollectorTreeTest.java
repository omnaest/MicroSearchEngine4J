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

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.omnaest.search.internal.CodeSequenceStatisticsCollector;
import org.omnaest.search.internal.CodeSequenceStatisticsCollectorMap;

public class CodeSequenceStatisticsCollectorTreeTest
{
	private CodeSequenceStatisticsCollector<Character, String> structureStatisticsTree = new CodeSequenceStatisticsCollectorMap<>();

	@Test
	public void test()
	{
		List<String> codeSequences = Arrays.asList("abcdefghij", "abcdekghij", "abcdekghij");
		for (String codeSequence : codeSequences)
		{
			this.structureStatisticsTree.analyze(toCodeSequence(codeSequence), codeSequence);
		}

		System.out.println(this.structureStatisticsTree);
	}

	private static List<Character> toCodeSequence(String rawCodeSequence)
	{
		return Arrays.asList(ArrayUtils.toObject(rawCodeSequence.toCharArray()));
	}
}
