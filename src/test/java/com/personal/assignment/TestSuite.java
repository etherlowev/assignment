package com.personal.assignment;

import com.personal.assignment.service.ApprovalServiceTest;
import com.personal.assignment.service.DocumentServiceTest;
import com.personal.assignment.service.HistoryServiceTest;
import com.personal.assignment.service.ParallelServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    ApprovalServiceTest.class,
    DocumentServiceTest.class,
    HistoryServiceTest.class,
    ParallelServiceTest.class
})
public class TestSuite {
}
