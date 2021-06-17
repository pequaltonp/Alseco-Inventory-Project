package com.example.demo.controller;

import com.example.demo.entity.Employee;
import com.example.demo.entity.Item;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ItemRepository itemRepository;

    @GetMapping("/")
    public String homePage(Model model) {
        List<Employee> employeeList = employeeRepository.findAll();
        employeeList.sort((a, b)-> (int) (a.getId() - b.getId()));

        model.addAttribute("employeeList", employeeList);
        model.addAttribute("newEmployee", new Employee());

        return "main";
    }

    @GetMapping("/filter")
    public String filteredEmployeePage(@RequestParam(value = "fullname", defaultValue = " ") String fullname,
            @RequestParam(value = "total_quantity", defaultValue = "0") int totalQuantity,
            @RequestParam(value = "total_cost", defaultValue = "0") int totalCost,
            Model model) {

        List<Employee> employeeAll = employeeRepository.findAll();
        List<Employee> filtered = new ArrayList<>();

        for (Employee employee: employeeAll) {
            if (employee.toString().equals(fullname)
                    || (employee.getItemList().size() == totalQuantity)
                    || (employee.totalCost() == totalCost))
                filtered.add(employee);
        }

        System.out.println(filtered);

        model.addAttribute("employeeList", filtered);
        model.addAttribute("newEmployee", new Employee());

        return "main";
    }

    @GetMapping("/details/{id}")
    public String viewDetails(@PathVariable Long id, Model model) {
        model.addAttribute("currentEmployee", employeeRepository.findById(id).get());

        return "details";
    }

    @PostMapping("/add_employee")
    public String newEmployee(@ModelAttribute(value = "newEmployee") Employee newEmployee) {
        System.out.println(newEmployee);
        employeeRepository.save(newEmployee);

        return "redirect:/";
    }

    @PostMapping("/newItem/{emplId}")
    public String addItem(@PathVariable Long emplId,
                          @ModelAttribute(value = "newItem") Item newItem) {

        Employee employee = employeeRepository.findById(emplId).get();
        newItem.setEmployee(employee);
        employee.getItemList().add(newItem);

        System.out.println(newItem);
        System.out.println(employee.getItemList());
        employeeRepository.save(employee);

        return "redirect:/edit/"+emplId;
    }

    @PostMapping("/{id}")
    public String updateEmployee(@PathVariable Long id,
                                 @ModelAttribute Employee currentEmployee) {

        if (currentEmployee.getItemList() != null) {
            for (Item item : currentEmployee.getItemList()) {
                item.setEmployee(currentEmployee);
            }
        }

        System.out.println(currentEmployee.getItemList());

        employeeRepository.save(currentEmployee);

        return "redirect:/details/"+id;
    }


    @GetMapping("/edit/{id}")
    public String editEmployee(@PathVariable Long id, Model model) {
        model.addAttribute("currentEmployee", employeeRepository.findById(id).get());
        model.addAttribute("newItem", new Item());
        return "editPage";
    }

    @GetMapping("/deleteItem/{id}")
    public String deleteItem(@PathVariable(value = "id") Long id) {
        Item item = itemRepository.findById(id).get();
        Employee employee = item.getEmployee();
        employee.getItemList().remove(item);

        itemRepository.delete(item);

        return "redirect:/edit/"+employee.getId();
    }

    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable(value = "id") Long id) {

        employeeRepository.deleteById(id);

        return "redirect:/";
    }
}
