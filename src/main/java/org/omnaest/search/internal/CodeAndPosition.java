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

public class CodeAndPosition<C>
{
	private C		code;
	private int		position;
	private boolean	terminal;
	private boolean	start;

	public CodeAndPosition(C code, int position, boolean terminal, boolean start)
	{
		super();
		this.code = code;
		this.position = position;
		this.terminal = terminal;
		this.start = start;
	}

	public C getCode()
	{
		return this.code;
	}

	public int getPosition()
	{
		return this.position;
	}

	/**
	 * Returns true, if the corresponding code is the last of the given code sequence
	 *
	 * @return
	 */
	public boolean isTerminal()
	{
		return this.terminal;
	}

	public boolean isStart()
	{
		return this.start;
	}

	@Override
	public String toString()
	{
		return "[code=" + this.code + ", position=" + this.position + "]";
	}

}