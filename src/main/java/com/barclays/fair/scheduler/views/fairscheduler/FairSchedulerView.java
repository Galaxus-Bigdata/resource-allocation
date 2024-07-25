package com.barclays.fair.scheduler.views.fairscheduler;

import com.barclays.fair.scheduler.data.FairScheduler;
import com.barclays.fair.scheduler.services.FairSchedulerService;
import com.barclays.fair.scheduler.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("Fair Scheduler")
@Route(value = "/:fairSchedulerID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@Uses(Icon.class)
public class FairSchedulerView extends Div implements BeforeEnterObserver {

    private final String FAIRSCHEDULER_ID = "fairSchedulerID";
    private final String FAIRSCHEDULER_EDIT_ROUTE_TEMPLATE = "/%s/edit";

    private final Grid<FairScheduler> grid = new Grid<>(FairScheduler.class, false);

    private TextField queue;
    private TextField weight;
    private TextField min_virtual_core;
    private TextField max_virtual_memory;
    private TextField max_running_apps;
    private TextField scheduling_policy;
    private Checkbox preemptable;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<FairScheduler> binder;

    private FairScheduler fairScheduler;

    private final FairSchedulerService fairSchedulerService;

    public FairSchedulerView(FairSchedulerService fairSchedulerService) {
        this.fairSchedulerService = fairSchedulerService;
        addClassNames("fair-scheduler-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("queue").setAutoWidth(true);
        grid.addColumn("weight").setAutoWidth(true);
        grid.addColumn("min_virtual_core").setAutoWidth(true);
        grid.addColumn("max_virtual_memory").setAutoWidth(true);
        grid.addColumn("max_running_apps").setAutoWidth(true);
        grid.addColumn("scheduling_policy").setAutoWidth(true);
        LitRenderer<FairScheduler> preemptableRenderer = LitRenderer.<FairScheduler>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", preemptable -> preemptable.isPreemptable() ? "check" : "minus")
                .withProperty("color",
                        preemptable -> preemptable.isPreemptable()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(preemptableRenderer).setHeader("Preemptable").setAutoWidth(true);

        grid.setItems(query -> fairSchedulerService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(FAIRSCHEDULER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(FairSchedulerView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(FairScheduler.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(weight).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("weight");
        binder.forField(min_virtual_core).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("min_virtual_core");
        binder.forField(max_virtual_memory).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("max_virtual_memory");
        binder.forField(max_running_apps).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("max_running_apps");

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.fairScheduler == null) {
                    this.fairScheduler = new FairScheduler();
                }
                binder.writeBean(this.fairScheduler);
                fairSchedulerService.update(this.fairScheduler);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(FairSchedulerView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> fairSchedulerId = event.getRouteParameters().get(FAIRSCHEDULER_ID).map(Long::parseLong);
        if (fairSchedulerId.isPresent()) {
            Optional<FairScheduler> fairSchedulerFromBackend = fairSchedulerService.get(fairSchedulerId.get());
            if (fairSchedulerFromBackend.isPresent()) {
                populateForm(fairSchedulerFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested fairScheduler was not found, ID = %s", fairSchedulerId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(FairSchedulerView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        queue = new TextField("Queue");
        weight = new TextField("Weight");
        min_virtual_core = new TextField("Min_virtual_core");
        max_virtual_memory = new TextField("Max_virtual_memory");
        max_running_apps = new TextField("Max_running_apps");
        scheduling_policy = new TextField("Scheduling_policy");
        preemptable = new Checkbox("Preemptable");
        formLayout.add(queue, weight, min_virtual_core, max_virtual_memory, max_running_apps, scheduling_policy,
                preemptable);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(FairScheduler value) {
        this.fairScheduler = value;
        binder.readBean(this.fairScheduler);

    }
}
