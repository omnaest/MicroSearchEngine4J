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
import java.util.function.Predicate;

import org.omnaest.search.classic.analyzer.AnalyzerBackNGram;
import org.omnaest.search.classic.analyzer.AnalyzerChain;
import org.omnaest.search.classic.analyzer.AnalyzerExact;
import org.omnaest.search.classic.analyzer.AnalyzerFrontNGram;
import org.omnaest.search.classic.analyzer.AnalyzerInverse;
import org.omnaest.search.classic.analyzer.AnalyzerSingleDeletions;
import org.omnaest.search.classic.analyzer.AnalyzerSingleInsertions;
import org.omnaest.search.classic.utils.StreamUtils;
import org.omnaest.search.classic.utils.StreamModifier.UnaryStreamModifier;

/**
 * Helper for {@link CodeSequenceStatisticsCollector}s
 *
 * @see #builder()
 * @author Omnaest
 */
public class CodeSequenceStatisticsCollectorUtils
{

	protected static class CodeSequenceStatisticsCollectorBuilderImpl<C, R> implements CodeSequenceStatisticsCollectorBuilder<C, R>
	{
		private CodeSequenceStatisticsCollectorModifiable<C, R> collector = new CodeSequenceStatisticsCollectorMap<>();

		private UnaryStreamModifier<CodeAndPosition<C>> generalCodeSequenceModifier = stream -> stream;

		@SuppressWarnings("unchecked")
		@Override
		public <C, R> CodeSequenceStatisticsCollector<C, R> build()
		{
			this.prependDefaultAnalysers();
			return (CodeSequenceStatisticsCollector<C, R>) this.collector;
		}

		private void prependDefaultAnalysers()
		{
			this.collector.addAnalyzer(new AnalyzerExact<>());
		}

		@Override
		public CodeSequenceStatisticsCollectorBuilder<C, R> withFromBackAnalysis()
		{
			this.collector.addAnalyzer(new AnalyzerBackNGram<>());
			return this;
		}

		@Override
		public CodeSequenceStatisticsCollectorBuilder<C, R> withSubSequenceAnalysis()
		{
			SubSequenceAnalysisOptions<C> options = SubSequenceAnalysisOptions.NONE();
			return this.withSubSequenceAnalysis(options);
		}

		@Override
		public CodeSequenceStatisticsCollectorBuilder<C, R> withSubSequenceAnalysis(SubSequenceAnalysisOptions<C> options)
		{
			this.collector.addAnalyzer(new AnalyzerChain<>(new AnalyzerFrontNGram<>(), new AnalyzerBackNGram<>()));
			return this;
		}

		@Override
		public CodeSequenceStatisticsCollectorBuilder<C, R> withSequenceLimit(int limit)
		{
			this.generalCodeSequenceModifier = StreamUtils.concat(this.generalCodeSequenceModifier, stream -> stream.limit(limit));
			return this;
		}

		@Override
		public CodeSequenceStatisticsCollectorBuilder<C, R> withDeletions()
		{
			this.collector.addAnalyzer(new AnalyzerChain<>(new AnalyzerSingleDeletions<>(), new AnalyzerFrontNGram<>(), new AnalyzerBackNGram<>()));
			return this;
		}

		@Override
		public CodeSequenceStatisticsCollectorBuilder<C, R> withInsertions()
		{
			this.collector.addAnalyzer(new AnalyzerChain<>(new AnalyzerSingleInsertions<>(), new AnalyzerFrontNGram<>(), new AnalyzerBackNGram<>()));
			return this;
		}

		@Override
		public CodeSequenceStatisticsCollectorBuilder<C, R> withInverseSubSequenceAnalysis()
		{
			this.collector.addAnalyzer(new AnalyzerChain<>(new AnalyzerInverse<>(), new AnalyzerFrontNGram<>(), new AnalyzerBackNGram<>()));
			return this;
		}

	}

	/**
	 * @see SubSequenceAnalysisOptions#NONE()
	 * @author Omnaest
	 */
	public static class SubSequenceAnalysisOptions<C>
	{
		private Predicate<List<C>>						allowedCodeSequenceMatcher;
		private UnaryStreamModifier<CodeAndPosition<C>>	codeSequenceModifier;

		public Predicate<List<C>> getAllowedCodeSequenceMatcher()
		{
			return this.allowedCodeSequenceMatcher;
		}

		public boolean hasCodeSequenceModifier()
		{
			return this.codeSequenceModifier != null;
		}

		public UnaryStreamModifier<CodeAndPosition<C>> getCodeSequenceModifier()
		{
			return this.codeSequenceModifier;
		}

		public boolean hasSubCodeSequencePredicate()
		{
			return this.allowedCodeSequenceMatcher != null;
		}

		public SubSequenceAnalysisOptions<C> setAllowedCodeSequenceMatcher(Predicate<List<C>> subCodeSequencePredicate)
		{
			this.allowedCodeSequenceMatcher = subCodeSequencePredicate;
			return this;
		}

		public SubSequenceAnalysisOptions<C> setCodeSequenceModifier(UnaryStreamModifier<CodeAndPosition<C>> codeSequenceModifier)
		{

			this.codeSequenceModifier = codeSequenceModifier;
			return this;
		}

		/**
		 * Returns a representation of options with no effect
		 *
		 * @return
		 */
		public static <C> SubSequenceAnalysisOptions<C> NONE()
		{
			return null;
		}
	}

	/**
	 * @see CodeSequenceStatisticsCollector
	 * @author Omnaest
	 * @param <C>
	 * @param <R>
	 */
	public static interface CodeSequenceStatisticsCollectorBuilder<C, R>
	{
		/**
		 * Includes analysis from the backside of the code sequence.<br>
		 * <br>
		 * So for your analyzed sequence of "abcde" you get hits for searches of "d","de","cde",...
		 *
		 * @return
		 */
		CodeSequenceStatisticsCollectorBuilder<C, R> withFromBackAnalysis();

		/**
		 * Includes analysis of partial sequences.<br>
		 * <br>
		 * E.g. the analyzed sequence of "abcde" will match on partial sequences like "a","ab","abc",...,"b","bc","bcd",...
		 *
		 * @return
		 */
		CodeSequenceStatisticsCollectorBuilder<C, R> withSubSequenceAnalysis();

		/**
		 * Similar to {@link #withSubSequenceAnalysis()} allowing to specify further {@link SubSequenceAnalysisOptions}
		 *
		 * @param options
		 * @return
		 */
		CodeSequenceStatisticsCollectorBuilder<C, R> withSubSequenceAnalysis(SubSequenceAnalysisOptions<C> options);

		/**
		 * Returns a new {@link CodeSequenceStatisticsCollector} instance
		 *
		 * @return
		 */
		public <C, R> CodeSequenceStatisticsCollector<C, R> build();

		CodeSequenceStatisticsCollectorBuilder<C, R> withSequenceLimit(int limit);

		CodeSequenceStatisticsCollectorBuilder<C, R> withDeletions();

		CodeSequenceStatisticsCollectorBuilder<C, R> withInverseSubSequenceAnalysis();

		CodeSequenceStatisticsCollectorBuilder<C, R> withInsertions();
	}

	/**
	 * @see CodeSequenceStatisticsCollector
	 * @see CodeSequenceStatisticsCollectorBuilder
	 * @return
	 */
	public static <C, R> CodeSequenceStatisticsCollectorBuilder<C, R> builder()
	{
		return new CodeSequenceStatisticsCollectorBuilderImpl<>();
	}
}
