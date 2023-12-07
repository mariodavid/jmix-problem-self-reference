package com.company.jpsr.view.employee;

import com.company.jpsr.entity.Employee;

import com.company.jpsr.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "employees", layout = MainView.class)
@ViewController("Employee.list")
@ViewDescriptor("employee-list-view.xml")
@LookupComponent("employeesDataGrid")
@DialogMode(width = "64em")
public class EmployeeListView extends StandardListView<Employee> {
    private static final Logger log = LoggerFactory.getLogger(EmployeeListView.class);

    @ViewComponent
    private CollectionContainer<Employee> employeesDc;
    @Autowired
    private EntityStates entityStates;

    @Subscribe(id = "employeesDl", target = Target.DATA_LOADER)
    public void onEmployeesDlPostLoad(final CollectionLoader.PostLoadEvent<Employee> event) {


        employeesDc.getItems().forEach(employee -> {
            boolean managerLoaded = entityStates.isLoaded(employee, "manager");
            log.info("Employee: {}, Manager loaded: {}", employee.getName(), managerLoaded);
            log.info("Employee: {}, Manager: {}", employee.getName(), employee.getManager());
        });
    }
}
