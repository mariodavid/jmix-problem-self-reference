package com.company.jpsr;

import com.company.jpsr.entity.Employee;
import com.company.jpsr.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class EmployeeTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    EntityStates entityStates;

    @Autowired
    DataSource dataSource;
    private Employee employee1;
    private Employee employee2;
    private Employee employee3;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setupData() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("DELETE FROM EMPLOYEE");

        employee1 = saveEmployee(1, null);
        employee2 = saveEmployee(2, employee1);
        employee3 = saveEmployee(3, employee2);
    }

    /**
     * this test is working, and it is also supposed to be working
     */
    @Test
    void given_fetchPlanContainsReport_andNotManager_expect_chainIsCorrect() {

        // given:
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, manager_id FROM EMPLOYEE ORDER BY id");

        // expect: employee 1 has no manager in DB
        assertThat(rows.get(0).get("id")).isEqualTo(uuid(1));
        assertThat(rows.get(0).get("manager_id")).isNull();

        // and: employee 2 has manager 1 in DB
        assertThat(rows.get(1).get("id")).isEqualTo(uuid(2));
        assertThat(rows.get(1).get("manager_id")).isEqualTo(uuid(1));

        // and: employee 3 has manager 2 in DB
        assertThat(rows.get(2).get("id")).isEqualTo(uuid(3));
        assertThat(rows.get(2).get("manager_id")).isEqualTo(uuid(2));

        // when:
        Employee loadedEmployee1 =
                dataManager.load(Employee.class)
                        .query("select e from Employee e where e.manager is null")
                        .fetchPlan(fetchPlanBuilder -> {

                                // here were are adding the "report" attribute to load directly the corresponding child elements
                                fetchPlanBuilder.add("report", FetchPlan.BASE);
                                fetchPlanBuilder.add("report.report", FetchPlan.BASE);
                        })
                        .list().stream().findFirst().orElseThrow();

        // then:
        assertThat(loadedEmployee1.getReport()).isEqualTo(employee2);

        // and:
        assertThat(loadedEmployee1.getReport().getReport()).isEqualTo(employee3);
    }


    /**
     * this test fails, but it should pass. It is not specifying the loading mechanism upfront and instead relies
     * on lazy loading. Due to this, the chain is not correctly specified. Instead of the actual chain:
     * 1 -> 2 -> 3, the objects represent the following chain: 1 -> 2 -> 1 - which is incorrect
     */
    @Test
    void given_fetchPlanContainsManager_andNotReport_expect_chainIsCorrect() {

        // given:
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, manager_id FROM EMPLOYEE ORDER BY id");

        // expect: employee 1 has no manager in DB
        assertThat(rows.get(0).get("id")).isEqualTo(uuid(1));
        assertThat(rows.get(0).get("manager_id")).isNull();

        // and: employee 2 has manager 1 in DB
        assertThat(rows.get(1).get("id")).isEqualTo(uuid(2));
        assertThat(rows.get(1).get("manager_id")).isEqualTo(uuid(1));

        // and: employee 3 has manager 2 in DB
        assertThat(rows.get(2).get("id")).isEqualTo(uuid(3));
        assertThat(rows.get(2).get("manager_id")).isEqualTo(uuid(2));

        // when:
        Employee loadedEmployee1 =
                dataManager.load(Employee.class)
                        .query("select e from Employee e where e.manager is null")
                        .fetchPlan(fetchPlanBuilder ->
                                // we explicitly don't automatically fetch the "report" attribute.
                                // this triggers the behaviour that the lazy loading through multiple levels
                                // does not work anymore
                                fetchPlanBuilder.addFetchPlan(FetchPlan.LOCAL)
                        )
                        .list().stream().findFirst().orElseThrow();

        // then: the first report is correct
        assertThat(loadedEmployee1.getReport()).isEqualTo(employee2);

        // but: the second report is incorrect. It says it is "employee1" instead - which describes the bug
        assertThat(loadedEmployee1.getReport().getReport()).isEqualTo(employee3);
    }

    private Employee saveEmployee(int number, Employee manager) {
        Employee employee = dataManager.create(Employee.class);
        employee.setId(uuid(number));
        employee.setName("" + number);
        employee.setManager(manager);
        dataManager.save(employee);
        return employee;
    }

    private static UUID uuid(int number) {
        return UUID.fromString("00000000-0000-0000-0000-00000000000" + number);
    }

}
