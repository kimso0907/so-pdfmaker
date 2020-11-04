package com.sooo.savingwallet.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sooo.pdfmaker.config.RootConfig;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= {RootConfig.class})
@Log4j
public class mapperTests {

//	@Setter(onMethod_ = {@Autowired})
//	private BudgetService mapper;
//
//	@Test
//	public void testGetBudgetClass() {
//		log.info("^-^");
//		log.info("^-^" + mapper.getBudgetClass());
//		log.info("^-^");
//	}
}
