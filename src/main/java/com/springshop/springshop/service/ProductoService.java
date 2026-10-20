package com.springshop.springshop.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springshop.springshop.model.Producto;

/**
 * Administra el catálogo en memoria; los datos se reinician al reiniciar la aplicación.
 */
@Service
public class ProductoService {

	private List<Producto> productos = new ArrayList<>();

	public ProductoService() {
		// Se cargan productos de ejemplo para que el catálogo esté disponible al iniciar.
		productos.add(new Producto(1L, "Auriculares Studio", "Sonido envolvente y comodidad para todo el día.",
				new BigDecimal("89.90"), 12));
		productos.add(new Producto(2L, "Mochila Urbana", "Diseño resistente con espacio para tus esenciales.",
				new BigDecimal("64.50"), 8));
		productos.add(new Producto(3L, "Botella Térmica", "Mantiene tus bebidas frías o calientes por horas.",
				new BigDecimal("28.00"), 24));
		productos.add(new Producto(4L, "Lámpara de Escritorio", "Luz regulable para acompañar tus ideas.",
				new BigDecimal("42.75"), 6));
		productos.add(new Producto(5L, "Cuaderno Clásico", "Papel de calidad para notas, planes y proyectos.",
				new BigDecimal("15.90"), 30));
	}

	/**
	 * Devuelve una vista no modificable para que las pantallas no alteren el catálogo.
	 */
	public synchronized List<Producto> obtenerProductos() {
		return List.copyOf(productos);
	}

	/**
	 * Busca un producto del catálogo por su identificador para validar solicitudes del carrito.
	 */
	public synchronized Optional<Producto> buscarPorId(Long id) {
		return productos.stream()
				.filter(producto -> Objects.equals(producto.getId(), id))
				.findFirst();
	}

	/**
	 * Descuenta en bloque cantidades previamente solicitadas, reemplazando el catálogo
	 * solo después de comprobar que todos los productos existen y tienen stock suficiente.
	 */
	public synchronized void descontarStock(Map<Long, Integer> cantidades) {
		Objects.requireNonNull(cantidades, "Las cantidades a descontar son obligatorias");
		if (cantidades.isEmpty()) {
			throw new IllegalArgumentException("Debe indicarse al menos un producto para descontar stock");
		}

		Map<Long, Integer> cantidadesValidadas = new HashMap<>();
		for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
			Long idProducto = Objects.requireNonNull(entrada.getKey(), "El identificador del producto es obligatorio");
			Integer cantidad = Objects.requireNonNull(entrada.getValue(), "La cantidad es obligatoria");
			if (idProducto <= 0 || cantidad <= 0) {
				throw new IllegalArgumentException("El producto y la cantidad deben ser válidos");
			}
			cantidadesValidadas.put(idProducto, cantidad);
		}

		Map<Long, Producto> productosActualizados = new HashMap<>();
		for (Producto producto : productos) {
			Integer cantidad = cantidadesValidadas.get(producto.getId());
			if (cantidad != null) {
				if (producto.getStock() < cantidad) {
					throw new IllegalArgumentException(
							"No hay stock suficiente para el producto " + producto.getNombre());
				}
				productosActualizados.put(producto.getId(), new Producto(producto.getId(), producto.getNombre(),
						producto.getDescripcion(), producto.getPrecio(), producto.getStock() - cantidad));
			} else {
				productosActualizados.put(producto.getId(), producto);
			}
		}

		if (!productosActualizados.keySet().containsAll(cantidadesValidadas.keySet())) {
			throw new IllegalArgumentException("Uno o más productos no existen en el catálogo");
		}

		// El reemplazo de referencia es el único cambio al catálogo y sucede tras validar el lote completo.
		List<Producto> catalogoActualizado = new ArrayList<>(productos.size());
		for (Producto producto : productos) {
			catalogoActualizado.add(productosActualizados.get(producto.getId()));
		}
		productos = catalogoActualizado;
	}
}
