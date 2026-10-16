package com.springshop.springshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.springshop.springshop.service.ProductoService;

/**
 * Atiende la página principal y prepara los datos que Thymeleaf muestra en ella.
 */
@Controller
public class HomeController {

	private final ProductoService productoService;

	public HomeController(ProductoService productoService) {
		this.productoService = productoService;
	}

	@GetMapping("/")
	public String mostrarCatalogo(Model model) {
		model.addAttribute("productos", productoService.obtenerProductos());
		return "index";
	}
}
