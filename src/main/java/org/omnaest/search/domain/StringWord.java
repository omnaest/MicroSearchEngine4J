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
package org.omnaest.search.domain;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class StringWord extends Word<Character>
{
	public StringWord(String text)
	{
		this(ArrayUtils.toObject(text.toCharArray()));
	}

	public StringWord(Character... codeSequence)
	{
		super(codeSequence);
	}

	public StringWord(List<Character> codeSequence)
	{
		super(codeSequence);
	}

	@Override
	public String toString()
	{
		return StringUtils.join(this.getCodeSequence(), "");
	}

}
