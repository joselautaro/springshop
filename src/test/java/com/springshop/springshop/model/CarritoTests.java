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

	@Test
	void eliminarProductoQuitaLaLineaCompletaIndependientementeDeLaCantidad() {
		Producto producto = producto(1L, 5);
		Carrito carrito = new Carrito();
		carrito.agregarProducto(producto);
		carrito.aumentarCantidad(producto.getId(), producto.getStock());
		carrito.aumentarCantidad(producto.getId(), producto.getStock());

		carrito.eliminarProducto(producto.getId());

		assertEquals(0, carrito.obtenerCantidad(producto.getId()));
		assertEquals(BigDecimal.ZERO, carrito.calcularTotal());
	}

	@Test
	void eliminarProductoRechazaIdsInvalidosYLineasAusentes() {
		Carrito carrito = new Carrito();

		assertThrows(IllegalArgumentException.class, () -> carrito.eliminarProducto(null));
		assertThrows(IllegalArgumentException.class, () -> carrito.eliminarProducto(0L));
		assertThrows(IllegalArgumentException.class, () -> carrito.eliminarProducto(1L));
	}

	@Test
	void vaciarEliminaTodasLasLineasDelCarrito() {
		Carrito carrito = new Carrito();
		carrito.agregarProducto(producto(1L, 5));
		carrito.agregarProducto(producto(2L, 3));

		carrito.vaciar();

		assertEquals(0, carrito.getItems().size());
		assertEquals(BigDecimal.ZERO, carrito.calcularTotal());
	}

	private Producto producto(Long id, int stock) {
		return new Producto(id, "Producto de prueba", "Descripción", new BigDecimal("12.50"), stock);
	}
}
