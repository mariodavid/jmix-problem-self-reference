package com.company.jpsr;

import com.company.jpsr.entity.Employee;
import com.company.jpsr.entity.User;
import com.company.jpsr.view.employee.EmployeeVirtualListView;
import com.company.jpsr.view.user.UserDetailView;
import com.company.jpsr.view.user.UserListView;
import com.vaadin.flow.component.Component;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@SpringBootTest(classes = {JmixProblemSelfReferenceApplication.class, FlowuiTestAssistConfiguration.class})
public class EmployeeVirtualListTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    EntityStates entityStates;
    @Autowired
    ViewNavigators viewNavigators;

    @Test
    void test_createUser() {

        Employee employee1 = saveEmployee("1", null);
        Employee employee2 = saveEmployee("2", employee1);
        Employee employee3 = saveEmployee("3", employee2);

        viewNavigators.view(EmployeeVirtualListView.class).navigate();

        EmployeeVirtualListView view = UiTestUtils.getCurrentView();

        List<Employee> employeesWithoutManagers = view.getEmployeesDc().getItems();
        assertThat(employeesWithoutManagers).hasSize(1);
        Employee loadedEmployee1 = employeesWithoutManagers.get(0);

        // and:
        assertThat(entityStates.isLoaded(loadedEmployee1, "report")).isTrue();
        assertThat(loadedEmployee1.getReport().getName()).isEqualTo("2");

        // and:
        assertThat(entityStates.isLoaded(loadedEmployee1.getReport(), "report")).isFalse();
        assertThat(loadedEmployee1.getReport().getReport().getName()).isEqualTo("3");
    }

    private Employee saveEmployee(String name, Employee manager) {
        Employee employee = dataManager.create(Employee.class);
        employee.setName(name);
        employee.setManager(manager);
        dataManager.save(employee);
        return employee;
    }

    @AfterEach
    void tearDown() {
        dataManager.load(Employee.class).all().list().forEach(dataManager::remove);
    }

    /**
     * Returns a component defined in the screen by the component id.
     * Throws an exception if not found.
     */
    @SuppressWarnings("unchecked")
    private <T> T findComponent(View<?> view, String componentId) {
        Optional<Component> component = UiComponentUtils.findComponent(view, componentId);
        Assertions.assertTrue(component.isPresent());
        return (T) component.get();
    }
}
