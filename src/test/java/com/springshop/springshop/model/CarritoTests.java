package com.springshop.springshop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CarritoTests {

	@Test
	void aumentarCantidadRespetaElStockVigente() {
		Producto producto = producto(1L, 2);
		Carrito carrito = new Carrito();
		carrito.agregarProducto(producto);

		carrito.aumentarCantidad(producto.getId(), 2);
		assertEquals(2, carrito.obtenerCantidad(producto.getId()));

		assertThrows(IllegalArgumentException.class, () -> carrito.aumentarCantidad(producto.getId(), 2));
		assertEquals(2, carrito.obtenerCantidad(producto.getId()));
	}

	@Test
	void disminuirCantidadNoPermiteBajarDeUnaUnidad() {
		Producto producto = producto(1L, 3);
		Carrito carrito = new Carrito();
		carrito.agregarProducto(producto);
		carrito.aumentarCantidad(producto.getId(), producto.getStock());

		carrito.disminuirCantidad(producto.getId());
		assertEquals(1, carrito.obtenerCantidad(producto.getId()));

		assertThrows(IllegalArgumentException.class, () -> carrito.disminuirCantidad(producto.getId()));
		assertEquals(1, carrito.obtenerCantidad(producto.getId()));
	}

	@Test
	void operacionesRechazanIdsInvalidosOProductosAusentes() {
		Carrito carrito = new Carrito();

		assertThrows(IllegalArgumentException.class, () -> carrito.aumentarCantidad(1L, 3));
		assertThrows(IllegalArgumentException.class, () -> carrito.disminuirCantidad(1L));
		assertThrows(IllegalArgumentException.class, () -> carrito.aumentarCantidad(0L, 3));
	}

	private Producto producto(Long id, int stock) {
		return new Producto(id, "Producto de prueba", "Descripción", new BigDecimal("12.50"), stock);
	}
}
