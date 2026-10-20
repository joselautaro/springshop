package com.springshop.springshop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springshop.springshop.model.Carrito;
import com.springshop.springshop.model.Pedido;
import com.springshop.springshop.model.Producto;

class PedidoServiceTests {

	private ProductoService productoService;
	private PedidoService pedidoService;

	@BeforeEach
	void prepararServicios() {
		productoService = new ProductoService();
		pedidoService = new PedidoService(productoService);
	}

	@Test
	void registraPedidoConPrecioActualDescuentaStockYPermiteConsultarlo() {
		Carrito carrito = new Carrito();
		carrito.agregarProducto(new Producto(1L, "Nombre obsoleto", "Descripción", new BigDecimal("1.00"), 12));
		carrito.aumentarCantidad(1L, 12);

		Pedido pedido = pedidoService.confirmarPedido(carrito, "Cliente", "cliente@example.com", "Dirección", "token-1");

		assertEquals("Auriculares Studio", pedido.getDetalles().getFirst().getNombreProducto());
		assertEquals(new BigDecimal("89.90"), pedido.getDetalles().getFirst().getPrecioUnitario());
		assertEquals(new BigDecimal("179.80"), pedido.getTotal());
		assertEquals(10, productoService.buscarPorId(1L).orElseThrow().getStock());
		assertEquals(pedido, pedidoService.buscarPorId(pedido.getId()).orElseThrow());
	}

	@Test
	void rechazaCarritoVacioSinRegistrarPedidoNiCambiarStock() {
		int stockInicial = productoService.buscarPorId(1L).orElseThrow().getStock();

		assertThrows(IllegalArgumentException.class,
				() -> pedidoService.confirmarPedido(new Carrito(), "Cliente", "cliente@example.com", "Dirección",
						"token-vacio"));

		assertTrue(pedidoService.buscarPorId(1L).isEmpty());
		assertEquals(stockInicial, productoService.buscarPorId(1L).orElseThrow().getStock());
	}

	@Test
	void rechazaProductosQueYaNoExistenSinRegistrarPedido() {
		Carrito carrito = new Carrito();
		carrito.agregarProducto(new Producto(999L, "Producto retirado", "Descripción", BigDecimal.ONE, 5));

		assertThrows(IllegalArgumentException.class,
				() -> pedidoService.confirmarPedido(carrito, "Cliente", "cliente@example.com", "Dirección",
						"token-inexistente"));

		assertFalse(pedidoService.buscarPorId(1L).isPresent());
		assertEquals(12, productoService.buscarPorId(1L).orElseThrow().getStock());
	}

	@Test
	void rechazoPorStockInsuficienteNoDescuentaNingunaLineaDelPedido() {
		Carrito primeraCompra = new Carrito();
		primeraCompra.agregarProducto(productoService.buscarPorId(1L).orElseThrow());
		for (int index = 1; index < 12; index++) {
			primeraCompra.aumentarCantidad(1L, 12);
		}
		pedidoService.confirmarPedido(primeraCompra, "Cliente", "cliente@example.com", "Dirección", "token-previo");

		Carrito segundaCompra = new Carrito();
		Producto productoAuriculares = new Producto(1L, "Auriculares", "Descripción", BigDecimal.ONE, 12);
		Producto mochila = productoService.buscarPorId(2L).orElseThrow();
		segundaCompra.agregarProducto(mochila);
		segundaCompra.agregarProducto(productoAuriculares);
		segundaCompra.aumentarCantidad(1L, 12);
		int stockMochilas = productoService.buscarPorId(2L).orElseThrow().getStock();

		assertThrows(IllegalArgumentException.class,
				() -> pedidoService.confirmarPedido(segundaCompra, "Cliente", "cliente@example.com", "Dirección",
						"token-sin-stock"));

		assertEquals(stockMochilas, productoService.buscarPorId(2L).orElseThrow().getStock());
		assertTrue(pedidoService.buscarPorId(2L).isEmpty());
	}

	@Test
	void unTokenRepetidoDevuelveElPedidoExistenteSinDescontarStockOtraVez() {
		Carrito carrito = new Carrito();
		carrito.agregarProducto(productoService.buscarPorId(1L).orElseThrow());

		Pedido primerPedido = pedidoService.confirmarPedido(carrito, "Cliente", "cliente@example.com", "Dirección",
				"token-repetido");
		int stockLuegoDeLaCompra = productoService.buscarPorId(1L).orElseThrow().getStock();
		Pedido reintento = pedidoService.confirmarPedido(new Carrito(), "Otro", "otro@example.com", "Otra dirección",
				"token-repetido");

		assertEquals(primerPedido.getId(), reintento.getId());
		assertEquals(stockLuegoDeLaCompra, productoService.buscarPorId(1L).orElseThrow().getStock());
		assertTrue(pedidoService.buscarPorId(primerPedido.getId()).isPresent());
	}

	@Test
	void comprasConcurrentesNoVendenMasUnidadesQueLasDisponibles() throws Exception {
		Carrito primerCarrito = carritoConCantidad(1L, 8);
		Carrito segundoCarrito = carritoConCantidad(1L, 8);
		CountDownLatch inicio = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Future<Boolean> primerResultado = executor.submit(() -> confirmarTrasLaBarrera(primerCarrito, "token-a", inicio));
			Future<Boolean> segundoResultado = executor.submit(() -> confirmarTrasLaBarrera(segundoCarrito, "token-b", inicio));
			inicio.countDown();

			int comprasExitosas = (primerResultado.get() ? 1 : 0) + (segundoResultado.get() ? 1 : 0);
			assertEquals(1, comprasExitosas);
			assertEquals(4, productoService.buscarPorId(1L).orElseThrow().getStock());
		} finally {
			executor.shutdownNow();
		}
	}

	private Carrito carritoConCantidad(Long idProducto, int cantidad) {
		Producto producto = productoService.buscarPorId(idProducto).orElseThrow();
		Carrito carrito = new Carrito();
		carrito.agregarProducto(producto);
		for (int index = 1; index < cantidad; index++) {
			carrito.aumentarCantidad(idProducto, producto.getStock());
		}
		return carrito;
	}

	private boolean confirmarTrasLaBarrera(Carrito carrito, String token, CountDownLatch inicio) throws Exception {
		inicio.await();
		try {
			pedidoService.confirmarPedido(carrito, "Cliente", "cliente@example.com", "Dirección", token);
			return true;
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}
}
