package com.springshop.springshop.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	@PostMapping("/carrito/aumentar/{id}")
	public String aumentarCantidad(@PathVariable String id, HttpSession session,
			RedirectAttributes redirectAttributes) {
		return modificarCantidad(id, true, session, redirectAttributes);
	}

	@PostMapping("/carrito/disminuir/{id}")
	public String disminuirCantidad(@PathVariable String id, HttpSession session,
			RedirectAttributes redirectAttributes) {
		return modificarCantidad(id, false, session, redirectAttributes);
	}

	@PostMapping("/carrito/eliminar/{id}")
	public String eliminarProducto(@PathVariable String id, HttpSession session,
			RedirectAttributes redirectAttributes) {
		long idProducto;
		try {
			idProducto = Long.parseLong(id);
		} catch (NumberFormatException exception) {
			redirectAttributes.addFlashAttribute("mensajeError", "El identificador del producto no es válido.");
			return "redirect:/carrito";
		}

		Carrito carrito = (Carrito) session.getAttribute(ATRIBUTO_CARRITO);
		if (carrito == null) {
			redirectAttributes.addFlashAttribute("mensajeError", "El producto no está presente en el carrito.");
			return "redirect:/carrito";
		}

		try {
			carrito.eliminarProducto(idProducto);
		} catch (IllegalArgumentException exception) {
			redirectAttributes.addFlashAttribute("mensajeError", exception.getMessage());
			return "redirect:/carrito";
		}

		redirectAttributes.addFlashAttribute("mensajeExito", "El producto se eliminó del carrito.");
		return "redirect:/carrito";
	}

	@PostMapping("/carrito/vaciar")
	public String vaciarCarrito(HttpSession session, RedirectAttributes redirectAttributes) {
		// Solo se modifica el carrito asociado a la sesión actual; la sesión permanece intacta.
		obtenerOCrearCarrito(session).vaciar();
		redirectAttributes.addFlashAttribute("mensajeExito", "El carrito se vació correctamente.");
		return "redirect:/carrito";
	}

	/**
	 * Valida el ID y el producto contra el catálogo antes de modificar el carrito de esta sesión.
	 */
	private String modificarCantidad(String id, boolean aumentar, HttpSession session,
			RedirectAttributes redirectAttributes) {
		long idProducto;
		try {
			idProducto = Long.parseLong(id);
		} catch (NumberFormatException exception) {
			redirectAttributes.addFlashAttribute("mensajeError", "El identificador del producto no es válido.");
			return "redirect:/carrito";
		}

		Producto producto = productoService.buscarPorId(idProducto).orElse(null);
		if (producto == null) {
			redirectAttributes.addFlashAttribute("mensajeError", "El producto solicitado no existe.");
			return "redirect:/carrito";
		}

		Carrito carrito = (Carrito) session.getAttribute(ATRIBUTO_CARRITO);
		if (carrito == null) {
			redirectAttributes.addFlashAttribute("mensajeError", "El producto no está presente en el carrito.");
			return "redirect:/carrito";
		}

		try {
			if (aumentar) {
				carrito.aumentarCantidad(producto.getId(), producto.getStock());
			} else {
				carrito.disminuirCantidad(producto.getId());
			}
		} catch (IllegalArgumentException exception) {
			redirectAttributes.addFlashAttribute("mensajeError", exception.getMessage());
			return "redirect:/carrito";
		}

		String accion = aumentar ? "aumentó" : "disminuyó";
		redirectAttributes.addFlashAttribute("mensajeExito",
				"Se " + accion + " la cantidad de " + producto.getNombre() + ".");
		return "redirect:/carrito";
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
