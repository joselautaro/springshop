package com.springshop.springshop.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springshop.springshop.model.Producto;

/**
 * Administra el catálogo en memoria; los datos se reinician al reiniciar la aplicación.
 */
@Service
public class ProductoService {

	private final ArrayList<Producto> productos = new ArrayList<>();

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
	public List<Producto> obtenerProductos() {
		return List.copyOf(productos);
	}

	/**
	 * Busca un producto del catálogo por su identificador para validar solicitudes del carrito.
	 */
	public Optional<Producto> buscarPorId(Long id) {
		return productos.stream()
				.filter(producto -> Objects.equals(producto.getId(), id))
				.findFirst();
	}
}
