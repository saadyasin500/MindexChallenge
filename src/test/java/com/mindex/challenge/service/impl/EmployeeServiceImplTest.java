package com.mindex.challenge.service.impl;

import com.mindex.challenge.controller.requests.CompensationRequest;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String employeeIdReportsUrl;
    private String employeeCompensationUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        employeeIdReportsUrl = "http://localhost:" + port + "/employee/{id}/reporting-structure";
        employeeCompensationUrl = "http://localhost:" + port + "/employee/{id}/compensation";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");


        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    @Test
    public void testReportingStructure(){
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        List<Employee> reports = new ArrayList<>();

        Employee report1 = new Employee();
        report1.setFirstName("Daniel");
        report1.setLastName("Mars");
        report1.setDepartment("Engineering");
        report1.setPosition("Developer");

        Employee report2 = new Employee();
        report1.setFirstName("Jack");
        report1.setLastName("Hamlin");
        report1.setDepartment("Engineering");
        report1.setPosition("Developer");

        Employee createdReport1 = restTemplate.postForEntity(employeeUrl, report1, Employee.class).getBody();

        Employee createdReport2 = restTemplate.postForEntity(employeeUrl, report2, Employee.class).getBody();

        reports.add(createdReport1); reports.add(createdReport2);

        testEmployee.setDirectReports(reports);

        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        ReportingStructure reportingStructure =
                restTemplate.getForEntity(employeeIdReportsUrl, ReportingStructure.class, createdEmployee.getEmployeeId()).getBody();

        assertEquals(2, reportingStructure.getNumberOfReports());
        assertNotNull(reportingStructure.getEmployee());
    }

    @Test
    public void testCompensation() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        CompensationRequest request = new CompensationRequest();
        request.setSalary(BigDecimal.valueOf(1));

        Compensation persistedCompensation = restTemplate.postForEntity(employeeCompensationUrl, request, Compensation.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(request.getSalary(), persistedCompensation.getSalary());
        assertEquals(LocalDate.now(), persistedCompensation.getEffectiveDate());
        assertEquals(createdEmployee.getEmployeeId(), persistedCompensation.getEmployeeId());

        Compensation compensationRetrievalResponse = restTemplate.getForEntity(employeeCompensationUrl, Compensation.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(persistedCompensation.getSalary(), compensationRetrievalResponse.getSalary());
        assertEquals(persistedCompensation.getEffectiveDate(), compensationRetrievalResponse.getEffectiveDate());
        assertEquals(persistedCompensation.getEmployeeId(), compensationRetrievalResponse.getEmployeeId());
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
