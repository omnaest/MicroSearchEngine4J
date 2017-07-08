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

import org.omnaest.search.classic.analyzer.Analyzer;
import org.omnaest.search.classic.domain.StringWord;

public class TextSearchIndex<R> extends GenericSearchIndex<StringWord, Character, R>
{

	@Override
	public TextSearchIndex<R> addAnalyzer(Analyzer<Character> analyzer)
	{
		super.addAnalyzer(analyzer);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TextSearchIndex<R> addAnalyzerChain(Analyzer<Character>... analyzers)
	{
		super.addAnalyzerChain(analyzers);
		return this;
	}

}
