package com.springshop.springshop.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springshop.springshop.model.Carrito;
import com.springshop.springshop.model.Producto;
import com.springshop.springshop.service.ProductoService;

/**
 * Gestiona las operaciones del carrito guardado en la sesión HTTP de cada usuario.
 */
@Controller
public class CarritoController {

	private static final String ATRIBUTO_CARRITO = "carrito";

	private final ProductoService productoService;

	public CarritoController(ProductoService productoService) {
		this.productoService = productoService;
	}

	@PostMapping("/carrito/agregar")
	public String agregarProducto(@RequestParam(name = "productoId", required = false) String productoId,
			HttpSession session, RedirectAttributes redirectAttributes) {
		if (productoId == null || productoId.isBlank()) {
			redirectAttributes.addFlashAttribute("mensajeError", "Seleccioná un producto válido.");
			return "redirect:/";
		}

		long id;
		try {
			id = Long.parseLong(productoId);
		} catch (NumberFormatException exception) {
			redirectAttributes.addFlashAttribute("mensajeError", "El identificador del producto no es válido.");
			return "redirect:/";
		}

		Producto producto = productoService.buscarPorId(id).orElse(null);
		if (producto == null) {
			redirectAttributes.addFlashAttribute("mensajeError", "El producto solicitado no existe.");
			return "redirect:/";
		}

		Carrito carrito = obtenerOCrearCarrito(session);
		try {
			carrito.agregarProducto(producto);
		} catch (IllegalArgumentException exception) {
			redirectAttributes.addFlashAttribute("mensajeError", exception.getMessage());
			return "redirect:/";
		}

		redirectAttributes.addFlashAttribute("mensajeExito",
				producto.getNombre() + " se agregó al carrito.");
		return "redirect:/";
	}

	@GetMapping("/carrito")
	public String mostrarCarrito(HttpSession session, Model model) {
		model.addAttribute("carrito", obtenerOCrearCarrito(session));
		return "carrito";
	}

	private Carrito obtenerOCrearCarrito(HttpSession session) {
		Carrito carrito = (Carrito) session.getAttribute(ATRIBUTO_CARRITO);
		if (carrito == null) {
			carrito = new Carrito();
			session.setAttribute(ATRIBUTO_CARRITO, carrito);
		}
		return carrito;
	}
}
