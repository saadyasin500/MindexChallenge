package com.mindex.challenge.service.impl;

import com.mindex.challenge.controller.requests.CompensationRequest;
import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Finding employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        // populate employee object for each employee in the hierarchy since it's currently only stored to have ids
        Queue<Employee> queue = new LinkedList<>(); // use BFS to iterate through reporting tree
        List<Employee> employeeReports = new ArrayList<>();

        if(employee.getDirectReports() == null) {
            return employee;
        }

        for(Employee report: employee.getDirectReports()) {
            Employee fullReport = employeeRepository.findByEmployeeId(report.getEmployeeId()); // populate the full employee object
            employeeReports.add(fullReport);
            queue.add(fullReport);
        }

        employee.setDirectReports(employeeReports);

        while(!queue.isEmpty()) {
            Employee report = queue.poll();

            if(report.getDirectReports() != null) {
                List<Employee> subemployeeReports = new ArrayList<>();

                for(Employee subreport: report.getDirectReports()) {
                    Employee fullReport = employeeRepository.findByEmployeeId(subreport.getEmployeeId()); // populate the full employee object
                    subemployeeReports.add(fullReport);
                    queue.add(fullReport);
                }

                report.setDirectReports(subemployeeReports);
            }

        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure retrieveReportingStructure(String id) {
        LOG.debug("Generating reporting structure for employee with id [{}]", id);
        // find the employee
        Employee employee = this.read(id);

        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid employeeId: " + id);
        }

        int numberOfReports = 0;

        // count all the reports for the given employee
        numberOfReports = countReports(employee);

        // build the reporting structure for the employee
        ReportingStructure reportingStructure = new ReportingStructure();
        reportingStructure.setEmployee(employee);
        reportingStructure.setNumberOfReports(numberOfReports);

        return reportingStructure;
    }

    @Override
    public Compensation retrieveCompensation(String id) {
        // first check to see if it's a valid employee id
        if(!employeeRepository.existsByEmployeeId(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid employeeId: " + id);
        }

        Optional<Compensation> compensation = compensationRepository.findByEmployeeId(id);
        if(compensation.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No compensation info found for this employee");
        }
        return compensation.get();
    }

    @Override
    public Compensation saveCompensation(String id, CompensationRequest compensation) {
        // first check to see if it's a valid employee id
        if(!employeeRepository.existsByEmployeeId(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid employeeId: " + id);
        }

        if(compensation.getEffectiveDate() == null) {
            compensation.setEffectiveDate(LocalDate.now()); // if user doesn't provide effective date, assume it's today (and let the user know)
        }

        Compensation compensationToPersist = new Compensation();

        compensationToPersist.setEmployeeId(id);
        compensationToPersist.setSalary(compensation.getSalary());
        compensationToPersist.setEffectiveDate(compensation.getEffectiveDate());

        compensationRepository.insert(compensationToPersist);

        return compensationToPersist;
    }

    private int countReports(Employee employee) {
        int count = 0;
        if(employee.getDirectReports() == null || employee.getDirectReports().isEmpty()) {
            return count;
        }
        Queue<Employee> queue = new LinkedList<>(); // since it's a tree-like reporting structure, iterate using BFS.
                                                    // we could also use DFS, but that risks stack overflow for very large reporting structures

        queue.addAll(employee.getDirectReports());
        while(!queue.isEmpty()) {
            Employee report = queue.poll();
            count++;

            if(report.getDirectReports() != null && !report.getDirectReports().isEmpty()) {
                queue.addAll(report.getDirectReports());
            }
        }

        return count;
    }



}
