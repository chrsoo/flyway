/**
 * Copyright 2010-2015 Axel Fontaine
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.osgi;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.flywaydb.osgi.OsgiServiceFilter;
import org.junit.Ignore;
import org.junit.Test;

public class OsgiServiceFilterTest {

	@Test
	public void test() {
		//@formtter:off
		assertEquals("(alpha=a)",
				OsgiServiceFilter.create()
						.property("alpha", "a")
						.build());
		assertEquals("(&(alpha=a)(beta=b))",
				OsgiServiceFilter.create()
						.and()
							.property("alpha", "a")
							.property("beta", "b")
						.end()
						.build());
		assertEquals("(&(alpha=a)(|(beta=b)(gamma=g)))",
				OsgiServiceFilter.create()
						.and()
							.property("alpha", "a")
							.or()
								.property("beta", "b")
								.property("gamma", "g")
							.end()
						.end()
						.build());
		assertEquals("(&(|(alpha=a)(beta=b))(|(gamma=g)(delta=d)))",
				OsgiServiceFilter.create()
						.and()
							.or()
								.property("alpha", "a")
								.property("beta", "b")
							.end()
							.or()
								.property("gamma", "g")
								.property("delta", "d")
							.end()
						.end()
						.build());
		//@formtter:on
	}

	@Test
	public void testWithProperties() {
		Properties props = new Properties();

		props.put("alpha", "a");
		props.put("beta", " b");
		props.put("gamma", "g ");

		//@formtter:off
		assertEquals("(alpha=a)",
				OsgiServiceFilter.with(props)
						.key("alpha")
						.build());

		assertEquals("(&(alpha=a)(beta=b))",
				OsgiServiceFilter.with(props)
						.and()
							.key("alpha")
							.key("beta")
						.end()
						.build());
		// @formtter:on
	}

	@Ignore("FIXME make sure to throw an exception when the operator is not present!")
	@Test(expected = IllegalStateException.class)
	public void missingOperator() {
		OsgiServiceFilter.create()
				.property("alpha", "a")
				.property("beta", "b")
				.build();

	}

	@Test(expected = IllegalStateException.class)
	public void missingEnd() {
		OsgiServiceFilter.create()
				.and()
				.build();

	}

}
