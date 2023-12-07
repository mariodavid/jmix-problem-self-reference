


### DB Content
In the DB we have the relationships:
- employee 1 has no manager
- employee 2 has employee 1 as manager
- employee 3 has employee 2 as manager

![](/img/db-content.png)

### Tree Data Grid
In the data grid it is correctly displaying the tree structure of the employees.

![](/img/tree-data-grid.png)

### Virtual List

The virtual list is supposed to be doing the following:

The data container loads only the top level rows (manager = null), but with a fetch plan,
to include the report (transient 1:1 inverse relationship of `manager`).


![](/img/virtual-list.png)
