package goorm.humandelivery.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import goorm.humandelivery.customer.service.CustomerService;
import goorm.humandelivery.driver.TaxiDriverService;

@Controller
@RequestMapping("api/v1/login")
public class LoginController {

	private final CustomerService customerService;
	private final TaxiDriverService taxiDriverService;

	@Autowired
	public LoginController(CustomerService customerService, TaxiDriverService taxiDriverService) {
		this.customerService = customerService;
		this.taxiDriverService = taxiDriverService;
	}
}
