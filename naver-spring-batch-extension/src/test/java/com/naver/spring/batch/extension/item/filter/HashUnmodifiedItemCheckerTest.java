package com.naver.spring.batch.extension.item.filter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class HashUnmodifiedItemCheckerTest {

	@Mock
	private HashRepository hashRepository;

	@Test
	public void check() throws Exception {
		final List<ItemHash> storedItemHash = new ArrayList<>();

		Mockito.doAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			storedItemHash.addAll((List)args[0]);
			return null;
		}).when(hashRepository).saveItemHashes(Matchers.anyListOf(ItemHash.class));

		Mockito.doAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			String hashKey = (String)args[0];

			for (ItemHash itemHash : storedItemHash) {
				if (hashKey.equals(itemHash.getItemKey())) {
					return itemHash.getItemHash();
				}
			}

			return null;
		}).when(hashRepository).getHashValue(Matchers.anyString());

		HashUnmodifiedItemChecker<TestObj> checker = new HashUnmodifiedItemChecker<>();
		checker.setHashRepository(hashRepository);
		checker.setKeyPropertyNames(Arrays.asList("id"));

		checker.setIgnorePropertyNames(Arrays.asList("randomVal", "randomVal2"));
		checker.afterPropertiesSet();

		TestObj p1 = new TestObj();
		p1.setId(1);
		p1.setName("YK");
		p1.setAge(20);

		p1.setRandomVal(123);
		p1.setRandomVal2(678);

		TestObj p2 = new TestObj();
		p2.setId(1);
		p2.setName("YK");
		p2.setAge(20);

		p2.setRandomVal(345);
		p2.setRandomVal2(493);

		boolean p1UnModified = checker.check(p1);
		checker.afterChunk(null);

		boolean p2UnModified = checker.check(p2);
		checker.afterChunk(null);

		Assert.assertFalse("p1UnModified must be false", p1UnModified);
		Assert.assertTrue("p2UnModified must be true",p2UnModified);
	}
}