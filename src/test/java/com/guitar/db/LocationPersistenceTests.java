package com.guitar.db;

import java.util.List;
import java.lang.System;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.guitar.db.repository.LocationJpaRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.guitar.db.model.Location;
import com.guitar.db.repository.LocationRepository;

import static org.junit.Assert.*;

@ContextConfiguration(locations={"classpath:com/guitar/db/applicationTests-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class LocationPersistenceTests {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private LocationJpaRepository locationJpaRepository;

	@Test
	public void testJpaFindall(){
		List<Location> locations = locationJpaRepository.findAll();
		assertNotNull(locations);
	}

	@Test
	@Transactional
	public void testSaveAndGetAndDelete() throws Exception {
		Location location = new Location();
		location.setCountry("Canada");
		location.setState("British Columbia");
		location = locationJpaRepository.saveAndFlush(location);
		
		// clear the persistence context so we don't return the previously cached location object
		// this is a test only thing and normally doesn't need to be done in prod code
		entityManager.clear();

		Location otherLocation = locationJpaRepository.findOne(location.getId());
		assertEquals("Canada", otherLocation.getCountry());
		assertEquals("British Columbia", otherLocation.getState());
		
		//delete BC location now
		locationJpaRepository.delete(otherLocation);
	}

	@Test
	public void testFindWithLike() throws Exception {
		List<Location> locs1 = locationJpaRepository.findByIgnoreCaseStateLike("new%");
		assertEquals(4, locs1.size());

		Location locs7 = locationJpaRepository.findFirstByIgnoreCaseStateLike("new%");
		assertEquals("New Hampshire", locs7.getState());

		Location locs8 = locationJpaRepository.findTopByIgnoreCaseStateLike("new%");
		assertEquals("New Hampshire", locs8.getState());

		List<Location> locs3 = locationJpaRepository.findByStateStartingWith("New");
		assertEquals(4, locs3.size());

		List<Location> locs4 = locationJpaRepository.findByStateEndingWith("Jersey");
		assertEquals(1, locs4.size());
		assertEquals("New Jersey", locs4.get(0).getState());
		assertEquals("United States", locs4.get(0).getCountry());

		List<Location> locs5 = locationJpaRepository.findByStateContaining("Jerse");
		assertEquals(1, locs5.size());
		assertEquals("New Jersey", locs5.get(0).getState());
		assertEquals("United States", locs5.get(0).getCountry());

		List<Location> locs2 = locationJpaRepository.findByStateNotLikeOrderByStateAsc("New%");
		assertEquals(46, locs2.size());
		locs2.forEach((location)->{
			System.out.println(location.getState());
		});
		List<Location> locs9 = locationJpaRepository.findDistinctByStateNotLikeOrderByStateAsc("New%");
		assertEquals(46, locs9.size());
		locs9.forEach((location)->{
			System.out.println(location.getState());
		});
		List<Location> locs6 = locationJpaRepository.findByStateNotLikeOrderByStateDesc("New%");
		assertEquals(46, locs6.size());
		locs6.forEach((location)->{
			System.out.println(location.getState());
		});
	}

	@Test
	public void testFindWithOr() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateOrCountry("New Jersey","New Jersey");
		assertNotNull(locs);
		assertEquals(1, locs.size());
	}

	@Test
	public void testFindWithAnd() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateAndCountry("New Jersey","United States");
		assertNotNull(locs);
		assertEquals(1, locs.size());
	}

	@Test
	public void testFindWithIsEquals() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateIsOrCountryEquals("New Jersey","New Jersey");
		assertNotNull(locs);
		assertEquals(1, locs.size());
	}

	@Test
	public void testFindWithNot() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateNot("New Jersey");
		assertNotNull(locs);
		assertNotSame("New Jersey",locs.get(0).getState());
	}

	@Test
	@Transactional  //note this is needed because we will get a lazy load exception unless we are in a tx
	public void testFindWithChildren() throws Exception {
		Location arizona = locationJpaRepository.findOne(3L);
		assertEquals("United States", arizona.getCountry());
		assertEquals("Arizona", arizona.getState());
		
		assertEquals(1, arizona.getManufacturers().size());
		
		assertEquals("Fender Musical Instruments Corporation", arizona.getManufacturers().get(0).getName());
	}
}
