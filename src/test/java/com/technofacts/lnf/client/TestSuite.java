package com.technofacts.lnf.client;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.springframework.test.context.ContextConfiguration;

@Suite
@SelectPackages("com.technofacts.lnf.client.controller")
@ContextConfiguration(classes = Application.class)
public class TestSuite {

}

