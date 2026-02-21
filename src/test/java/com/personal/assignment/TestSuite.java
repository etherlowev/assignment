package com.personal.assignment;

import com.personal.assignment.service.ApprovalServiceTest;
import com.personal.assignment.service.DocumentServiceTest;
import com.personal.assignment.service.HistoryServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    ApprovalServiceTest.class,
    DocumentServiceTest.class,
    HistoryServiceTest.class,
})
public class TestSuite {
}
