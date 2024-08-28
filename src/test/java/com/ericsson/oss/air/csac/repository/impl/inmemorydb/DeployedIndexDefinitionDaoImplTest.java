/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.inmemorydb;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeployedIndexDefinitionDaoImplTest {

    private DeployedIndexDefinitionDaoImpl testDao;

    @BeforeEach
    void setUp() throws Exception {
        this.testDao = new DeployedIndexDefinitionDaoImpl();
    }

    @Test
    void stream() {

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();
        final DeployedIndexDefinitionDto dto2 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto2").build();

        this.testDao.saveAll(List.of(dto1, dto2));

        assertEquals(2, this.testDao.stream().count());
        assertEquals(dto1, this.testDao.stream().filter(d -> d.indexDefinitionName().equals("dto1")).findFirst().get());
        assertEquals(dto2, this.testDao.stream().filter(d -> d.indexDefinitionName().equals("dto2")).findFirst().get());
    }

    @Test
    void save() {

        assertEquals(0, this.testDao.count());

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();

        this.testDao.save(dto1);

        assertEquals(1, this.testDao.count());
    }

    @Test
    void saveAll() {

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();
        final DeployedIndexDefinitionDto dto2 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto2").build();

        this.testDao.saveAll(List.of(dto1, dto2));

        assertEquals(2, this.testDao.count());
    }

    @Test
    void findById() {

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();
        final DeployedIndexDefinitionDto dto2 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto2").build();

        this.testDao.saveAll(List.of(dto1, dto2));

        assertTrue(this.testDao.findById("dto1").isPresent());

    }

    @Test
    void existsById() {

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();
        final DeployedIndexDefinitionDto dto2 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto2").build();

        this.testDao.saveAll(List.of(dto1, dto2));

        assertTrue(this.testDao.existsById("dto1"));
    }

    @Test
    void findAll() {

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();
        final DeployedIndexDefinitionDto dto2 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto2").build();

        this.testDao.saveAll(List.of(dto1, dto2));

        final List<DeployedIndexDefinitionDto> actual = new ArrayList<>();
        this.testDao.findAll().forEach(actual::add);

        assertEquals(2, actual.size());
    }

    @Test
    void findAllById() {

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();
        final DeployedIndexDefinitionDto dto2 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto2").build();
        final DeployedIndexDefinitionDto dto3 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto3").build();
        final DeployedIndexDefinitionDto dto4 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto4").build();

        this.testDao.saveAll(List.of(dto1, dto2, dto3, dto4));

        final List<DeployedIndexDefinitionDto> actual = new ArrayList<>();
        this.testDao.findAllById(List.of("dto2", "dto4")).forEach(actual::add);

        assertEquals(2, actual.size());

        assertTrue(actual.containsAll(List.of(dto2, dto4)));
    }

    @Test
    void count() {

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();
        final DeployedIndexDefinitionDto dto2 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto2").build();
        final DeployedIndexDefinitionDto dto3 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto3").build();
        final DeployedIndexDefinitionDto dto4 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto4").build();

        this.testDao.saveAll(List.of(dto1, dto2, dto3, dto4));

        assertEquals(4, this.testDao.count());
    }

    @Test
    void deleteById() {

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();
        final DeployedIndexDefinitionDto dto2 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto2").build();
        final DeployedIndexDefinitionDto dto3 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto3").build();
        final DeployedIndexDefinitionDto dto4 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto4").build();

        this.testDao.saveAll(List.of(dto1, dto2, dto3, dto4));

        assertTrue(this.testDao.existsById("dto3"));

        this.testDao.deleteById("dto3");

        assertFalse(this.testDao.existsById("dto3"));
    }

    @Test
    void delete() {

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();
        final DeployedIndexDefinitionDto dto2 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto2").build();
        final DeployedIndexDefinitionDto dto3 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto3").build();
        final DeployedIndexDefinitionDto dto4 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto4").build();

        this.testDao.saveAll(List.of(dto1, dto2, dto3, dto4));

        assertTrue(this.testDao.existsById("dto3"));

        this.testDao.delete(dto3);

        assertFalse(this.testDao.existsById("dto3"));
    }

    @Test
    void deleteAllById() {

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();
        final DeployedIndexDefinitionDto dto2 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto2").build();
        final DeployedIndexDefinitionDto dto3 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto3").build();
        final DeployedIndexDefinitionDto dto4 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto4").build();

        this.testDao.saveAll(List.of(dto1, dto2, dto3, dto4));

        assertTrue(this.testDao.existsById("dto1"));
        assertTrue(this.testDao.existsById("dto3"));

        this.testDao.deleteAllById(List.of("dto1", "dto3"));

        assertFalse(this.testDao.existsById("dto1"));
        assertFalse(this.testDao.existsById("dto3"));
    }

    @Test
    void deleteAll() {

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();
        final DeployedIndexDefinitionDto dto2 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto2").build();
        final DeployedIndexDefinitionDto dto3 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto3").build();
        final DeployedIndexDefinitionDto dto4 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto4").build();

        this.testDao.saveAll(List.of(dto1, dto2, dto3, dto4));

        assertTrue(this.testDao.existsById("dto1"));
        assertTrue(this.testDao.existsById("dto2"));
        assertTrue(this.testDao.existsById("dto3"));
        assertTrue(this.testDao.existsById("dto4"));

        assertEquals(4, this.testDao.count());

        this.testDao.deleteAll();

        assertEquals(0, this.testDao.count());
    }

    @Test
    void testDeleteAll() {

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();
        final DeployedIndexDefinitionDto dto2 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto2").build();
        final DeployedIndexDefinitionDto dto3 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto3").build();
        final DeployedIndexDefinitionDto dto4 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto4").build();

        this.testDao.saveAll(List.of(dto1, dto2, dto3, dto4));

        assertTrue(this.testDao.existsById("dto1"));
        assertTrue(this.testDao.existsById("dto2"));
        assertTrue(this.testDao.existsById("dto3"));
        assertTrue(this.testDao.existsById("dto4"));

        assertEquals(4, this.testDao.count());

        this.testDao.deleteAll(List.of(dto1, dto3));

        assertEquals(2, this.testDao.count());

        assertFalse(this.testDao.existsById("dto1"));
        assertTrue(this.testDao.existsById("dto2"));
        assertFalse(this.testDao.existsById("dto3"));
        assertTrue(this.testDao.existsById("dto4"));
    }

    @Test
    void clear() {

        final DeployedIndexDefinitionDto dto1 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();
        final DeployedIndexDefinitionDto dto2 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto2").build();
        final DeployedIndexDefinitionDto dto3 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto3").build();
        final DeployedIndexDefinitionDto dto4 = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto4").build();

        this.testDao.saveAll(List.of(dto1, dto2, dto3, dto4));

        assertEquals(4, this.testDao.count());

        this.testDao.clear();

        assertEquals(0, this.testDao.count());
    }
}