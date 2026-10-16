package com.springshop.springshop.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import com.springshop.springshop.model.Carrito;
import com.springshop.springshop.service.ProductoService;

@SpringBootTest
@AutoConfigureMockMvc
class CarritoControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProductoService productoService;

	@Test
	void aumentarCantidadModificaElCarritoDeLaSesionYRedirigeAlCarrito() throws Exception {
		MockHttpSession session = sesionConUnProducto();
		Carrito carrito = (Carrito) session.getAttribute("carrito");

		mockMvc.perform(post("/carrito/aumentar/1").session(session))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/carrito"))
				.andExpect(flash().attributeExists("mensajeExito"));

		assertEquals(2, carrito.obtenerCantidad(1L));
	}

	@Test
	void disminuirCantidadRechazaBajarDeUnaYNoModificaElCarrito() throws Exception {
		MockHttpSession session = sesionConUnProducto();
		Carrito carrito = (Carrito) session.getAttribute("carrito");

		mockMvc.perform(post("/carrito/disminuir/1").session(session))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/carrito"))
				.andExpect(flash().attributeExists("mensajeError"));

		assertEquals(1, carrito.obtenerCantidad(1L));
	}

	@Test
	void productoInexistenteSeRechazaSinModificarElCarrito() throws Exception {
		MockHttpSession session = sesionConUnProducto();
		Carrito carrito = (Carrito) session.getAttribute("carrito");

		mockMvc.perform(post("/carrito/aumentar/999").session(session))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/carrito"))
				.andExpect(flash().attributeExists("mensajeError"));

		assertEquals(1, carrito.obtenerCantidad(1L));
	}

	@Test
	void eliminarProductoQuitaSoloDelCarritoDeLaSesionActual() throws Exception {
		MockHttpSession primeraSesion = sesionConUnProducto();
		Carrito primerCarrito = (Carrito) primeraSesion.getAttribute("carrito");
		primerCarrito.aumentarCantidad(1L, 12);

		MockHttpSession segundaSesion = sesionConUnProducto();
		Carrito segundoCarrito = (Carrito) segundaSesion.getAttribute("carrito");

		mockMvc.perform(post("/carrito/eliminar/1").session(primeraSesion))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/carrito"))
				.andExpect(flash().attributeExists("mensajeExito"));

		assertEquals(0, primerCarrito.obtenerCantidad(1L));
		assertEquals(1, segundoCarrito.obtenerCantidad(1L));
		assertEquals(12, productoService.buscarPorId(1L).orElseThrow().getStock());
	}

	@Test
	void eliminarProductoAusenteMuestraErrorSinCambiarElCarrito() throws Exception {
		MockHttpSession session = sesionConUnProducto();
		Carrito carrito = (Carrito) session.getAttribute("carrito");

		mockMvc.perform(post("/carrito/eliminar/2").session(session))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/carrito"))
				.andExpect(flash().attributeExists("mensajeError"));

		assertEquals(1, carrito.obtenerCantidad(1L));
	}

	@Test
	void carritoSeMantieneAisladoEntreSesionesYLaVistaSeRenderiza() throws Exception {
		MockHttpSession primeraSesion = sesionConUnProducto();
		MockHttpSession segundaSesion = new MockHttpSession();

		mockMvc.perform(post("/carrito/aumentar/1").session(primeraSesion))
				.andExpect(status().is3xxRedirection());

		assertEquals(2, ((Carrito) primeraSesion.getAttribute("carrito")).obtenerCantidad(1L));
		assertTrue(segundaSesion.getAttribute("carrito") == null);
		assertNotEquals(primeraSesion.getId(), segundaSesion.getId());

		mockMvc.perform(get("/carrito").session(segundaSesion))
				.andExpect(status().isOk());
	}

	private MockHttpSession sesionConUnProducto() {
		MockHttpSession session = new MockHttpSession();
		Carrito carrito = new Carrito();
		carrito.agregarProducto(productoService.buscarPorId(1L).orElseThrow());
		session.setAttribute("carrito", carrito);
		return session;
	}
}
