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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.omnaest.search.analyzer.AnalysisType;
import org.omnaest.search.analyzer.Analyzer;
import org.omnaest.search.analyzer.Token;

public class SequenceIndex<C, R>
{
	private Map<SequenceKey<C>, IndexEntry<C, R>>	sequenceKeyToEntryMap	= new ConcurrentHashMap<>();
	private Analyzer<C>								analyzer;

	public static class IndexEntry<C, R>
	{
		private AtomicInteger					count				= new AtomicInteger();
		private List<TokenAndReference<C, R>>	tokenAndReferences	= Collections.synchronizedList(new ArrayList<>());

		public IndexEntry<C, R> addTokenAndReference(Token<C> token, R reference)
		{
			this.count.incrementAndGet();
			this.tokenAndReferences.add(new TokenAndReference<>(token, reference));
			return this;
		}

		public int getCount()
		{
			return this.count.get();
		}

		public List<TokenAndReference<C, R>> getTokenAndReferences()
		{
			return this.tokenAndReferences;
		}

	}

	public static class TokenAndReference<C, R>
	{
		private Token<C>	token;
		private R			reference;

		public TokenAndReference(Token<C> token, R reference)
		{
			super();
			this.token = token;
			this.reference = reference;
		}

		public Token<C> getToken()
		{
			return this.token;
		}

		public R getReference()
		{
			return this.reference;
		}

	}

	public SequenceIndex(Analyzer<C> analyzer)
	{
		super();
		this.analyzer = analyzer;
	}

	public IndexEntry<C, R> getOrCreateEntry(SequenceKey<C> sequenceKey)
	{
		return this.sequenceKeyToEntryMap.computeIfAbsent(sequenceKey, sk -> new IndexEntry<>());
	}

	public int getCount()
	{
		return this.sequenceKeyToEntryMap.size();
	}

	public List<IndexEntry<C, R>> getEntries()
	{
		return new ArrayList<>(this.sequenceKeyToEntryMap.values());
	}

	public boolean contains(SequenceKey<C> sequenceKey)
	{
		return this.sequenceKeyToEntryMap.containsKey(sequenceKey);
	}

	public Stream<IndexEntry<C, R>> getEntries(SequenceKey<C> sequenceKey)
	{
		return this.analyzer.analyzeQuery(sequenceKey)
							.map(sequence -> this.sequenceKeyToEntryMap.get(sequence));
	}

	public AnalysisType getAnalysisType()
	{
		return this.analyzer.getAnalysisType();
	}

}
