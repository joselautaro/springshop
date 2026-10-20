package com.springshop.springshop.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.View;

import com.springshop.springshop.model.Carrito;
import com.springshop.springshop.model.DetallePedido;
import com.springshop.springshop.model.EstadoPedido;
import com.springshop.springshop.model.Pedido;
import com.springshop.springshop.model.Producto;
import com.springshop.springshop.service.PedidoService;

class CheckoutControllerTests {

	private PedidoService pedidoService;
	private MockMvc mockMvc;

	@BeforeEach
	void prepararControlador() {
		pedidoService = mock(PedidoService.class);
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(new CheckoutController(pedidoService))
				.setValidator(validator)
				.setViewResolvers((viewName, locale) -> viewName.startsWith("redirect:")
						? new RedirectView(viewName.substring("redirect:".length()))
						: mock(View.class))
				.build();
	}

	@Test
	void checkoutRedirigeCuandoLaSesionNoTieneProductos() throws Exception {
		mockMvc.perform(get("/checkout").session(new MockHttpSession()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/carrito"));
	}

	@Test
	void checkoutGeneraTokenDeServidorYRenderizaFormulario() throws Exception {
		MockHttpSession session = sesionConCarrito();

		MvcResult resultado = mockMvc.perform(get("/checkout").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("checkout"))
				.andReturn();

		Object formulario = resultado.getModelAndView().getModel().get("checkoutForm");
		assertNotNull(formulario);
		String token = ((com.springshop.springshop.model.CheckoutForm) formulario).getTokenCheckout();
		assertNotNull(token);
		assertFalse(token.isBlank());
	}

	@Test
	void erroresDeValidacionMuestranCheckoutYConservanLosDatosYCarrito() throws Exception {
		MockHttpSession session = sesionConCarrito();
		String token = tokenDeCheckout(session);

		MvcResult resultado = mockMvc.perform(post("/checkout/confirmar").session(session)
				.param("nombreCliente", "")
				.param("emailCliente", "correo-invalido")
				.param("direccionEntrega", "")
				.param("tokenCheckout", token))
				.andExpect(status().isOk())
				.andExpect(view().name("checkout"))
				.andReturn();

		assertTrue(resultado.getModelAndView().getModel().containsKey("carrito"));
		assertEquals("correo-invalido", ((com.springshop.springshop.model.CheckoutForm) resultado.getModelAndView()
				.getModel().get("checkoutForm")).getEmailCliente());
		assertEquals(1, ((Carrito) session.getAttribute("carrito")).obtenerCantidad(1L));
		verifyNoInteractions(pedidoService);
	}

	@Test
	void confirmarPedidoVacíaSoloElCarritoDeLaSesionYUnReenvioNoDuplicaElPedido() throws Exception {
		MockHttpSession session = sesionConCarrito();
		Carrito carrito = (Carrito) session.getAttribute("carrito");
		String token = tokenDeCheckout(session);
		Pedido pedido = pedidoDePrueba();
		when(pedidoService.confirmarPedido(eq(carrito), eq("Cliente"), eq("cliente@example.com"),
				eq("Dirección"), eq(token))).thenReturn(pedido);

		for (int intento = 0; intento < 2; intento++) {
			mockMvc.perform(post("/checkout/confirmar").session(session)
					.param("nombreCliente", "Cliente")
					.param("emailCliente", "cliente@example.com")
					.param("direccionEntrega", "Dirección")
					.param("tokenCheckout", token))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/pedidos/confirmacion/42"));
		}

		assertTrue(carrito.getItems().isEmpty());
		verify(pedidoService, times(1)).confirmarPedido(carrito, "Cliente", "cliente@example.com", "Dirección", token);
	}

	@Test
	void laConfirmacionSoloSeConsultaDesdeLaSesionQueCreoElPedido() throws Exception {
		MockHttpSession sesionCompradora = sesionConCarrito();
		Carrito carrito = (Carrito) sesionCompradora.getAttribute("carrito");
		String token = tokenDeCheckout(sesionCompradora);
		Pedido pedido = pedidoDePrueba();
		when(pedidoService.confirmarPedido(eq(carrito), anyString(), anyString(), anyString(), eq(token)))
				.thenReturn(pedido);
		when(pedidoService.buscarPorId(42L)).thenReturn(Optional.of(pedido));
		mockMvc.perform(post("/checkout/confirmar").session(sesionCompradora)
				.param("nombreCliente", "Cliente")
				.param("emailCliente", "cliente@example.com")
				.param("direccionEntrega", "Dirección")
				.param("tokenCheckout", token))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/pedidos/confirmacion/42"));

		mockMvc.perform(get("/pedidos/confirmacion/42").session(new MockHttpSession()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/carrito"));
		verify(pedidoService, times(0)).buscarPorId(42L);

		mockMvc.perform(get("/pedidos/confirmacion/42").session(sesionCompradora))
				.andExpect(status().isOk())
				.andExpect(view().name("confirmacion"));
		verify(pedidoService, times(1)).buscarPorId(42L);
	}

	private MockHttpSession sesionConCarrito() {
		MockHttpSession session = new MockHttpSession();
		Carrito carrito = new Carrito();
		carrito.agregarProducto(new Producto(1L, "Producto", "Descripción", new BigDecimal("12.50"), 5));
		session.setAttribute("carrito", carrito);
		return session;
	}

	private String tokenDeCheckout(MockHttpSession session) throws Exception {
		MvcResult resultado = mockMvc.perform(get("/checkout").session(session))
				.andExpect(status().isOk())
				.andReturn();
		return ((com.springshop.springshop.model.CheckoutForm) resultado.getModelAndView()
				.getModel().get("checkoutForm")).getTokenCheckout();
	}

	private Pedido pedidoDePrueba() {
		DetallePedido detalle = new DetallePedido(1L, "Producto", 1, new BigDecimal("12.50"));
		return new Pedido(42L, "Cliente", "cliente@example.com", "Dirección", LocalDateTime.now(),
				List.of(detalle), EstadoPedido.CONFIRMADO);
	}
}
