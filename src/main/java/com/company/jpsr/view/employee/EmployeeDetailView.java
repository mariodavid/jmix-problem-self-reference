package com.company.jpsr.view.employee;

import com.company.jpsr.entity.Employee;

import com.company.jpsr.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "employees/:id", layout = MainView.class)
@ViewController("Employee.detail")
@ViewDescriptor("employee-detail-view.xml")
@EditedEntityContainer("employeeDc")
public class EmployeeDetailView extends StandardDetailView<Employee> {
}
