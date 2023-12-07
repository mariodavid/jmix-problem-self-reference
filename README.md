


### DB Content
In the DB we have the relationships:
- employee 1 has no manager
- employee 2 has employee 1 as manager
- employee 3 has employee 2 as manager

![](/img/db-content.png)


## Problem

When having a data model that is self-referencing (like Employee has a Manager, which is also an employee in this example),
lazy loading does not work when accessing the data through the `@OneToOne` inverse association.

### Domain Model

```java
@JmixEntity
@Table(name = "EMPLOYEE", indexes = {
        @Index(name = "IDX_EMPLOYEE_MANAGER", columnList = "MANAGER_ID")
})
@Entity
public class Employee {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANAGER_ID")
    private Employee manager;


    @OneToOne(mappedBy = "manager", fetch = FetchType.LAZY)
    private Employee report;
}
```

### Test

Given the DB contains the above-mentioned data: 1 -> 2 -> 3, when loading through data manager without specifying a fetch plan that includes the `report` attribute, the lazy loading behaviour is wrong. Instead of `1 -> 2 -> 3` it returns `1 -> 2 -> 1`.

See the following test [EmployeeTest.java](/src/test/java/com/company/jpsr/EmployeeTest.java):

```java
public class EmployeeTest {
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
}
```
