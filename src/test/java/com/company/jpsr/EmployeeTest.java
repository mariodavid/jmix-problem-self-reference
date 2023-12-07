package com.company.jpsr;

import com.company.jpsr.entity.Employee;
import com.company.jpsr.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sample integration test for the User entity.
 */
@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class EmployeeTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    EntityStates entityStates;

    @Test
    void given_employeeManagerChainOfThree_when_loadTopLevelManagersWithFirstLevelReports_expect_() {

        // given:
        Employee employee1 = saveEmployee("1", null);
        Employee employee2 = saveEmployee("2", employee1);
        Employee employee3 = saveEmployee("3", employee2);

        // when:
        List<Employee> employeesWithoutManagers =
                dataManager.load(Employee.class).query("select e from Employee e where e.manager is null")
                        .fetchPlan(fetchPlanBuilder -> fetchPlanBuilder.add("report", FetchPlan.BASE))
                        .list();

        // then:
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
}
