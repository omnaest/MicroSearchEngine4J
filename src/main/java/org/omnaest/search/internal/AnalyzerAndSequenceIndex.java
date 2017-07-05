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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.omnaest.search.analyzer.AnalysisType;
import org.omnaest.search.analyzer.Analyzer;

public class AnalyzerAndSequenceIndex<C, R>
{
	private Map<Analyzer<C>, SequenceIndex<C, R>>	analyzerToSequenceIndexMap	= new ConcurrentHashMap<>();
	private List<Analyzer<C>>						analyzers					= new ArrayList<>();

	public SequenceIndex<C, R> getIndex(Analyzer<C> analyzer)
	{
		return this.analyzerToSequenceIndexMap.computeIfAbsent(analyzer, key -> new SequenceIndex<>(analyzer));
	}

	public List<SequenceIndex<C, R>> getAllIndexes()
	{
		return new ArrayList<>(this.analyzerToSequenceIndexMap.values());
	}

	public AnalyzerAndSequenceIndex<C, R> addAnalyzer(Analyzer<C> analyzer)
	{
		this.analyzers.add(analyzer);
		return this;
	}

	public List<Analyzer<C>> getAnalyzers()
	{
		return this.analyzers;
	}

	public List<SequenceIndex<C, R>> getIndexesFor(AnalysisType analysisType)
	{
		return this.analyzerToSequenceIndexMap	.entrySet()
												.stream()
												.filter(entry -> analysisType == null || entry	.getKey()
																								.getAnalysisType()
																								.equals(analysisType))
												.map(entry -> entry.getValue())
												.collect(Collectors.toList());
	}

	public boolean hasAnalysisType(AnalysisType analysisType)
	{
		return this.analyzerToSequenceIndexMap	.keySet()
												.stream()
												.anyMatch(analyzer -> analyzer	.getAnalysisType()
																				.equals(analysisType));
	}
}
