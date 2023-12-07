package com.company.jpsr.view.employee;

import com.company.jpsr.entity.Employee;
import com.company.jpsr.view.main.MainView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "virtual-employees", layout = MainView.class)
@ViewController("Employee.virtualList")
@ViewDescriptor("employee-virtual-list-view.xml")
@DialogMode(width = "64em")
public class EmployeeVirtualListView extends StandardListView<Employee> {
    @ViewComponent
    private CollectionContainer<Employee> employeesDc;
    @ViewComponent
    private VerticalLayout wrapper;
    @Autowired
    private UiComponents uiComponents;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        VirtualList<Employee> virtualList = uiComponents.create(VirtualList.class);
        virtualList.setWidthFull();
        virtualList.setHeightFull();
        virtualList.setRenderer(new ComponentRenderer<>(employee ->
                        new EmployeeListItem(
                                employee,
                                uiComponents
                        )
                )
        );
        List<Employee> items = employeesDc.getItems();
        virtualList.setItems(items);
        wrapper.add(virtualList);
    }

    public CollectionContainer<Employee> getEmployeesDc() {
        return employeesDc;
    }
}
