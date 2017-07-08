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
package org.omnaest.search.next;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.search.next.utils.SingletonContainer;

public class AdaptionIndex<C, R>
{
	private SingletonContainer<Group<C, R>>	groupSingletons	= new SingletonContainer<>();
	private SingletonContainer<Node<C, R>>	nodeSingletons	= new SingletonContainer<>();

	protected static class CodeAndPosition<C>
	{
		private C	code;
		private int	position;

		public CodeAndPosition(C code, int position)
		{
			super();
			this.code = code;
			this.position = position;
		}

		public C getCode()
		{
			return this.code;
		}

		public int getPosition()
		{
			return this.position;
		}

		@Override
		public String toString()
		{
			return "[" + this.code + "," + this.position + "]";
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.code == null) ? 0 : this.code.hashCode());
			result = prime * result + this.position;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (this.getClass() != obj.getClass())
				return false;
			CodeAndPosition<?> other = (CodeAndPosition<?>) obj;
			if (this.code == null)
			{
				if (other.code != null)
					return false;
			}
			else if (!this.code.equals(other.code))
				return false;
			if (this.position != other.position)
				return false;
			return true;
		}

	}

	protected static class LinkedCodeAndPosition<C>
	{
		private CodeAndPosition<C>			codeAndPosition;
		private LinkedCodeAndPosition<C>	previous;
		private LinkedCodeAndPosition<C>	next;

		public LinkedCodeAndPosition(CodeAndPosition<C> codeAndPosition)
		{
			super();
			this.codeAndPosition = codeAndPosition;
		}

		public CodeAndPosition<C> getCodeAndPosition()
		{
			return this.codeAndPosition;
		}

		public LinkedCodeAndPosition<C> getPrevious()
		{
			return this.previous;
		}

		public LinkedCodeAndPosition<C> getNext()
		{
			return this.next;
		}

		public LinkedCodeAndPosition<C> setPrevious(LinkedCodeAndPosition<C> previous)
		{
			this.previous = previous;
			return this;
		}

		public LinkedCodeAndPosition<C> setPreviousAndSetThisForItsNext(LinkedCodeAndPosition<C> previous)
		{
			this.previous = previous;
			if (previous != null)
			{
				previous.setNext(this);
			}
			return this;
		}

		public LinkedCodeAndPosition<C> setNext(LinkedCodeAndPosition<C> next)
		{
			this.next = next;
			return this;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.codeAndPosition == null) ? 0 : this.codeAndPosition.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (this.getClass() != obj.getClass())
				return false;
			LinkedCodeAndPosition<?> other = (LinkedCodeAndPosition<?>) obj;
			if (this.codeAndPosition == null)
			{
				if (other.codeAndPosition != null)
					return false;
			}
			else if (!this.codeAndPosition.equals(other.codeAndPosition))
				return false;
			return true;
		}

	}

	protected static class CodeSequenceAndReference<C, R>
	{
		private List<C>	codeSequence;
		private R		reference;

		public CodeSequenceAndReference(List<C> codeSequence, R reference)
		{
			super();
			this.codeSequence = codeSequence;
			this.reference = reference;
		}

		public List<C> getCodeSequence()
		{
			return this.codeSequence;
		}

		public R getReference()
		{
			return this.reference;
		}

	}

	protected static interface Group<C, R>
	{
		Set<Node<C, R>> getNodes();

		void addNode(Node<C, R> node);

	}

	protected static abstract class AbstractGroup<C, R> implements Group<C, R>
	{
		private Set<Node<C, R>> nodes = Collections.synchronizedSet(new HashSet<>());

		@Override
		public void addNode(Node<C, R> node)
		{
			this.nodes.add(node);
		}

		@Override
		public Set<Node<C, R>> getNodes()
		{
			return this.nodes;
		}
	}

	protected static class CodeSequenceGroup<C, R> extends AbstractGroup<C, R>
	{
		private List<C> codeSequence;

		public CodeSequenceGroup(List<C> codeSequence)
		{
			super();
			this.codeSequence = codeSequence;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.codeSequence == null) ? 0 : this.codeSequence.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (this.getClass() != obj.getClass())
				return false;
			CodeSequenceGroup<?, ?> other = (CodeSequenceGroup<?, ?>) obj;
			if (this.codeSequence == null)
			{
				if (other.codeSequence != null)
					return false;
			}
			else if (!this.codeSequence.equals(other.codeSequence))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return "CodeSequenceGroup [codeSequence=" + this.codeSequence + "]";
		}

	}

	protected static class LinkedCodeAndPositionSequence<C>
	{
		private LinkedCodeAndPosition<C>	startLinkedCodeAndPosition;
		private int							size;

		public LinkedCodeAndPositionSequence(LinkedCodeAndPosition<C> startLinkedCodeAndPosition, int size)
		{
			super();
			this.startLinkedCodeAndPosition = startLinkedCodeAndPosition;
			this.size = size;
		}

		public List<C> getCodeSequence()
		{
			return this	.getCodeAndPositionSequence()
						.stream()
						.map(codeAndPositon -> codeAndPositon.getCode())
						.collect(Collectors.toList());
		}

		public LinkedCodeAndPositionSequence<C> getLeftSideExpansion()
		{
			LinkedCodeAndPosition<C> previous = this.startLinkedCodeAndPosition.getPrevious();
			return previous != null ? new LinkedCodeAndPositionSequence<>(previous, this.size + 1) : null;
		}

		public LinkedCodeAndPositionSequence<C> getRightSideExpansion()
		{
			LinkedCodeAndPosition<C> rightSideLinkedCodeAndPosition = this.getRightSideLinkedCodeAndPosition();
			LinkedCodeAndPosition<C> next = rightSideLinkedCodeAndPosition != null ? rightSideLinkedCodeAndPosition.getNext() : null;
			return next != null ? new LinkedCodeAndPositionSequence<>(this.startLinkedCodeAndPosition, this.size + 1) : null;
		}

		private LinkedCodeAndPosition<C> getRightSideLinkedCodeAndPosition()
		{
			LinkedCodeAndPosition<C> retval = this.startLinkedCodeAndPosition;

			for (int ii = 0; ii < this.size - 1; ii++)
			{
				retval = retval != null ? retval.getNext() : null;
			}

			return retval;
		}

		public List<CodeAndPosition<C>> getCodeAndPositionSequence()
		{
			List<CodeAndPosition<C>> retlist = new ArrayList<>();

			LinkedCodeAndPosition<C> linkedCodeAndPosition = this.startLinkedCodeAndPosition;
			for (int ii = 0; ii < this.size; ii++)
			{
				CodeAndPosition<C> codeAndPosition = linkedCodeAndPosition.getCodeAndPosition();
				retlist.add(codeAndPosition);
				linkedCodeAndPosition = linkedCodeAndPosition.getNext();
			}
			return retlist;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + this.size;
			result = prime * result + ((this.startLinkedCodeAndPosition == null) ? 0 : this.startLinkedCodeAndPosition.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (this.getClass() != obj.getClass())
				return false;
			LinkedCodeAndPositionSequence<?> other = (LinkedCodeAndPositionSequence<?>) obj;
			if (this.size != other.size)
				return false;
			if (this.startLinkedCodeAndPosition == null)
			{
				if (other.startLinkedCodeAndPosition != null)
					return false;
			}
			else if (!this.startLinkedCodeAndPosition.equals(other.startLinkedCodeAndPosition))
				return false;
			return true;
		}

	}

	protected static class Node<C, R>
	{
		private LinkedCodeAndPositionSequence<C>	linkedCodeAndPositionSequence;
		private Set<Group<C, R>>					groups		= Collections.synchronizedSet(new HashSet<>());
		private List<Node<C, R>>					origins		= Collections.synchronizedList(new ArrayList<>());
		private List<Node<C, R>>					descendants	= Collections.synchronizedList(new ArrayList<>());
		private Set<R>								references	= Collections.synchronizedSet(new LinkedHashSet<>());

		private AtomicBoolean exploded = new AtomicBoolean(false);

		public static interface ExplodeOperation
		{
			void explode();
		}

		public Node(LinkedCodeAndPositionSequence<C> linkedCodeAndPositionSequence)
		{
			super();
			this.linkedCodeAndPositionSequence = linkedCodeAndPositionSequence;
		}

		public boolean isExploded()
		{
			return this.exploded.get();
		}

		public void explodeAndExecuteIfNotExploded(ExplodeOperation operation)
		{
			if (!this.exploded.getAndSet(true))
			{
				operation.explode();
			}
		}

		public Set<Group<C, R>> getGroups()
		{
			return this.groups;
		}

		public List<Node<C, R>> getOrigins()
		{
			return this.origins;
		}

		public List<Node<C, R>> getDescendants()
		{
			return this.descendants;
		}

		public Set<R> getReferences()
		{
			return this.references;
		}

		public LinkedCodeAndPositionSequence<C> getLinkedCodeAndPositionSequence()
		{
			return this.linkedCodeAndPositionSequence;
		}

		/**
		 * Returns true, if the {@link Group} was not present already and was added, otherwise false
		 *
		 * @param group
		 * @return
		 */
		public boolean addGroup(Group<C, R> group)
		{
			return this.groups.add(group);
		}

		@Override
		public String toString()
		{
			return "Node [" + this	.getLinkedCodeAndPositionSequence()
									.getCodeAndPositionSequence()
					+ "]";
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.linkedCodeAndPositionSequence == null) ? 0 : this.linkedCodeAndPositionSequence.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (this.getClass() != obj.getClass())
				return false;
			Node<?, ?> other = (Node<?, ?>) obj;
			if (this.linkedCodeAndPositionSequence == null)
			{
				if (other.linkedCodeAndPositionSequence != null)
					return false;
			}
			else if (!this.linkedCodeAndPositionSequence.equals(other.linkedCodeAndPositionSequence))
				return false;
			return true;
		}

	}

	protected static interface NodeOperator<C, R>
	{
		int getOccurenceNumber();

		boolean isExploded();

		void explode();

		boolean hasGroup();

		void generateAndLinkToGroup();
	}

	public void analyze(Stream<C> codeSequence, R reference)
	{
		if (codeSequence != null)
		{
			CodeSequenceAndReference<C, R> codeSequenceAndReference = new CodeSequenceAndReference<>(codeSequence.collect(Collectors.toList()), reference);

			AtomicInteger position = new AtomicInteger();
			AtomicReference<LinkedCodeAndPosition<C>> previous = new AtomicReference<>();
			this.analyze(codeSequenceAndReference	.getCodeSequence()
													.stream()
													.map(code -> new CodeAndPosition<>(code, position.getAndIncrement()))
													.map(codeAndPosition -> new LinkedCodeAndPosition<>(codeAndPosition).setPreviousAndSetThisForItsNext(previous.get()))
													.peek(linkedCodeAndPosition -> previous.set(linkedCodeAndPosition))
													.map(linkedCodeAndPosition -> new LinkedCodeAndPositionSequence<>(linkedCodeAndPosition, 1))
													.map(linkedCodeAndPositionSequence -> this.createOrGetSingletonNode(linkedCodeAndPositionSequence)));
		}
	}

	private Node<C, R> createOrGetSingletonNode(LinkedCodeAndPositionSequence<C> linkedCodeAndPositionSequence)
	{
		return this.nodeSingletons.returnAsSingleton(new Node<>(linkedCodeAndPositionSequence));
	}

	protected void analyze(Stream<Node<C, R>> nodes)
	{
		if (nodes != null)
		{
			nodes.forEach(this::analyze);
		}
	}

	protected void analyze(Node<C, R> node)
	{
		NodeOperator<C, R> nodeOperator = this.wrapNodeInOperator(node);

		if (!nodeOperator.hasGroup())
		{
			nodeOperator.generateAndLinkToGroup();
		}

		int occurences = nodeOperator.getOccurenceNumber();
		if (occurences > 1)
		{
			boolean exploded = nodeOperator.isExploded();
			if (!exploded)
			{
				nodeOperator.explode();
			}
		}
	}

	private NodeOperator<C, R> wrapNodeInOperator(Node<C, R> node)
	{
		NodeOperator<C, R> nodeOperator = new NodeOperator<C, R>()
		{
			@Override
			public int getOccurenceNumber()
			{
				return node	.getGroups()
							.stream()
							.map(group -> group	.getNodes()
												.size())
							.mapToInt(occurence -> occurence)
							.max()
							.orElseGet(() -> 0);
			}

			@Override
			public boolean isExploded()
			{
				return node.isExploded();
			}

			@Override
			public void explode()
			{
				node.explodeAndExecuteIfNotExploded(() ->
				{
					LinkedCodeAndPositionSequence<C> linkedCodeAndPositionSequence = node.getLinkedCodeAndPositionSequence();
					LinkedCodeAndPositionSequence<C> leftSideExpansion = linkedCodeAndPositionSequence.getLeftSideExpansion();
					LinkedCodeAndPositionSequence<C> rightSideExpansion = linkedCodeAndPositionSequence.getRightSideExpansion();

					if (leftSideExpansion != null)
					{
						Node<C, R> leftSideNode = AdaptionIndex.this.createOrGetSingletonNode(leftSideExpansion);
						AdaptionIndex.this.analyze(leftSideNode);
					}
					if (rightSideExpansion != null)
					{
						Node<C, R> rightSideNode = rightSideExpansion != null ? AdaptionIndex.this.createOrGetSingletonNode(rightSideExpansion) : null;
						AdaptionIndex.this.analyze(rightSideNode);
					}

					node.getGroups()
						.stream()
						.flatMap(group -> group	.getNodes()
												.stream())
						.map(node -> AdaptionIndex.this.wrapNodeInOperator(node))
						.forEach(nodeOperator -> nodeOperator.explode());
				});
			}

			@Override
			public boolean hasGroup()
			{
				return !node.getGroups()
							.isEmpty();
			}

			@Override
			public void generateAndLinkToGroup()
			{
				if (!this.hasGroup())
				{
					//
					Group<C, R> group = new CodeSequenceGroup<>(node.getLinkedCodeAndPositionSequence()
																	.getCodeSequence());
					group = AdaptionIndex.this.groupSingletons.returnAsSingleton(group);

					//
					boolean added = node.addGroup(group);
					if (added)
					{
						group.addNode(node);
					}
				}
			}
		};
		return nodeOperator;
	}

	public SingletonContainer<Group<C, R>> getGroupSingletons()
	{
		return this.groupSingletons;
	}

	public static interface MatchGroup<C, R>
	{
		Object getGroup();

		Stream<MatchNode<C, R>> getNodes();

		int getOccurrenceNumber();
	}

	private static class MatchGroupImpl<C, R> implements MatchGroup<C, R>
	{
		private Group<C, R> group;

		public MatchGroupImpl(Group<C, R> group)
		{
			super();
			this.group = group;
		}

		@Override
		public Stream<MatchNode<C, R>> getNodes()
		{
			return this.group	.getNodes()
								.stream()
								.map(node -> new MatchNodeImpl<>(node));
		}

		@Override
		public Object getGroup()
		{
			return "" + this.group;
		}

		@Override
		public String toString()
		{
			return "[" + this.getGroup() + ", occurrences=" + this.getOccurrenceNumber() + "]";
		}

		@Override
		public int getOccurrenceNumber()
		{
			return this.group	.getNodes()
								.size();
		}

	}

	public static interface MatchNode<C, R>
	{

	}

	private static class MatchNodeImpl<C, R> implements MatchNode<C, R>
	{
		private Node<C, R> node;

		public MatchNodeImpl(Node<C, R> node)
		{
			super();
			this.node = node;
		}

		@Override
		public String toString()
		{
			return "[" + this.node + "]";
		}

	}

	public Stream<MatchGroup<C, R>> extractGroups()
	{
		return this.groupSingletons	.getElements()
									.map(group -> new MatchGroupImpl<>(group));
	}
}
