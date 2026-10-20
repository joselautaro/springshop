package com.springshop.springshop.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.springshop.springshop.model.Carrito;
import com.springshop.springshop.model.DetallePedido;
import com.springshop.springshop.model.EstadoPedido;
import com.springshop.springshop.model.ItemCarrito;
import com.springshop.springshop.model.Pedido;
import com.springshop.springshop.model.Producto;

/**
 * Registra pedidos en memoria y coordina la confirmación con el stock del catálogo.
 */
@Service
public class PedidoService {

	private Map<Long, Pedido> pedidos = new HashMap<>();
	private Map<String, Long> pedidosPorToken = new HashMap<>();
	private final AtomicLong siguienteId = new AtomicLong();
	private final ProductoService productoService;

	public PedidoService(ProductoService productoService) {
		this.productoService = Objects.requireNonNull(productoService, "El servicio de productos es obligatorio");
	}

	/**
	 * Confirma el carrito usando precios actuales del catálogo. Repetir el token devuelve
	 * el pedido ya registrado, evitando descontar nuevamente el stock en un reintento.
	 */
	public synchronized Pedido confirmarPedido(Carrito carrito, String nombreCliente, String emailCliente,
			String direccionEntrega, String tokenCheckout) {
		if (tokenCheckout == null || tokenCheckout.isBlank()) {
			throw new IllegalArgumentException("El token de checkout es obligatorio");
		}

		Long idExistente = pedidosPorToken.get(tokenCheckout);
		if (idExistente != null) {
			return pedidos.get(idExistente);
		}

		validarDatosCliente(nombreCliente, emailCliente, direccionEntrega);
		Objects.requireNonNull(carrito, "El carrito es obligatorio");
		synchronized (productoService) {
			List<ItemCarrito> items = carrito.getItems();
			if (items.isEmpty()) {
				throw new IllegalArgumentException("No se puede confirmar un carrito vacío");
			}

			Map<Long, Integer> cantidades = new LinkedHashMap<>();
			List<DetallePedido> detalles = new ArrayList<>();
			for (ItemCarrito item : items) {
				if (item == null || item.getProducto() == null) {
					throw new IllegalArgumentException("El carrito contiene una línea inválida");
				}
				int cantidad = item.getCantidad();
				Long idProducto = item.getProducto().getId();
				if (idProducto == null || idProducto <= 0 || cantidad <= 0) {
					throw new IllegalArgumentException("El carrito contiene un producto o una cantidad inválida");
				}

				Producto productoActual = productoService.buscarPorId(idProducto)
						.orElseThrow(() -> new IllegalArgumentException(
								"El producto solicitado ya no existe en el catálogo"));
				int cantidadAcumulada;
				try {
					cantidadAcumulada = Math.addExact(cantidades.getOrDefault(idProducto, 0), cantidad);
				} catch (ArithmeticException exception) {
					throw new IllegalArgumentException("La cantidad solicitada es demasiado grande", exception);
				}
				cantidades.put(idProducto, cantidadAcumulada);

				if (productoActual.getStock() < cantidadAcumulada) {
					throw new IllegalArgumentException(
							"No hay stock suficiente para el producto " + productoActual.getNombre());
				}
				detalles.add(new DetallePedido(productoActual.getId(), productoActual.getNombre(), cantidad,
						productoActual.getPrecio()));
			}

			long idPedido = siguienteId.incrementAndGet();
			Pedido pedido = new Pedido(idPedido, nombreCliente, emailCliente, direccionEntrega, LocalDateTime.now(),
					detalles, EstadoPedido.CONFIRMADO);

			// Preparar ambos índices antes del cambio de stock evita exponer pedidos parciales.
			Map<Long, Pedido> pedidosActualizados = new HashMap<>(pedidos);
			pedidosActualizados.put(idPedido, pedido);
			Map<String, Long> tokensActualizados = new HashMap<>(pedidosPorToken);
			tokensActualizados.put(tokenCheckout, idPedido);

			productoService.descontarStock(cantidades);
			pedidos = pedidosActualizados;
			pedidosPorToken = tokensActualizados;
			return pedido;
		}
	}

	/**
	 * Busca un pedido por su identificador sin exponer el mapa interno.
	 */
	public synchronized Optional<Pedido> buscarPorId(Long id) {
		return Optional.ofNullable(pedidos.get(id));
	}

	private void validarDatosCliente(String nombreCliente, String emailCliente, String direccionEntrega) {
		if (nombreCliente == null || nombreCliente.isBlank()) {
			throw new IllegalArgumentException("El nombre del cliente es obligatorio");
		}
		if (emailCliente == null || emailCliente.isBlank()) {
			throw new IllegalArgumentException("El email del cliente es obligatorio");
		}
		if (direccionEntrega == null || direccionEntrega.isBlank()) {
			throw new IllegalArgumentException("La dirección de entrega es obligatoria");
		}
	}
}
