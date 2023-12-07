package com.company.jpsr.view.employee;

import com.company.jpsr.entity.Employee;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.UiComponents;

public class EmployeeListItem extends VerticalLayout {
    private final Employee employee;
    private final UiComponents uiComponents;

    public EmployeeListItem(Employee employee, UiComponents uiComponents) {
        this.employee = employee;
        this.uiComponents = uiComponents;

        initLayout();
    }

    private void initLayout() {
        setWidthFull();
        setHeight("100px");
        setPadding(false);
        setSpacing(false);

        HorizontalLayout layout = uiComponents.create(HorizontalLayout.class);

        layout.add(nameSpan(employee));

        if (employee.getReport() != null) {
            layout.add(nameSpan(employee.getReport()));

            if (employee.getReport().getReport() != null) {
                layout.add(nameSpan(employee.getReport().getReport()));
            }
        }

        add(layout);
    }

    private Span nameSpan(Employee employee) {
        Span reportSpan = uiComponents.create(Span.class);
        reportSpan.setText(employee.getName());
        return reportSpan;
    }
}
