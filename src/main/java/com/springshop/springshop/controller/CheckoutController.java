package com.springshop.springshop.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springshop.springshop.model.Carrito;
import com.springshop.springshop.model.CheckoutForm;
import com.springshop.springshop.model.Pedido;
import com.springshop.springshop.service.PedidoService;

/**
 * Conecta el formulario de checkout con la sesión y el servicio de pedidos.
 */
@Controller
public class CheckoutController {

	private static final String ATRIBUTO_CARRITO = "carrito";
	private static final String ATRIBUTO_CHECKOUT = "checkout";

	private final PedidoService pedidoService;

	public CheckoutController(PedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	@GetMapping("/checkout")
	public String mostrarCheckout(HttpSession session, Model model) {
		Carrito carrito = obtenerCarrito(session);
		if (carrito == null || carrito.getItems().isEmpty()) {
			return "redirect:/carrito";
		}

		CheckoutForm formulario = new CheckoutForm();
		formulario.setTokenCheckout(generarToken(session));
		model.addAttribute("checkoutForm", formulario);
		model.addAttribute("carrito", carrito);
		return "checkout";
	}

	@PostMapping("/checkout/confirmar")
	public String confirmarPedido(@Valid @ModelAttribute("checkoutForm") CheckoutForm formulario,
			BindingResult bindingResult, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		synchronized (session) {
			EstadoCheckout estado = obtenerEstadoCheckout(session);
			Long pedidoExistente = estado.pedidosPorToken.get(formulario.getTokenCheckout());
			if (pedidoExistente != null) {
				return "redirect:/pedidos/confirmacion/" + pedidoExistente;
			}

			Carrito carrito = obtenerCarrito(session);
			if (carrito == null || carrito.getItems().isEmpty()) {
				redirectAttributes.addFlashAttribute("mensajeError",
						"No se encontró un carrito con productos para confirmar.");
				return "redirect:/carrito";
			}

			if (formulario.getTokenCheckout() == null
					|| !estado.tokensActivos.contains(formulario.getTokenCheckout())) {
				bindingResult.rejectValue("tokenCheckout", "checkout.token.invalido",
						"El intento de checkout no es válido. Volvé a iniciar la compra.");
			}
			if (bindingResult.hasErrors()) {
				model.addAttribute("carrito", carrito);
				return "checkout";
			}

			try {
				Pedido pedido = pedidoService.confirmarPedido(carrito, formulario.getNombreCliente(),
						formulario.getEmailCliente(), formulario.getDireccionEntrega(),
						formulario.getTokenCheckout());

				// La sesión conserva la relación entre token, pedido y comprador para reintentos y autorización.
				estado.tokensActivos.remove(formulario.getTokenCheckout());
				estado.pedidosPorToken.put(formulario.getTokenCheckout(), pedido.getId());
				estado.pedidosAutorizados.add(pedido.getId());
				carrito.vaciar();
				return "redirect:/pedidos/confirmacion/" + pedido.getId();
			} catch (IllegalArgumentException exception) {
				model.addAttribute("carrito", carrito);
				model.addAttribute("mensajeError", exception.getMessage());
				return "checkout";
			}
		}
	}

	@GetMapping("/pedidos/confirmacion/{id}")
	public String mostrarConfirmacion(@PathVariable Long id, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) {
		synchronized (session) {
			EstadoCheckout estado = obtenerEstadoCheckout(session);
			if (!estado.pedidosAutorizados.contains(id)) {
				redirectAttributes.addFlashAttribute("mensajeError", "No se encontró el pedido en esta sesión.");
				return "redirect:/carrito";
			}

			Pedido pedido = pedidoService.buscarPorId(id).orElse(null);
			if (pedido == null) {
				redirectAttributes.addFlashAttribute("mensajeError", "No se encontró el pedido solicitado.");
				return "redirect:/carrito";
			}

			model.addAttribute("pedido", pedido);
			return "confirmacion";
		}
	}

	private Carrito obtenerCarrito(HttpSession session) {
		Object atributo = session.getAttribute(ATRIBUTO_CARRITO);
		return atributo instanceof Carrito carrito ? carrito : null;
	}

	private String generarToken(HttpSession session) {
		synchronized (session) {
			String token = UUID.randomUUID().toString();
			obtenerEstadoCheckout(session).tokensActivos.add(token);
			return token;
		}
	}

	private EstadoCheckout obtenerEstadoCheckout(HttpSession session) {
		Object atributo = session.getAttribute(ATRIBUTO_CHECKOUT);
		if (atributo instanceof EstadoCheckout estado) {
			return estado;
		}

		EstadoCheckout estado = new EstadoCheckout();
		session.setAttribute(ATRIBUTO_CHECKOUT, estado);
		return estado;
	}

	/**
	 * Conserva los tokens de checkout válidos y los pedidos visibles para esta sesión.
	 */
	private static class EstadoCheckout {
		private final Set<String> tokensActivos = new HashSet<>();
		private final Map<String, Long> pedidosPorToken = new HashMap<>();
		private final Set<Long> pedidosAutorizados = new HashSet<>();
	}
}
