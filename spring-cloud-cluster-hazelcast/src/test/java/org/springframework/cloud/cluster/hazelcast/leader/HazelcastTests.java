/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.cluster.hazelcast.leader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import org.junit.Test;

import org.springframework.cloud.cluster.leader.Context;
import org.springframework.cloud.cluster.leader.DefaultCandidate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tests for hazelcast leader election.
 *
 * @author Janne Valkealahti
 * @author Patrick Peralta
 *
 */
public class HazelcastTests {

	@Test
	public void testSimpleLeader() throws InterruptedException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
				Config1.class);
		TestCandidate candidate = ctx.getBean(TestCandidate.class);
		assertThat(candidate.onGrantedLatch.await(5, TimeUnit.SECONDS), is(true));
		ctx.close();
	}

	@Configuration
	static class Config1 {

		@Bean
		public TestCandidate candidate() {
			return new TestCandidate();
		}

		@Bean
		public HazelcastInstance hazelcastInstance() {
			return Hazelcast.newHazelcastInstance();
		}

		@Bean
		public LeaderInitiator initiator() {
			return new LeaderInitiator(hazelcastInstance(), candidate());
		}

	}

	static class TestCandidate extends DefaultCandidate {

		CountDownLatch onGrantedLatch = new CountDownLatch(1);

		@Override
		public void onGranted(Context ctx) {
			onGrantedLatch.countDown();
			super.onGranted(ctx);
		}

	}

}
