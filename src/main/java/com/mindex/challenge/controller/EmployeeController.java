package com.mindex.challenge.controller;

import com.mindex.challenge.controller.requests.CompensationRequest;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {
    private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/employee")
    public Employee create(@RequestBody Employee employee) {
        LOG.debug("Received employee create request for [{}]", employee);

        return employeeService.create(employee);
    }

    @GetMapping("/employee/{id}")
    public Employee read(@PathVariable String id) {
        LOG.debug("Received employee get request for id [{}]", id);

        return employeeService.read(id);
    }

    @PutMapping("/employee/{id}")
    public Employee update(@PathVariable String id, @RequestBody Employee employee) {
        LOG.debug("Received employee update request for id [{}] and employee [{}]", id, employee);

        employee.setEmployeeId(id);
        return employeeService.update(employee);
    }

    @GetMapping("/employee/{id}/reporting-structure")
    public ReportingStructure getReportingStructure(@PathVariable String id) {
        LOG.debug("Received request to retrieve reporting structure for employee with id [{}]", id);

        return employeeService.retrieveReportingStructure(id);
    }

    @GetMapping("/employee/{id}/compensation")
    public Compensation getCompensation(@PathVariable String id) {
        LOG.debug("Received request to retrieve compensation info for employee with id [{}]", id);

        return employeeService.retrieveCompensation(id);
    }

    @PostMapping("/employee/{id}/compensation")
    public Compensation saveCompensation(@PathVariable String id, @RequestBody @Valid CompensationRequest compensation) {
        LOG.debug("Received request to save compensation info for employee with id [{}]", id);

        return employeeService.saveCompensation(id, compensation);
    }
}
